"""Tests for scripts/sync_ccusage_to_db.py (stdlib unittest; run: python -m unittest discover -s scripts/tests)."""

import importlib.util
import io
import os
import subprocess
import unittest
import urllib.error
from contextlib import redirect_stderr, redirect_stdout
from unittest import mock

SCRIPT = os.path.join(os.path.dirname(__file__), "..", "sync_ccusage_to_db.py")
spec = importlib.util.spec_from_file_location("sync_ccusage_to_db", SCRIPT)
sync_mod = importlib.util.module_from_spec(spec)
spec.loader.exec_module(sync_mod)


def ok_response(status=201):
    resp = mock.MagicMock()
    resp.status = status
    resp.__enter__.return_value = resp
    return resp


def http_error(code):
    return urllib.error.HTTPError("http://x/ai-usage", code, "err", {}, io.BytesIO(b"{}"))


DNS_ERROR = urllib.error.URLError(OSError(8, "nodename nor servname provided, or not known"))


def day(period, models):
    return {"period": period, "modelBreakdowns": [{"modelName": m, "inputTokens": 1, "outputTokens": 1} for m in models]}


class PostUsageRetryTest(unittest.TestCase):

    def post(self, side_effect):
        with mock.patch.object(sync_mod.urllib.request, "urlopen", side_effect=side_effect) as urlopen, \
                mock.patch.object(sync_mod.time, "sleep") as sleep, redirect_stderr(io.StringIO()):
            status = sync_mod.post_usage({"modelName": "m"}, dry_run=False)
        return status, urlopen, sleep

    def test_post_Should_RetryDnsErrorsWithBackoff_AndSucceed(self):
        status, urlopen, sleep = self.post([DNS_ERROR, DNS_ERROR, ok_response(201)])
        self.assertEqual(201, status)
        self.assertEqual(3, urlopen.call_count)
        self.assertEqual([mock.call(2.0), mock.call(4.0)], sleep.call_args_list)

    def test_post_Should_GiveUpAfterMaxAttempts(self):
        status, urlopen, _ = self.post([DNS_ERROR] * 10)
        self.assertEqual(sync_mod.NETWORK_FAILURE, status)
        self.assertEqual(sync_mod.MAX_ATTEMPTS, urlopen.call_count)

    def test_post_Should_RetryTransientServerErrors(self):
        status, urlopen, _ = self.post([http_error(503), ok_response(201)])
        self.assertEqual(201, status)
        self.assertEqual(2, urlopen.call_count)

    def test_post_Should_NotRetryClientErrors(self):
        status, urlopen, _ = self.post([http_error(401)])
        self.assertEqual(401, status)
        self.assertEqual(1, urlopen.call_count)

    def test_post_Should_TreatConflictAsDuplicateWithoutRetry(self):
        status, urlopen, _ = self.post([http_error(409)])
        self.assertEqual(409, status)
        self.assertEqual(1, urlopen.call_count)


class RunCcusageTest(unittest.TestCase):

    def test_ccusage_Should_GetAGenerousTimeout(self):
        done = subprocess.CompletedProcess([], 0, stdout='{"daily": []}', stderr="")
        with mock.patch.object(sync_mod.shutil, "which", return_value="/bin/ccusage"), \
                mock.patch.object(sync_mod.subprocess, "run", return_value=done) as run:
            sync_mod.run_ccusage()
        self.assertGreaterEqual(run.call_args.kwargs["timeout"], 120)

    def test_ccusage_Should_RaiseClearFailure_When_ItTimesOut(self):
        with mock.patch.object(sync_mod.shutil, "which", return_value="/bin/ccusage"), \
                mock.patch.object(sync_mod.subprocess, "run",
                                  side_effect=subprocess.TimeoutExpired(["ccusage"], 180)):
            with self.assertRaises(sync_mod.SyncFailure) as ctx:
                sync_mod.run_ccusage()
        self.assertIn("timed out", str(ctx.exception))


class RunLevelOutcomeTest(unittest.TestCase):

    def run_main(self, daily, post_side_effect):
        out, err = io.StringIO(), io.StringIO()
        with mock.patch.object(sync_mod, "run_ccusage", return_value=daily), \
                mock.patch.object(sync_mod, "post_usage", side_effect=post_side_effect) as post, \
                mock.patch.object(sync_mod, "notify_failure") as notify, \
                redirect_stdout(out), redirect_stderr(err):
            code = sync_mod.main([])
        return code, post, notify, out.getvalue(), err.getvalue()

    def test_main_Should_Exit0_AndTimestampSummary_When_AllSent(self):
        code, _, notify, out, _ = self.run_main([day("2026-10-01", ["claude-a", "claude-b"])], lambda p, d: 201)
        self.assertEqual(0, code)
        notify.assert_not_called()
        self.assertRegex(out, r"\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}.*完成：共 2 筆，送出 2")

    def test_main_Should_AbortEarly_And_FailTheRun_When_BackendUnreachable(self):
        daily = [day(f"2026-09-{d:02d}", ["claude-a"]) for d in range(1, 21)]
        code, post, notify, _, err = self.run_main(daily, lambda p, d: sync_mod.NETWORK_FAILURE)
        self.assertNotEqual(0, code)
        self.assertEqual(sync_mod.MAX_CONSECUTIVE_NETWORK_FAILURES, post.call_count)
        self.assertIn("RUN FAILED", err)
        notify.assert_called_once()

    def test_main_Should_FailTheRun_When_SomeRecordsFail(self):
        statuses = iter([201, 401, 201])
        code, _, notify, _, err = self.run_main([day("2026-10-01", ["a", "b", "c"])], lambda p, d: next(statuses))
        self.assertNotEqual(0, code)
        self.assertIn("RUN FAILED", err)
        self.assertIn("1/3", err)
        notify.assert_called_once()

    def test_main_Should_FailTheRun_When_CcusageFails(self):
        err = io.StringIO()
        with mock.patch.object(sync_mod, "run_ccusage", side_effect=sync_mod.SyncFailure("ccusage timed out after 180s")), \
                mock.patch.object(sync_mod, "notify_failure") as notify, \
                redirect_stdout(io.StringIO()), redirect_stderr(err):
            code = sync_mod.main([])
        self.assertNotEqual(0, code)
        self.assertIn("RUN FAILED", err.getvalue())
        self.assertIn("timed out", err.getvalue())
        notify.assert_called_once()


if __name__ == "__main__":
    unittest.main()
