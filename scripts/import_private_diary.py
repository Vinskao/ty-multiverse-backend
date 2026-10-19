"""Validate local diary exports, then import over SSH without logging private text or credentials."""
import argparse
import base64
import datetime
import hashlib
import inspect
import json
import pathlib
import subprocess
import uuid

CATEGORIES = {'general', 'relationship', 'investment', 'wellbeing'}
SOURCE_FILES = ('diary-events.json', 'relationship-events.json', 'investment-events.json',
                'relationship-timeline.md', 'investment-timeline.md', 'diary-schema.md')


def sql_literal(value):
    return 'NULL' if value is None else "'" + str(value).replace("'", "''") + "'"


def prepare_rows(directory):
    directory = pathlib.Path(directory)
    document = json.loads((directory / 'diary-events.json').read_text(encoding='utf-8-sig'))
    captured = datetime.date.fromisoformat(document['created_at']).isoformat()
    relationship = directory / 'relationship-events.json'
    relationship_unknown = json.loads(relationship.read_text(encoding='utf-8-sig')).get('undated_or_uncertain', []) if relationship.exists() else []
    rows, identifiers = [], set()

    def add(key, date, category, kind, precision, title, markdown, metadata):
        if category not in CATEGORIES or key in identifiers or len(key) > 128:
            raise ValueError('Invalid category or duplicate event ID')
        if date is not None:
            date = datetime.date.fromisoformat(date).isoformat()
        if not title or len(title) > 200 or not markdown.strip() or len(markdown) > 200000:
            raise ValueError('Invalid diary content length')
        identifiers.add(key)
        rows.append({'id': str(uuid.uuid5(uuid.NAMESPACE_URL, 'ty-diary:' + key)), 'source_event_id': key,
                     'entry_date': date, 'category': category, 'entry_kind': kind, 'date_precision': precision,
                     'title': title, 'markdown': markdown, 'metadata_json': json.dumps(metadata, ensure_ascii=False),
                     'content_hash': hashlib.sha256(markdown.encode()).hexdigest()})

    for event in document['events']:
        category = event['category']
        precision = event.get('date_precision', 'unknown')
        if precision not in {'day', 'approximate', 'unknown', 'range', 'month', 'year'}:
            raise ValueError('Invalid date precision')
        # Keep the supplied date and precision together; never turn an approximate date into an exact one.
        body = '# ' + event['title'] + '\n\n' + event['description'] + '\n\n'
        body += 'Date precision: ' + precision + '\n\n```json\n' + json.dumps(event, ensure_ascii=False, indent=2) + '\n```\n'
        add('event:' + event['id'], event.get('date'), category, 'event', precision, event['title'], body, event)
    for event in document.get('undated_or_uncertain', []):
        key = 'undated:' + hashlib.sha256(json.dumps(event, ensure_ascii=False, sort_keys=True).encode()).hexdigest()
        category = 'relationship' if event in relationship_unknown else 'general'
        add(key, None, category, 'event', event.get('date_precision', 'unknown'),
            '日期待確認 / ' + event.get('period', 'unknown'), event['description'], event)
    for filename in SOURCE_FILES:
        path = directory / filename
        if not path.exists():
            continue
        raw = path.read_text(encoding='utf-8-sig')
        if path.suffix == '.json':
            json.loads(raw)
        category = 'relationship' if filename.startswith('relationship') else 'investment' if filename.startswith('investment') else 'general'
        body = '```json\n' + raw + '\n```\n' if path.suffix == '.json' else raw
        add('document:' + filename, captured, category, 'document', 'document', filename, body,
            {'source_file': filename, 'sha256': hashlib.sha256(path.read_bytes()).hexdigest(),
             'date_semantics': 'document capture date, not an event occurrence date'})
    return rows


def import_sql(rows, owner):
    columns = ('id', 'owner_subject', 'entry_date', 'category', 'entry_kind', 'date_precision',
               'source_event_id', 'title', 'markdown', 'metadata_json', 'content_hash')
    statements = []
    for row in rows:
        values = {**row, 'id': str(uuid.uuid5(uuid.NAMESPACE_URL, owner + ':' + row['id'])), 'owner_subject': owner}
        statements.append('INSERT INTO diary_entries (' + ','.join(columns) + ',created_at) VALUES (' +
                          ','.join(sql_literal(values[c]) for c in columns) + ',CURRENT_TIMESTAMP) ON CONFLICT DO NOTHING;')
    return '\n'.join(statements)


