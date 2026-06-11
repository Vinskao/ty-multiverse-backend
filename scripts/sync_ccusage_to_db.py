#!/usr/bin/env python3
"""
ccusage → ty-multiverse-backend bridge

每次執行讀取 `ccusage daily --json`，將 modelBreakdowns 逐筆 POST 到後端。
granularity = 'daily_aggregate'，DB 有 unique index 防止重複寫入。

用法：
  python3 sync_ccusage_to_db.py                    # 全量同步
  python3 sync_ccusage_to_db.py --since 2026-06-01 # 只同步指定日期之後
  python3 sync_ccusage_to_db.py --dry-run          # 只印出，不實際送出

環境變數：
  TYMB_URL              後端 URL，預設 http://localhost:8080/tymb
  AI_USAGE_INGEST_TOKEN 後端 ingest token
"""

import argparse
import json
import os
import platform
import shutil
import subprocess
import sys
from datetime import datetime, timezone
from typing import Optional
import urllib.request
import urllib.error


def load_local_env() -> None:
    env_path = os.path.join(os.path.dirname(__file__), "..", ".env.local")
    try:
        with open(env_path, encoding="utf-8") as env_file:
            for raw_line in env_file:
                line = raw_line.strip()
                if not line or line.startswith("#") or "=" not in line:
                    continue
                key, value = line.split("=", 1)
                os.environ.setdefault(key.strip(), value.strip())
    except FileNotFoundError:
        pass


load_local_env()

TYMB_URL = os.getenv("TYMB_URL", "http://localhost:8080/tymb")
INGEST_TOKEN = os.getenv("AI_USAGE_INGEST_TOKEN", "")
AI_USAGE_ENDPOINT = f"{TYMB_URL}/ai-usage"
SOURCE_DEVICE = os.getenv("AI_USAGE_SOURCE_DEVICE") or platform.node() or "unknown"

# model 名稱前綴 → aiProvider
def infer_provider(model_name: str) -> str:
    m = model_name.lower()
    if m.startswith("claude"):
        return "claude-code"
    if m.startswith("gpt") or "codex" in m:
        return "codex"
    if m.startswith("gemini"):
        return "gemini-cli"
    if m.startswith("qwen") or "dashscope" in m:
        return "qwen"
    return "unknown"


def post_usage(payload: dict, dry_run: bool) -> int:
    """POST payload，回傳 HTTP status code。dry-run 時回傳 201。"""
    if dry_run:
        print(f"  [DRY-RUN] {json.dumps(payload)}")
        return 201
    try:
        body = json.dumps(payload).encode("utf-8")
        req = urllib.request.Request(
            AI_USAGE_ENDPOINT,
            data=body,
            headers={
                "Content-Type": "application/json",
                "X-AI-Usage-Token": INGEST_TOKEN,
            },
            method="POST",
        )
        with urllib.request.urlopen(req, timeout=8) as resp:
            return resp.status
    except urllib.error.HTTPError as e:
        if e.code == 409:
            return 409  # 重複，已存在，正常
        print(f"  HTTP {e.code}: {e.read().decode()[:200]}", file=sys.stderr)
        return e.code
    except Exception as e:
        print(f"  Error: {e}", file=sys.stderr)
        return 0


def run_ccusage() -> list:
    """執行 ccusage daily --json，回傳 daily 陣列。"""
    try:
        ccusage_bin = shutil.which("ccusage") or shutil.which("ccusage.cmd")
        if not ccusage_bin:
            raise FileNotFoundError()
        result = subprocess.run(
            [ccusage_bin, "daily", "--json"],
            capture_output=True, text=True, timeout=30
        )
        if result.returncode != 0:
            print(f"ccusage 執行失敗: {result.stderr}", file=sys.stderr)
            sys.exit(1)
        data = json.loads(result.stdout)
        return data.get("daily", [])
    except FileNotFoundError:
        print("找不到 ccusage，請先執行: npm install -g ccusage", file=sys.stderr)
        sys.exit(1)
    except json.JSONDecodeError as e:
        print(f"ccusage 輸出解析失敗: {e}", file=sys.stderr)
        sys.exit(1)


def sync(since: Optional[datetime], dry_run: bool):
    daily_data = run_ccusage()
    print(f"ccusage 回傳 {len(daily_data)} 天的資料")

    total = sent = skipped_time = skipped_dup = errors = 0

    for day in daily_data:
        period = day.get("period", "")
        if not period:
            continue

        ts = datetime.fromisoformat(f"{period}T00:00:00+00:00")
        if since and ts < since:
            skipped_time += 1
            continue

        for breakdown in day.get("modelBreakdowns", []):
            model_name = breakdown.get("modelName", "")
            if not model_name:
                continue

            total += 1
            payload = {
                "sourceDevice": SOURCE_DEVICE,
                "aiProvider": infer_provider(model_name),
                "modelName": model_name,
                "inputTokens": breakdown.get("inputTokens", 0),
                "outputTokens": breakdown.get("outputTokens", 0),
                "cacheCreationInputTokens": breakdown.get("cacheCreationTokens", 0),
                "cacheReadInputTokens": breakdown.get("cacheReadTokens", 0),
                "estimatedCostUsd": breakdown.get("cost"),
                "endpoint": "ccusage-daily-sync",
                "status": "success",
                "granularity": "daily_aggregate",
                # called_at 設為當天 UTC 00:00，DB unique index 以日期為鍵
                "calledAt": f"{period}T00:00:00Z",
            }

            status_code = post_usage(payload, dry_run)
            if status_code in (200, 201):
                sent += 1
            elif status_code == 409:
                skipped_dup += 1
            else:
                errors += 1
                print(f"  失敗 {status_code}：{period} {model_name}", file=sys.stderr)

    print(f"\n完成：共 {total} 筆，送出 {sent}，已存在跳過 {skipped_dup}，"
          f"跳過早於 since {skipped_time}，錯誤 {errors}")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="ccusage → ty-multiverse-backend sync")
    parser.add_argument("--since", help="只同步此日期之後，格式 YYYY-MM-DD")
    parser.add_argument("--dry-run", action="store_true", help="只印出不送出")
    args = parser.parse_args()

    since = None
    if args.since:
        since = datetime.fromisoformat(args.since).replace(tzinfo=timezone.utc)

    sync(since=since, dry_run=args.dry_run)
