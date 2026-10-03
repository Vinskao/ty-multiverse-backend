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
  TYMB_URL                 後端 URL，預設 http://localhost:8080/tymb
  AI_USAGE_INGEST_TOKEN    後端 ingest token
  CCUSAGE_TIMEOUT_SECONDS  ccusage 執行逾時，預設 180
  AI_USAGE_MAX_ATTEMPTS    單筆 POST 遇網路錯誤/429/5xx 的最多嘗試次數，預設 4（退避 2s、4s、8s）
  AI_USAGE_NOTIFY          macOS 上整批失敗時跳通知，預設 1

整批結果：成功 exit 0；有任何一筆失敗、ccusage 失敗或後端連不上 → stderr 印出帶時間戳的
「RUN FAILED」並以非 0 結束（launchd 的 last exit code 可見），macOS 另跳系統通知。
"""

import argparse
import json
import os
import platform
import shutil
import subprocess
import sys
import time
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

CCUSAGE_TIMEOUT_SECONDS = int(os.getenv("CCUSAGE_TIMEOUT_SECONDS", "180"))
MAX_ATTEMPTS = int(os.getenv("AI_USAGE_MAX_ATTEMPTS", "4"))
BACKOFF_SECONDS = 2.0
# 連續這麼多筆都是網路錯誤（每筆已各自重試過）就視為後端連不上，停止本次執行
MAX_CONSECUTIVE_NETWORK_FAILURES = 5
RETRYABLE_HTTP = {429, 502, 503, 504}
NETWORK_FAILURE = 0  # post_usage 的回傳值：重試後仍是網路層錯誤


class SyncFailure(Exception):
    """整批同步無法進行（ccusage 失敗、後端連不上等）。"""


def now_iso() -> str:
    return datetime.now(timezone.utc).astimezone().isoformat(timespec="seconds")

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
    """POST payload，回傳 HTTP status code；重試後仍是網路錯誤回傳 NETWORK_FAILURE。dry-run 時回傳 201。

    網路錯誤（DNS、連線、逾時）與 429/502/503/504 會以指數退避重試，最多 MAX_ATTEMPTS 次；
    其他 4xx 不重試。409 代表已存在，視為正常。
    """
    if dry_run:
        print(f"  [DRY-RUN] {json.dumps(payload)}")
        return 201
    body = json.dumps(payload).encode("utf-8")
    for attempt in range(1, MAX_ATTEMPTS + 1):
        try:
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
            if e.code in RETRYABLE_HTTP and attempt < MAX_ATTEMPTS:
                time.sleep(BACKOFF_SECONDS * 2 ** (attempt - 1))
                continue
            print(f"  HTTP {e.code}: {e.read().decode()[:200]}", file=sys.stderr)
            return e.code
        except (urllib.error.URLError, TimeoutError, ConnectionError, OSError) as e:
            if attempt < MAX_ATTEMPTS:
                time.sleep(BACKOFF_SECONDS * 2 ** (attempt - 1))
                continue
            print(f"  Error（重試 {MAX_ATTEMPTS} 次仍失敗）: {e}", file=sys.stderr)
            return NETWORK_FAILURE
    return NETWORK_FAILURE


def run_ccusage() -> list:
    """執行 ccusage daily --json，回傳 daily 陣列；失敗時拋出 SyncFailure。"""
    ccusage_bin = shutil.which("ccusage") or shutil.which("ccusage.cmd")
    if not ccusage_bin:
        raise SyncFailure("找不到 ccusage，請先執行: npm install -g ccusage")
    try:
        result = subprocess.run(
            [ccusage_bin, "daily", "--json"],
            capture_output=True, text=True, timeout=CCUSAGE_TIMEOUT_SECONDS
        )
    except subprocess.TimeoutExpired:
        raise SyncFailure(f"ccusage timed out after {CCUSAGE_TIMEOUT_SECONDS}s "
                          "(可用 CCUSAGE_TIMEOUT_SECONDS 調整)")
    if result.returncode != 0:
        raise SyncFailure(f"ccusage 執行失敗 (exit {result.returncode}): {result.stderr.strip()[:300]}")
    try:
        data = json.loads(result.stdout)
    except json.JSONDecodeError as e:
        raise SyncFailure(f"ccusage 輸出解析失敗: {e}")
    return data.get("daily", [])


def notify_failure(message: str) -> None:
    """macOS 上跳系統通知（launchd 背景執行時也看得到）；其他平台或失敗時靜默略過。"""
    if platform.system() != "Darwin" or os.getenv("AI_USAGE_NOTIFY", "1") == "0":
        return
    text = message.replace('"', "'")[:200]
    try:
        subprocess.run(["osascript", "-e", f'display notification "{text}" with title "ccusage sync failed"'],
                       capture_output=True, timeout=10)
    except Exception:
        pass


def sync(since: Optional[datetime], dry_run: bool) -> dict:
    """同步一次，回傳統計。ccusage 失敗或後端持續連不上時拋出 SyncFailure。"""
    daily_data = run_ccusage()
    print(f"{now_iso()} ccusage 回傳 {len(daily_data)} 天的資料")

    total = sent = skipped_time = skipped_dup = errors = network_errors = 0
    consecutive_network = 0

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
                consecutive_network = 0
            elif status_code == 409:
                skipped_dup += 1
                consecutive_network = 0
            else:
                errors += 1
                print(f"  失敗 {status_code}：{period} {model_name}", file=sys.stderr)
                if status_code == NETWORK_FAILURE:
                    network_errors += 1
                    consecutive_network += 1
                    if consecutive_network >= MAX_CONSECUTIVE_NETWORK_FAILURES:
                        raise SyncFailure(
                            f"後端連不上（{AI_USAGE_ENDPOINT}），連續 {consecutive_network} 筆重試後仍失敗，"
                            f"已送出 {sent} 筆後中止")
                else:
                    consecutive_network = 0

    print(f"{now_iso()} 完成：共 {total} 筆，送出 {sent}，已存在跳過 {skipped_dup}，"
          f"跳過早於 since {skipped_time}，錯誤 {errors}")
    return {"total": total, "sent": sent, "errors": errors, "network_errors": network_errors}


def main(argv=None) -> int:
    parser = argparse.ArgumentParser(description="ccusage → ty-multiverse-backend sync")
    parser.add_argument("--since", help="只同步此日期之後，格式 YYYY-MM-DD")
    parser.add_argument("--dry-run", action="store_true", help="只印出不送出")
    args = parser.parse_args(argv)

    since = None
    if args.since:
        since = datetime.fromisoformat(args.since).replace(tzinfo=timezone.utc)

    try:
        result = sync(since=since, dry_run=args.dry_run)
    except SyncFailure as e:
        message = f"RUN FAILED: {e}"
        print(f"{now_iso()} {message}", file=sys.stderr)
        notify_failure(message)
        return 1

    if result["errors"]:
        message = (f"RUN FAILED: {result['errors']}/{result['total']} 筆失敗"
                   f"（網路 {result['network_errors']}，其他 {result['errors'] - result['network_errors']}）")
        print(f"{now_iso()} {message}", file=sys.stderr)
        notify_failure(message)
        return 2
    return 0


if __name__ == "__main__":
    sys.exit(main())
