#!/usr/bin/env python3
"""Upload every TCX workout file in a folder to POST /health-data/workout-tcx, one after another.

For a pile of files exported from WorkOutDoors, sharing them one by one from the phone is slow. This does the same upload from a
computer: it paces itself below the gateway's 12 requests a minute, keeps a log so a second run skips what already went through,
and prints what the server read from each file (kind, distance, heart rate, climb) so a wrong number is visible right away.

  HEALTH_INGEST_TOKEN   the ingest token (never printed, never logged)
  TYMB_URL              default https://tymultiverse.cc/tymb

  python -I -B scripts/upload_tcx_folder.py F:/D/tcx --dry-run     # list what would be sent
  python -I -B scripts/upload_tcx_folder.py F:/D/tcx               # send

Uploading the same file again is safe: the server updates the same workout instead of adding another one.
"""
import argparse
import json
import os
import sys
import time
import urllib.error
import urllib.request
from pathlib import Path

DEFAULT_URL = "https://tymultiverse.cc/tymb"
MAX_BYTES = 30 * 1024 * 1024  # the server's own limit
LOG_NAME = "upload_tcx.log.jsonl"


def find_files(folder):
    return sorted(p for p in Path(folder).rglob("*") if p.is_file() and p.suffix.lower() == ".tcx")


def load_done(log_path):
    """File name and size of every upload the log says succeeded, so a rerun skips them."""
    done = set()
    if not log_path.exists():
        return done
    for line in log_path.read_text(encoding="utf-8").splitlines():
        try:
            row = json.loads(line)
        except ValueError:
            continue
        if row.get("status") == 200:
            done.add((row.get("file"), row.get("size")))
    return done


def plan(files, done):
    """(to_send, skipped_done, too_big). A file is identified by its name and size."""
    send, skipped, big = [], [], []
    for path in files:
        size = path.stat().st_size
        if size > MAX_BYTES:
            big.append(path)
        elif (path.name, size) in done:
            skipped.append(path)
        else:
            send.append(path)
    return send, skipped, big


def describe(reply):
    """One readable line from the server's reply: what it understood from the file."""
    data = reply.get("data") or {}
    parts = [str(data.get("kind") or data.get("activity") or "?"), str(data.get("day") or "?")]
    if data.get("distanceM") is not None:
        parts.append(f"{data['distanceM'] / 1000:.2f} km")
    if data.get("durationS") is not None:
        seconds = int(data["durationS"])
        parts.append(f"{seconds // 3600}:{seconds % 3600 // 60:02d}:{seconds % 60:02d}")
    if data.get("avgHr") is not None:
        parts.append(f"HR {data['avgHr']}/{data.get('maxHr')}")
    if data.get("ascentM") is not None:
        parts.append(f"+{data['ascentM']} m")
    return "  ".join(parts)


def upload(url, token, path):
    request = urllib.request.Request(url + "/health-data/workout-tcx", data=path.read_bytes(), method="POST",
                                     headers={"X-Health-Token": token, "Content-Type": "application/xml"})
    try:
        with urllib.request.urlopen(request, timeout=120) as response:
            return response.status, json.loads(response.read().decode("utf-8"))
    except urllib.error.HTTPError as error:
        try:
            body = json.loads(error.read().decode("utf-8"))
        except ValueError:
            body = {"message": error.reason}
        return error.code, body


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("folder")
    parser.add_argument("--dry-run", action="store_true", help="only list what would be sent")
    parser.add_argument("--delay", type=float, default=6.5, help="seconds between uploads (the gateway allows 12 a minute)")
    parser.add_argument("--log", help=f"progress log, default <folder>/{LOG_NAME}")
    args = parser.parse_args(argv)

    folder = Path(args.folder)
    if not folder.is_dir():
        print(f"not a folder: {folder}", file=sys.stderr)
        return 2
    log_path = Path(args.log) if args.log else folder / LOG_NAME
    files = find_files(folder)
    send, skipped, big = plan(files, load_done(log_path))
    print(f"{len(files)} TCX files: {len(send)} to send, {len(skipped)} already done, {len(big)} over 30 MB")
    for path in big:
        print(f"  too big, skipped: {path.name}")
    if args.dry_run or not send:
        for path in send:
            print(f"  would send {path.name}")
        return 0

    token = os.environ.get("HEALTH_INGEST_TOKEN", "")
    if not token:
        print("set HEALTH_INGEST_TOKEN first", file=sys.stderr)
        return 2
    url = os.environ.get("TYMB_URL", DEFAULT_URL).rstrip("/")
    failures = 0
    with log_path.open("a", encoding="utf-8") as log:
        for number, path in enumerate(send, 1):
            status, reply = 0, {}
            for attempt in range(4):
                status, reply = upload(url, token, path)
                if status != 429:
                    break
                print("  rate limited, waiting 20 s")
                time.sleep(20)
            ok = status == 200
            failures += 0 if ok else 1
            outcome = describe(reply) if ok else "HTTP %s %s" % (status, reply.get("message"))
            print("[%d/%d] %s: %s" % (number, len(send), path.name, outcome))
            log.write(json.dumps({"file": path.name, "size": path.stat().st_size, "status": status}, ensure_ascii=False) + "\n")
            log.flush()
            if status >= 500 or status == 401:
                print("stopping: the server or the token is the problem, not this file", file=sys.stderr)
                return 1
            if number < len(send):
                time.sleep(args.delay)
    print(f"done, {failures} failed")
    return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main())
