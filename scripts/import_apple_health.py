#!/usr/bin/env python3
"""Import an Apple Health export into the backend (docs/HEALTH_DATA.md).

  python3 import_apple_health.py export.zip            parse and report only
  python3 import_apple_health.py export.zip --apply    also send (TYMB_URL, HEALTH_INGEST_TOKEN)

Runs locally. Only daily aggregates and per-workout totals leave this machine: no start times, GPS routes or device names.
"""
import argparse, collections, hashlib, json, math, os, re, sys, urllib.request, zipfile
from datetime import datetime
from xml.etree.ElementTree import XMLPullParser

CUMULATIVE = re.compile(r"^(StepCount|Distance\w+|\w*EnergyBurned|FlightsClimbed|Apple(Exercise|Stand|Move)Time|Dietary\w+|"
                        r"SwimmingStrokeCount|PushCount|TimeInDaylight|NumberOf\w+|InhalerUsage|InsulinDelivery)$")
# The owner chose not to import reproductive health (2026-11-23).
EXCLUDED = re.compile(r"Menstrua|Intermenstrual|Ovulation|Cervical|Pregnan|Lactation|Contracepti|Progesterone|SexualActivity|"
                      r"BleedingDuring|BleedingAfter|BasalBodyTemperature|Menopaus", re.I)
PREFIX = re.compile(r"^HK(QuantityTypeIdentifier|CategoryTypeIdentifier|CategoryValue|DataType|WorkoutActivityType)")
METERS = {"km": 1000.0, "mi": 1609.344, "m": 1.0, "ft": 0.3048, "yd": 0.9144}
SECONDS = {"min": 60.0, "s": 1.0, "hr": 3600.0}
KCAL = {"kcal": 1.0, "Cal": 1.0, "kJ": 1 / 4.184}


def short(name):
    return re.sub(r"[^A-Za-z0-9.]", "", PREFIX.sub("", name or ""))


def when(text):
    return datetime.strptime(text, "%Y-%m-%d %H:%M:%S %z")


def number(text):
    try:
        value = float(text)
    except (TypeError, ValueError):
        return None
    return value if math.isfinite(value) else None


def xml_chunks(stream):
    """The export without its DOCTYPE: some iOS versions write a DTD that XML parsers reject."""
    head = b""
    while b"<HealthData" not in head:
        part = stream.read(1 << 16)
        if not part:
            raise SystemExit("This file is not an Apple Health export (no HealthData element).")
        head += part
    # The DTD mentions HealthData only as "<!ELEMENT HealthData", so the first "<HealthData" is the root element.
    yield head[head.index(b"<HealthData"):]
    while part := stream.read(1 << 20):
        yield part


def elements(stream):
    parser = XMLPullParser(events=("start", "end"))
    root, depth = None, 0
    for chunk in xml_chunks(stream):
        parser.feed(chunk)
        for event, el in parser.read_events():
            if event == "start":
                depth += 1
                root = el if root is None else root
                continue
            depth -= 1
            if depth == 1:
                yield el
                root.clear()


def workout(el):
    duration = number(el.get("duration"))
    start, end = when(el.get("startDate")), when(el.get("endDate"))
    seconds = duration * SECONDS.get(el.get("durationUnit", "min"), 60.0) if duration else (end - start).total_seconds()
    meters = kcal = None
    d, e = number(el.get("totalDistance")), number(el.get("totalEnergyBurned"))
    if d:
        meters = d * METERS.get(el.get("totalDistanceUnit"), 1000.0)
    if e:
        kcal = e * KCAL.get(el.get("totalEnergyBurnedUnit"), 1.0)
    for s in el.findall("WorkoutStatistics"):
        kind, value = short(s.get("type")), number(s.get("sum"))
        if value is None:
            continue
        if kind.startswith("Distance") and s.get("unit") in METERS:
            meters = value * METERS[s.get("unit")]
        elif kind == "ActiveEnergyBurned":
            kcal = value * KCAL.get(s.get("unit"), 1.0)
    activity = short(el.get("workoutActivityType"))
    key = hashlib.sha256(f"{activity}|{int(start.timestamp())}".encode()).hexdigest()
    return {"start": start, "end": end, "watch": "watch" in (el.get("sourceName") or "").lower(),
            "row": {"sourceId": key, "activity": activity, "day": start.date().isoformat(), "distanceM": meters,
                    "durationS": seconds, "energyKcal": kcal}}


def dedupe(workouts):
    """One session saved by two apps overlaps; keep the Apple Watch copy, else the longer one."""
    kept = []
    for w in sorted(workouts, key=lambda w: w["start"]):
        twin = next((k for k in kept[-5:] if k["row"]["activity"] == w["row"]["activity"] and overlaps(k, w)), None)
        if twin is None:
            kept.append(w)
        elif (w["watch"], w["row"]["durationS"]) > (twin["watch"], twin["row"]["durationS"]):
            kept[kept.index(twin)] = w
    return [w["row"] for w in kept]


