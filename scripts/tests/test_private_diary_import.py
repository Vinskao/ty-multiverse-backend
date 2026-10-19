import importlib.util
import json
import pathlib
import tempfile
import unittest

SPEC = importlib.util.spec_from_file_location('private_diary_import', pathlib.Path(__file__).parents[1] / 'import_private_diary.py')
module = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(module)


class PrivateDiaryImportTest(unittest.TestCase):
    def sample(self, directory, events=None):
        payload = {'created_at': '2026-10-19', 'events': events or [
            {'id': 'INV-1', 'date': '2026-10-18', 'date_precision': 'approximate', 'category': 'investment',
             'title': 'Test investment', 'description': 'Synthetic fixture', 'metrics': {'value': 0}, 'investment': {'thesis': 'Test'}}],
            'undated_or_uncertain': [{'period': 'unknown', 'description': 'Undated fixture', 'date_precision': 'unknown'}]}
        (directory / 'diary-events.json').write_text(json.dumps(payload), encoding='utf-8')
        return payload

    def test_retains_zero_metrics_approximate_dates_and_undated_records(self):
        with tempfile.TemporaryDirectory() as temporary:
            directory = pathlib.Path(temporary)
            self.sample(directory)
            rows = module.prepare_rows(directory)
            event = next(r for r in rows if r['source_event_id'] == 'event:INV-1')
            self.assertEqual('investment', event['category'])
            self.assertEqual('approximate', event['date_precision'])
            self.assertEqual(0, json.loads(event['metadata_json'])['metrics']['value'])
            self.assertIsNone(next(r for r in rows if r['source_event_id'].startswith('undated:'))['entry_date'])
            self.assertEqual(len(rows), len(module.prepare_rows(directory)))
            self.assertEqual([r['id'] for r in rows], [r['id'] for r in module.prepare_rows(directory)])

    def test_rejects_duplicate_ids_invalid_categories_and_impossible_dates(self):
        with tempfile.TemporaryDirectory() as temporary:
            directory = pathlib.Path(temporary)
            payload = self.sample(directory)
            event = payload['events'][0]
            for events in ([event, event], [{**event, 'category': 'public'}], [{**event, 'date': '2026-02-30'}]):
                self.sample(directory, events)
                with self.assertRaises(ValueError): module.prepare_rows(directory)

    def test_sql_escapes_quotes_and_preserves_unicode_without_owner_interpolation(self):
        value = "A 'quote' $tag$"
        self.assertEqual("'A ''quote'' $tag$'", module.sql_literal(value))
        self.assertEqual('NULL', module.sql_literal(None))
        with tempfile.TemporaryDirectory() as temporary:
            directory = pathlib.Path(temporary)
            self.sample(directory)
            sql = module.import_sql(module.prepare_rows(directory), 'owner-subject')
            self.assertIn('ON CONFLICT DO NOTHING', sql)
            self.assertIn('owner_subject', sql)


if __name__ == '__main__': unittest.main()