def execute_import(rows, migration, username, host):
    # Remote Python 3.6 compatible. Secret values are used only in memory and are never printed.
    remote = r'''import subprocess,json,base64,urllib.parse,uuid
def k(*args): return subprocess.check_output(['kubectl']+list(args),universal_newlines=True)
deployment=json.loads(k('get','deployment','ty-multiverse-backend','-o','json'))
envs=deployment['spec']['template']['spec']['containers'][0].get('env',[])
def env(name):
    item=next(x for x in envs if x['name']==name)
    if 'value' in item: return item['value']
    ref=item['valueFrom']['secretKeyRef']
    secret=json.loads(k('get','secret',ref['name'],'-o','json'))
    return base64.b64decode(secret['data'][ref['key']]).decode()
url=urllib.parse.urlparse(env('SPRING_DATASOURCE_URL').replace('jdbc:','',1))
assert url.hostname=='postgres' and url.path=='/peoplesystem', 'Unexpected database target'
assert urllib.parse.parse_qs(url.query).get('currentSchema',['public'])==['public'], 'Unexpected schema'
role=env('SPRING_DATASOURCE_USERNAME')
def psql(database,sql,transaction=False):
    command=['kubectl','exec','-i','postgres-0','--','psql','-U',role,'-d',database,'-v','ON_ERROR_STOP=1','-At']
    if transaction: command+=['--single-transaction']
    result=subprocess.run(command+['-f','-'],input=sql.encode(),stdout=subprocess.PIPE,stderr=subprocess.PIPE)
    if result.returncode: raise RuntimeError('Database operation failed; private SQL and credentials were not logged')
    return result.stdout.decode().strip()
def literal(value): return 'NULL' if value is None else "'"+str(value).replace("'","''")+"'"
username=base64.b64decode('__USERNAME__').decode()
subjects=set()
for database in ['peoplesystem','postgres']:
    schemas=psql(database,"SELECT table_schema FROM information_schema.tables WHERE table_name='user_entity'").splitlines()
    for schema in schemas:
        identifier='"'+schema.replace('"','""')+'"'
        subjects.update(psql(database,'SELECT id FROM '+identifier+'.user_entity WHERE username='+literal(username)).splitlines())
assert len(subjects)==1, 'Owner identity must resolve uniquely before any write'
owner=next(iter(subjects))
migration=base64.b64decode('__MIGRATION__').decode()
columns_ready=psql('peoplesystem',"SELECT COUNT(*) FROM information_schema.columns WHERE table_schema='public' AND table_name='diary_entries' AND column_name IN ('category','entry_kind','date_precision','source_event_id','metadata_json')")
if columns_ready!='5': psql('peoplesystem',migration,True)
rows=json.loads(base64.b64decode('__ROWS__').decode())
sql_literal=literal
__IMPORT_FUNCTION__
psql('peoplesystem',import_sql(rows,owner),True)
print(psql('peoplesystem','SELECT category,entry_kind,COUNT(*) FROM diary_entries WHERE owner_subject='+literal(owner)+' GROUP BY category,entry_kind ORDER BY category,entry_kind'))
print('owner_verified=true')
'''
    payload = remote.replace('__USERNAME__', base64.b64encode(username.encode()).decode())
    payload = payload.replace('__IMPORT_FUNCTION__', inspect.getsource(import_sql))
    payload = payload.replace('__MIGRATION__', base64.b64encode(migration.encode()).decode())
    payload = payload.replace('__ROWS__', base64.b64encode(json.dumps(rows, ensure_ascii=False).encode()).decode())
    result = subprocess.run(['ssh', host, 'python3 -'], input=payload.encode(), stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    if result.returncode:
        raise RuntimeError('Private diary import failed; no private payload was logged. Check database connectivity and unique owner identity.')
    print(result.stdout.decode())


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--source-dir', type=pathlib.Path, required=True)
    parser.add_argument('--owner-username', required=True)
    parser.add_argument('--ssh-host', default='oke-node')
    parser.add_argument('--apply', action='store_true')
    args = parser.parse_args()
    rows = prepare_rows(args.source_dir)
    print(json.dumps({'validated_rows': len(rows), 'dated_events': sum(r['entry_kind']=='event' and r['entry_date'] is not None for r in rows),
                      'undated_events': sum(r['entry_date'] is None for r in rows), 'source_documents': sum(r['entry_kind']=='document' for r in rows)}))
    if args.apply:
        execute_import(rows, (pathlib.Path(__file__).parents[1] / 'db/private_diary_categories.sql').read_text(encoding='utf-8'), args.owner_username, args.ssh_host)


if __name__ == '__main__': main()
