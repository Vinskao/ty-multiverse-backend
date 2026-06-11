#!/usr/bin/env python3
"""
Sync ccusage daily aggregates directly into PostgreSQL.

This bypasses the backend /ai-usage ingest endpoint and writes one row per
source device + day + provider + model. Re-running updates the same device row.
"""

import argparse
import json
import os
import platform
import re
import shutil
import subprocess
import sys
from datetime import datetime, timezone
from decimal import Decimal
from pathlib import Path
from typing import Optional
from urllib.parse import parse_qs, urlparse

import psycopg2


PROJECT_DIR = Path(__file__).resolve().parent.parent
LOCAL_PROPERTIES = PROJECT_DIR / "src/main/resources/env/local.properties"
SCHEMA_SQL = PROJECT_DIR / "db/ai_token_usage_granularity.sql"
SOURCE_DEVICE = os.getenv("AI_USAGE_SOURCE_DEVICE") or platform.node() or "unknown"


def load_properties(path: Path) -> dict[str, str]:
    props: dict[str, str] = {}
    with path.open(encoding="utf-8") as prop_file:
        for raw_line in prop_file:
            line = raw_line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            key, value = line.split("=", 1)
            props[key.strip()] = value.strip()
    return props


def jdbc_to_dsn(jdbc_url: str, username: str, password: str) -> str:
    url = jdbc_url.removeprefix("jdbc:")
    parsed = urlparse(url)
    query = parse_qs(parsed.query)
    sslmode = query.get("sslmode", ["require"])[0]
    database = parsed.path.lstrip("/")
    return (
        f"host={parsed.hostname} port={parsed.port or 5432} dbname={database} "
        f"user={username} password={password} sslmode={sslmode}"
    )


def infer_provider(model_name: str) -> str:
    model = model_name.lower()
    if model.startswith("claude"):
        return "claude-code"
    if model.startswith("gpt") or "codex" in model:
        return "codex"
    if model.startswith("gemini"):
        return "gemini-cli"
    if model.startswith("qwen") or "dashscope" in model:
        return "qwen"
    return "unknown"


def run_ccusage() -> list[dict]:
    ccusage_bin = shutil.which("ccusage") or shutil.which("ccusage.cmd")
    if not ccusage_bin:
        print("ccusage not found. Install with: npm install -g ccusage", file=sys.stderr)
        sys.exit(1)

    result = subprocess.run(
        [ccusage_bin, "daily", "--json"],
        capture_output=True,
        text=True,
        timeout=30,
    )
    if result.returncode != 0:
        print(f"ccusage failed: {result.stderr}", file=sys.stderr)
        sys.exit(1)
    return json.loads(result.stdout).get("daily", [])


def apply_schema(conn) -> None:
    if not SCHEMA_SQL.is_file():
        return
    with conn.cursor() as cur:
        cur.execute(SCHEMA_SQL.read_text(encoding="utf-8"))
    conn.commit()


def existing_id(cur, payload: dict) -> Optional[int]:
    cur.execute(
        """
        SELECT id
        FROM ai_token_usage
        WHERE ((called_at AT TIME ZONE 'UTC')::date) = %s::date
          AND model_name = %s
          AND ai_provider = %s
          AND granularity = 'daily_aggregate'
          AND source_device = %s
        LIMIT 1
        """,
        (
            payload["period"],
            payload["model_name"],
            payload["ai_provider"],
            payload["source_device"],
        ),
    )
    row = cur.fetchone()
    return row[0] if row else None


def upsert_usage(cur, payload: dict) -> str:
    row_id = existing_id(cur, payload)
    values = (
        payload["source_device"],
        payload["ai_provider"],
        payload["model_name"],
        payload["input_tokens"],
        payload["output_tokens"],
        payload["cache_creation_input_tokens"],
        payload["cache_read_input_tokens"],
        payload["estimated_cost_usd"],
        payload["endpoint"],
        payload["status"],
        payload["granularity"],
        payload["called_at"],
    )

    if row_id:
        cur.execute(
            """
            UPDATE ai_token_usage
            SET source_device = %s,
                ai_provider = %s,
                model_name = %s,
                input_tokens = %s,
                output_tokens = %s,
                cache_creation_input_tokens = %s,
                cache_read_input_tokens = %s,
                estimated_cost_usd = %s,
                endpoint = %s,
                status = %s,
                granularity = %s,
                called_at = %s
            WHERE id = %s
            """,
            values + (row_id,),
        )
        return "updated"

    cur.execute(
        """
        INSERT INTO ai_token_usage (
            source_device,
            ai_provider,
            model_name,
            input_tokens,
            output_tokens,
            cache_creation_input_tokens,
            cache_read_input_tokens,
            estimated_cost_usd,
            endpoint,
            status,
            granularity,
            called_at
        )
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        """,
        values,
    )
    return "inserted"


def sync(since: Optional[datetime], dry_run: bool) -> None:
    props = load_properties(LOCAL_PROPERTIES)
    dsn = jdbc_to_dsn(
        props["spring.datasource.url"],
        props["spring.datasource.username"],
        props["spring.datasource.password"],
    )

    daily_data = run_ccusage()
    print(f"ccusage returned {len(daily_data)} days")

    inserted = updated = skipped = errors = total = 0
    with psycopg2.connect(dsn) as conn:
        apply_schema(conn)
        with conn.cursor() as cur:
            for day in daily_data:
                period = day.get("period", "")
                if not period:
                    continue
                ts = datetime.fromisoformat(f"{period}T00:00:00+00:00")
                if since and ts < since:
                    skipped += 1
                    continue

                for breakdown in day.get("modelBreakdowns", []):
                    model_name = breakdown.get("modelName", "")
                    if not model_name:
                        continue
                    total += 1
                    payload = {
                        "period": period,
                        "source_device": SOURCE_DEVICE,
                        "ai_provider": infer_provider(model_name),
                        "model_name": model_name,
                        "input_tokens": breakdown.get("inputTokens", 0),
                        "output_tokens": breakdown.get("outputTokens", 0),
                        "cache_creation_input_tokens": breakdown.get("cacheCreationTokens", 0),
                        "cache_read_input_tokens": breakdown.get("cacheReadTokens", 0),
                        "estimated_cost_usd": Decimal(str(breakdown.get("cost") or 0)),
                        "endpoint": "ccusage-direct-db-sync",
                        "status": "success",
                        "granularity": "daily_aggregate",
                        "called_at": f"{period}T00:00:00+00:00",
                    }

                    if dry_run:
                        print(f"[DRY-RUN] {payload}")
                        inserted += 1
                        continue

                    try:
                        result = upsert_usage(cur, payload)
                        if result == "inserted":
                            inserted += 1
                        else:
                            updated += 1
                    except Exception as exc:
                        conn.rollback()
                        errors += 1
                        print(f"failed {period} {model_name}: {exc}", file=sys.stderr)
                    else:
                        conn.commit()

    print(
        f"done: total={total}, inserted={inserted}, updated={updated}, "
        f"skipped_before_since={skipped}, errors={errors}"
    )


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Sync ccusage directly into PostgreSQL")
    parser.add_argument("--since", help="Only sync dates on or after YYYY-MM-DD")
    parser.add_argument("--dry-run", action="store_true")
    args = parser.parse_args()

    since = None
    if args.since:
        since = datetime.fromisoformat(args.since).replace(tzinfo=timezone.utc)

    sync(since=since, dry_run=args.dry_run)