def overlaps(a, b):
    shared = (min(a["end"], b["end"]) - max(a["start"], b["start"])).total_seconds()
    shortest = min((a["end"] - a["start"]).total_seconds(), (b["end"] - b["start"]).total_seconds())
    return shortest > 0 and shared > shortest / 2


def parse(stream):
    workouts, skipped = [], collections.Counter()
    # (metric, day) -> source -> [sum, count, min, max, unit]; per source, because phone and watch both count the same steps.
    days = collections.defaultdict(dict)
    for el in elements(stream):
        try:
            if el.tag == "Workout":
                w = workout(el)
                if w["row"]["durationS"] > 0:
                    workouts.append(w)
            elif el.tag == "Record":
                kind = short(el.get("type"))
                if EXCLUDED.search(kind):
                    continue
                end = when(el.get("endDate"))
                day = when(el.get("startDate")).date()
                if el.get("type", "").startswith("HKCategoryType"):
                    value = (end - when(el.get("startDate"))).total_seconds() / 60
                    kind, unit, day = f"{kind}.{short(el.get('value'))}".rstrip("."), "min", end.date()
                else:
                    value, unit = number(el.get("value")), el.get("unit") or ""
                if value is None or not kind:
                    skipped[kind or "?"] += 1
                    continue
                slot = days[(kind, day.isoformat())].setdefault(el.get("sourceName") or "", [0.0, 0, value, value, unit])
                slot[0] += value; slot[1] += 1; slot[2] = min(slot[2], value); slot[3] = max(slot[3], value); slot[4] = unit
        except (TypeError, ValueError):
            skipped[el.tag] += 1
    daily = []
    for (metric, day), sources in sorted(days.items()):
        if CUMULATIVE.match(metric) or "." in metric or next(iter(sources.values()))[4] == "min":
            best = max(sources.values(), key=lambda s: s[0])
            row = {"total": best[0], "average": None, "minimum": None, "maximum": None, "samples": best[1], "unit": best[4]}
        else:
            s = list(sources.values())
            count = sum(x[1] for x in s)
            row = {"total": None, "average": sum(x[0] for x in s) / count, "minimum": min(x[2] for x in s),
                   "maximum": max(x[3] for x in s), "samples": count, "unit": s[0][4]}
        daily.append({"metric": metric, "day": day, **row})
    return dedupe(workouts), daily, skipped


def open_export(path):
    if not zipfile.is_zipfile(path):
        return open(path, "rb")
    z = zipfile.ZipFile(path)
    # The main file name is localized (export.xml, 輸出.xml); it is the largest XML that is not the CDA copy.
    names = [i for i in z.infolist() if i.filename.endswith(".xml") and "cda" not in i.filename.lower()]
    if not names:
        raise SystemExit("No export XML inside this zip.")
    return z.open(max(names, key=lambda i: i.file_size))


def send(base, token, path, rows):
    for i in range(0, len(rows), 1000):
        request = urllib.request.Request(f"{base}/health-data/{path}", data=json.dumps(rows[i:i + 1000]).encode(), method="POST",
                                         headers={"Content-Type": "application/json", "X-Health-Token": token})
        with urllib.request.urlopen(request, timeout=60):
            pass
        print(f"  {path}: {min(i + 1000, len(rows))}/{len(rows)}")


def main():
    args = argparse.ArgumentParser()
    args.add_argument("export")
    args.add_argument("--apply", action="store_true")
    args = args.parse_args()
    with open_export(args.export) as stream:
        workouts, daily, skipped = parse(stream)
    runs = [w for w in workouts if w["activity"] == "Running"]
    print(f"workouts {len(workouts)} (running {len(runs)}, {sum(w['distanceM'] or 0 for w in runs) / 1000:.1f} km); "
          f"daily rows {len(daily)} over {len({d['metric'] for d in daily})} metrics; skipped {sum(skipped.values())}")
    for metric, count in sorted(collections.Counter(d["metric"] for d in daily).items()):
        print(f"  {metric}: {count} days")
    if not args.apply:
        print("Check only; rerun with --apply to send.")
        return
    base, token = os.environ.get("TYMB_URL", "").rstrip("/"), os.environ.get("HEALTH_INGEST_TOKEN", "")
    if not base.startswith(("https://", "http://localhost")) or not token:
        sys.exit("Set TYMB_URL (https) and HEALTH_INGEST_TOKEN.")
    send(base, token, "workouts", workouts)
    send(base, token, "daily", daily)


if __name__ == "__main__":
    main()
