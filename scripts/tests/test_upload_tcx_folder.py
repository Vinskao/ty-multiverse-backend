import json
import sys
import tempfile
import unittest
from pathlib import Path
from unittest import mock

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import upload_tcx_folder as up


def make(folder, name, size=10):
    path = Path(folder) / name
    path.write_bytes(b"x" * size)
    return path


class PlanTest(unittest.TestCase):
    def test_finds_tcx_files_in_subfolders_and_ignores_other_files(self):
        with tempfile.TemporaryDirectory() as tmp:
            make(tmp, "b.tcx")
            make(tmp, "a.TCX")
            make(tmp, "notes.txt")
            (Path(tmp) / "sub").mkdir()
            make(Path(tmp) / "sub", "c.tcx")
            self.assertEqual(["a.TCX", "b.tcx", "c.tcx"], sorted(p.name for p in up.find_files(tmp)))

    def test_a_second_run_skips_files_the_log_says_went_through(self):
        with tempfile.TemporaryDirectory() as tmp:
            done_file = make(tmp, "done.tcx", 10)
            new_file = make(tmp, "new.tcx", 10)
            changed = make(tmp, "changed.tcx", 20)
            send, skipped, big = up.plan([done_file, new_file, changed], {("done.tcx", 10), ("changed.tcx", 99)})
            self.assertEqual(["new.tcx", "changed.tcx"], [p.name for p in send])
            self.assertEqual(["done.tcx"], [p.name for p in skipped])
            self.assertEqual([], big)

    def test_a_file_over_the_server_limit_is_not_sent(self):
        with tempfile.TemporaryDirectory() as tmp:
            huge = make(tmp, "huge.tcx", 10)
            with mock.patch.object(up, "MAX_BYTES", 5):
                send, skipped, big = up.plan([huge], set())
            self.assertEqual(([], [], ["huge.tcx"]), ([p.name for p in send], skipped, [p.name for p in big]))

    def test_the_log_only_remembers_successes_and_survives_garbage(self):
        with tempfile.TemporaryDirectory() as tmp:
            log = Path(tmp) / "log.jsonl"
            log.write_text(
                json.dumps({"file": "ok.tcx", "size": 1, "status": 200}) + "\n"
                + json.dumps({"file": "bad.tcx", "size": 2, "status": 400}) + "\nnot json\n",
                encoding="utf-8")
            self.assertEqual({("ok.tcx", 1)}, up.load_done(log))
            self.assertEqual(set(), up.load_done(Path(tmp) / "missing.jsonl"))


class DescribeTest(unittest.TestCase):
    def test_shows_what_the_server_understood(self):
        reply = {"data": {"kind": "TrailRun", "day": "2026-11-21", "distanceM": 16549, "durationS": 20898,
                          "avgHr": 148, "maxHr": 176, "ascentM": 912}}
        self.assertEqual("TrailRun  2026-11-21  16.55 km  5:48:18  HR 148/176  +912 m", up.describe(reply))

    def test_missing_fields_do_not_break_the_line(self):
        self.assertEqual("Hiking  2026-11-21  1.00 km", up.describe({"data": {"activity": "Hiking", "day": "2026-11-21", "distanceM": 1000}}))
        self.assertEqual("?  ?", up.describe({}))


class MainTest(unittest.TestCase):
    def run_main(self, tmp, replies, extra=()):
        sent = []

        def fake_upload(url, token, path):
            sent.append((url, path.name))
            return replies.pop(0)

        with mock.patch.object(up, "upload", fake_upload), mock.patch.object(up.time, "sleep"), \
                mock.patch.dict(up.os.environ, {"HEALTH_INGEST_TOKEN": "secret-token-value"}, clear=False):
            code = up.main([str(tmp), "--delay", "0", *extra])
        return code, sent

    def test_uploads_each_file_once_logs_it_and_never_logs_the_token(self):
        with tempfile.TemporaryDirectory() as tmp:
            make(tmp, "a.tcx")
            make(tmp, "b.tcx")
            code, sent = self.run_main(tmp, [(200, {"data": {"activity": "Running", "day": "2026-11-01"}}), (400, {"message": "bad"})])
            self.assertEqual(1, code)  # one failed
            self.assertEqual(["a.tcx", "b.tcx"], [name for _, name in sent])
            log = (Path(tmp) / up.LOG_NAME).read_text(encoding="utf-8")
            self.assertNotIn("secret-token-value", log)
            self.assertEqual({("a.tcx", 10)}, up.load_done(Path(tmp) / up.LOG_NAME))

    def test_rerun_sends_only_what_is_left(self):
        with tempfile.TemporaryDirectory() as tmp:
            make(tmp, "a.tcx")
            make(tmp, "b.tcx")
            self.run_main(tmp, [(200, {"data": {}}), (400, {"message": "bad"})])
            code, sent = self.run_main(tmp, [(200, {"data": {}})])
            self.assertEqual(0, code)
            self.assertEqual(["b.tcx"], [name for _, name in sent])

    def test_dry_run_sends_nothing(self):
        with tempfile.TemporaryDirectory() as tmp:
            make(tmp, "a.tcx")
            code, sent = self.run_main(tmp, [], ["--dry-run"])
            self.assertEqual((0, []), (code, sent))

    def test_a_rejected_token_stops_the_run_instead_of_failing_every_file(self):
        with tempfile.TemporaryDirectory() as tmp:
            make(tmp, "a.tcx")
            make(tmp, "b.tcx")
            code, sent = self.run_main(tmp, [(401, {"message": "no"})])
            self.assertEqual(1, code)
            self.assertEqual(["a.tcx"], [name for _, name in sent])

    def test_rate_limit_is_retried(self):
        with tempfile.TemporaryDirectory() as tmp:
            make(tmp, "a.tcx")
            code, sent = self.run_main(tmp, [(429, {}), (200, {"data": {}})])
            self.assertEqual(0, code)
            self.assertEqual(2, len(sent))

    def test_missing_token_is_refused_before_anything_is_sent(self):
        with tempfile.TemporaryDirectory() as tmp:
            make(tmp, "a.tcx")
            with mock.patch.dict(up.os.environ, {}, clear=True):
                self.assertEqual(2, up.main([tmp]))


if __name__ == "__main__":
    unittest.main()
