import io
import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import import_apple_health as health

XML = """<?xml version="1.0"?><!DOCTYPE HealthData [ <!ELEMENT HealthData (broken ]>
<HealthData locale="zh_TW">
 <Record type="HKQuantityTypeIdentifierStepCount" sourceName="iPhone" unit="count" value="100" startDate="2026-11-01 08:00:00 +0800" endDate="2026-11-01 08:10:00 +0800"/>
 <Record type="HKQuantityTypeIdentifierStepCount" sourceName="Watch" unit="count" value="90" startDate="2026-11-01 08:00:00 +0800" endDate="2026-11-01 08:10:00 +0800"/>
 <Record type="HKQuantityTypeIdentifierStepCount" sourceName="Watch" unit="count" value="60" startDate="2026-11-01 09:00:00 +0800" endDate="2026-11-01 09:10:00 +0800"/>
 <Record type="HKQuantityTypeIdentifierHeartRate" sourceName="Watch" unit="count/min" value="60" startDate="2026-11-01 08:00:00 +0800" endDate="2026-11-01 08:00:00 +0800"/>
 <Record type="HKQuantityTypeIdentifierHeartRate" sourceName="Watch" unit="count/min" value="80" startDate="2026-11-01 09:00:00 +0800" endDate="2026-11-01 09:00:00 +0800"/>
 <Record type="HKCategoryTypeIdentifierSleepAnalysis" sourceName="Watch" value="HKCategoryValueSleepAnalysisAsleepCore" startDate="2026-11-01 23:30:00 +0800" endDate="2026-11-02 00:30:00 +0800"/>
 <Record type="HKCategoryTypeIdentifierMenstrualFlow" sourceName="x" value="HKCategoryValueVaginalBleedingUnspecified" startDate="2026-11-01 08:00:00 +0800" endDate="2026-11-01 09:00:00 +0800"/>
 <Record type="HKQuantityTypeIdentifierAtrialFibrillationBurden" sourceName="Watch" unit="%" value="0.02" startDate="2026-11-01 08:00:00 +0800" endDate="2026-11-01 09:00:00 +0800"/>
 <Record type="HKQuantityTypeIdentifierBodyMass" sourceName="Scale" unit="kg" value="bad" startDate="2026-11-01 08:00:00 +0800" endDate="2026-11-01 08:00:00 +0800"/>
 <Workout workoutActivityType="HKWorkoutActivityTypeRunning" duration="25" durationUnit="min" sourceName="Apple Watch" startDate="2026-11-01 06:00:00 +0800" endDate="2026-11-01 06:25:00 +0800">
  <WorkoutStatistics type="HKQuantityTypeIdentifierDistanceWalkingRunning" sum="5" unit="km"/>
  <WorkoutStatistics type="HKQuantityTypeIdentifierActiveEnergyBurned" sum="300" unit="kcal"/></Workout>
 <Workout workoutActivityType="HKWorkoutActivityTypeRunning" duration="26" durationUnit="min" sourceName="Strava" totalDistance="5.1" totalDistanceUnit="km" startDate="2026-11-01 06:00:05 +0800" endDate="2026-11-01 06:26:00 +0800"/>
 <Workout workoutActivityType="HKWorkoutActivityTypeRunning" duration="10" durationUnit="min" sourceName="Old" totalDistance="1" totalDistanceUnit="mi" startDate="2020-01-01 06:00:00 +0800" endDate="2020-01-01 06:10:00 +0800"/>
</HealthData>"""


class AppleHealthTest(unittest.TestCase):
    def setUp(self):
        self.workouts, rows, self.skipped = health.parse(io.BytesIO(XML.encode()))
        self.daily = {(r["metric"], r["day"]): r for r in rows}

    def test_one_run_saved_by_two_apps_is_kept_once_from_the_watch(self):
        self.assertEqual([(w["day"], round(w["distanceM"])) for w in self.workouts], [("2020-01-01", 1609), ("2026-11-01", 5000)])
        self.assertEqual(self.workouts[1]["durationS"], 1500)
        self.assertEqual(self.workouts[1]["energyKcal"], 300)

    def test_phone_and_watch_steps_are_not_added_together(self):
        self.assertEqual(self.daily[("StepCount", "2026-11-01")]["total"], 150)

    def test_discrete_metric_is_averaged_and_bad_values_are_skipped(self):
        row = self.daily[("HeartRate", "2026-11-01")]
        self.assertEqual((row["average"], row["minimum"], row["maximum"], row["total"]), (70, 60, 80, None))
        self.assertEqual(self.skipped["BodyMass"], 1)

    def test_sleep_minutes_belong_to_the_wake_up_day(self):
        self.assertEqual(self.daily[("SleepAnalysis.SleepAnalysisAsleepCore", "2026-11-02")]["total"], 60)

    def test_reproductive_health_is_never_imported(self):
        metrics = {m for m, _ in self.daily}
        self.assertFalse([m for m in metrics if "Menstrual" in m])
        self.assertIn("AtrialFibrillationBurden", metrics)

    def test_no_time_or_source_leaves_the_machine(self):
        self.assertEqual(set(self.workouts[0]), {"sourceId", "activity", "day", "distanceM", "durationS", "energyKcal"})


if __name__ == "__main__":
    unittest.main()
