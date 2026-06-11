#!/usr/bin/env python3
"""
Claude Code Token Usage Sync

掃描 ~/.claude/projects/ 下所有 .jsonl，
抽出 assistant 訊息的 usage 欄位，POST 到 ty-multiverse-backend /ai-usage。

用法：
  python3 sync_claude_code_usage.py                  # 全量同步（首次）
  python3 sync_claude_code_usage.py --since 2026-06-01  # 只同步指定日期之後
  python3 sync_claude_code_usage.py --dry-run        # 只印出，不實際送出

環境變數：
  TYMB_URL              後端 URL，預設 http://localhost:8080/tymb
  AI_USAGE_INGEST_TOKEN 後端 ingest token（在 application.yml 設定的 ai-usage.ingest-token）
"""

import argparse
import json
import os
import sys
from datetime import datetime, timezone
from pathlib import Path
from typing import Optional
import urllib.request
import urllib.error

TYMB_URL = os.getenv("TYMB_URL", "http://localhost:8080/tymb")
INGEST_TOKEN = os.getenv("AI_USAGE_INGEST_TOKEN", "")
AI_USAGE_ENDPOINT = f"{TYMB_URL}/ai-usage"
CLAUDE_PROJECTS_DIR = Path.home() / ".claude" / "projects"

# Anthropic Claude Code 模型費率（USD per 1K tokens）
COST_RATES = {
    "claude-sonnet-4-6":              {"input": 0.003,   "output": 0.015,  "cache_read": 0.0003,  "cache_write": 0.00375},
    "claude-opus-4-8":                {"input": 0.015,   "output": 0.075,  "cache_read": 0.0015,  "cache_write": 0.01875},
    "claude-haiku-4-5-20251001":      {"input": 0.0008,  "output": 0.004,  "cache_read": 0.00008, "cache_write": 0.001},
    "claude-haiku-4-5":               {"input": 0.0008,  "output": 0.004,  "cache_read": 0.00008, "cache_write": 0.001},
    "claude-3-5-sonnet-20241022":     {"input": 0.003,   "output": 0.015,  "cache_read": 0.0003,  "cache_write": 0.00375},
    "claude-3-5-haiku-20241022":      {"input": 0.0008,  "output": 0.004,  "cache_read": 0.00008, "cache_write": 0.001},
}


def calculate_cost(model: str, usage: dict) -> Optional[float]:
    rates = COST_RATES.get(model)
    if not rates:
        return None
    input_t  = usage.get("input_tokens", 0)
    output_t = usage.get("output_tokens", 0)
    cache_r  = usage.get("cache_read_input_tokens", 0)
    cache_w  = usage.get("cache_creation_input_tokens", 0)
    cost = (
        rates["input"]       * input_t  / 1000 +
        rates["output"]      * output_t / 1000 +
        rates["cache_read"]  * cache_r  / 1000 +
        rates["cache_write"] * cache_w  / 1000
    )
    return round(cost, 8)


def post_usage(payload: dict, dry_run: bool) -> bool:
    if dry_run:
        print(f"  [DRY-RUN] {json.dumps(payload)}")
        return True
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
            return resp.status in (200, 201)
    except urllib.error.HTTPError as e:
        print(f"  HTTP {e.code}: {e.read().decode()[:200]}", file=sys.stderr)
        return False
    except Exception as e:
        print(f"  Error: {e}", file=sys.stderr)
        return False


def parse_since(since_str: Optional[str]) -> Optional[datetime]:
    if not since_str:
        return None
    return datetime.fromisoformat(since_str).replace(tzinfo=timezone.utc)


def scan_and_sync(since: Optional[datetime], dry_run: bool):
    jsonl_files = list(CLAUDE_PROJECTS_DIR.rglob("*.jsonl"))
    print(f"掃描 {len(jsonl_files)} 個 .jsonl 檔案...")

    seen_msg_ids: set = set()  # 用 message.id 去重，避免跨 jsonl 重複計算
    total = 0
    sent = 0
    skipped_time = 0
    skipped_dup = 0

    for path in jsonl_files:
        try:
            with open(path, encoding="utf-8") as f:
                for line in f:
                    line = line.strip()
                    if not line:
                        continue
                    try:
                        record = json.loads(line)
                    except json.JSONDecodeError:
                        continue

                    if record.get("type") != "assistant":
                        continue

                    msg = record.get("message", {})
                    usage = msg.get("usage")
                    if not usage:
                        continue

                    # 去重：同一個 API 回應可能出現在多個 jsonl
                    msg_id = msg.get("id")
                    if msg_id:
                        if msg_id in seen_msg_ids:
                            skipped_dup += 1
                            continue
                        seen_msg_ids.add(msg_id)

                    ts_str = record.get("timestamp")
                    if not ts_str:
                        continue

                    ts = datetime.fromisoformat(ts_str.replace("Z", "+00:00"))
                    if since and ts < since:
                        skipped_time += 1
                        continue

                    model = msg.get("model", "claude-unknown")
                    session_id = record.get("sessionId", "")
                    total += 1

                    payload = {
                        "aiProvider": "claude-code",
                        "modelName": model,
                        "inputTokens": usage.get("input_tokens", 0),
                        "outputTokens": usage.get("output_tokens", 0),
                        "cacheCreationInputTokens": usage.get("cache_creation_input_tokens", 0),
                        "cacheReadInputTokens": usage.get("cache_read_input_tokens", 0),
                        "estimatedCostUsd": calculate_cost(model, usage),
                        "sessionId": session_id,
                        "requestId": msg_id,
                        "endpoint": "claude-code-cli",
                        "status": "success",
                    }

                    ok = post_usage(payload, dry_run)
                    if ok:
                        sent += 1
                    else:
                        print(f"  失敗：{path.name} ts={ts_str}", file=sys.stderr)

        except Exception as e:
            print(f"讀取 {path} 失敗：{e}", file=sys.stderr)

    print(f"\n完成：共 {total} 筆（去重後），送出 {sent} 筆，跳過重複 {skipped_dup} 筆，跳過早於 since {skipped_time} 筆")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Claude Code token usage sync")
    parser.add_argument("--since", help="只同步此日期之後的記錄，格式 YYYY-MM-DD")
    parser.add_argument("--dry-run", action="store_true", help="只印出不送出")
    args = parser.parse_args()

    since = parse_since(args.since)
    scan_and_sync(since=since, dry_run=args.dry_run)
