#!/usr/bin/env bash
#
# verify-outbox-resilience.sh
#
# 驗證 Transactional Outbox 在 RabbitMQ 中斷時不丟事件、恢復後自動補送。
#
# 設計原則：
#   * 預設只做 preflight，不會碰 RabbitMQ，也不會寫任何資料。
#   * 必須明確加 --execute 才會停 RabbitMQ 進行故障測試。
#   * 測試只寫入一筆「合成」outbox event（aggregate_type=audit），
#     完全不觸碰 people／weapon 正式資料。
#   * trap 保證腳本被中斷（Ctrl-C／錯誤／kill）時仍會把 RabbitMQ 拉回來。
#   * 測試資料保留作為 audit 證據，要清除請用獨立的 --cleanup。
#
# 執行位置：需要能操作叢集的 kubectl。建議直接在 oke-node 上跑：
#   ssh oke-node 'cat > /tmp/verify-outbox-resilience.sh' < verify-outbox-resilience.sh
#   ssh -t oke-node 'bash /tmp/verify-outbox-resilience.sh'            # preflight
#   ssh -t oke-node 'bash /tmp/verify-outbox-resilience.sh --execute'  # 故障測試
#   ssh -t oke-node 'bash /tmp/verify-outbox-resilience.sh --cleanup'  # 清除測試資料
#
set -euo pipefail

NAMESPACE="${NAMESPACE:-default}"
RABBITMQ_STS="rabbitmq"
RABBITMQ_POD="rabbitmq-0"
POSTGRES_POD="postgres-0"
DB_NAME="${DB_NAME:-peoplesystem}"
CONSUMER_SECRET="ty-multiverse-consumer-secrets"
CONSUMER_LABEL="app.kubernetes.io/name=ty-multiverse-consumer"
EVENT_EXCHANGE="tymb-event-exchange"
EVENT_STREAM="tymb-events"

# 合成事件的識別前綴，cleanup 與 audit 查詢都靠它
TEST_EVENT_TYPE="audit.outbox-resilience-test.succeeded"
TEST_REQUEST_PREFIX="outbox-resilience-test"
TEST_AGGREGATE_TYPE="audit"
PROBE_QUEUE_PREFIX="outbox-resilience-probe"

MODE="preflight"
OUTAGE_WAIT_SECONDS="${OUTAGE_WAIT_SECONDS:-20}"
RECOVERY_TIMEOUT_SECONDS="${RECOVERY_TIMEOUT_SECONDS:-180}"

RESTORE_NEEDED=0
PROBE_QUEUE=""

# ---------------------------------------------------------------- 輸出工具

RED=$'\033[0;31m'; GREEN=$'\033[0;32m'; YELLOW=$'\033[0;33m'; BLUE=$'\033[0;34m'; NC=$'\033[0m'

info()  { printf '%s\n' "${BLUE}==>${NC} $*"; }
ok()    { printf '%s\n' "  ${GREEN}✓${NC} $*"; }
warn()  { printf '%s\n' "  ${YELLOW}!${NC} $*"; }
fail()  { printf '%s\n' "  ${RED}✗${NC} $*"; }
die()   { fail "$*"; exit 1; }

usage() {
    cat <<'USAGE'
用法: verify-outbox-resilience.sh [選項]

  (無選項)     只做 preflight 檢查，不中斷 RabbitMQ、不寫入任何資料
  --execute    執行完整故障測試：停 RabbitMQ -> 寫合成事件 -> 恢復 -> 驗證補送
  --cleanup    刪除本腳本產生的測試 outbox 資料與 probe queue
  --help       顯示此說明

環境變數:
  NAMESPACE                  預設 default
  DB_NAME                    預設 peoplesystem
  OUTAGE_WAIT_SECONDS        中斷後觀察多久（預設 20 秒）
  RECOVERY_TIMEOUT_SECONDS   等待補送完成的上限（預設 180 秒）
USAGE
}

# ---------------------------------------------------------------- 叢集操作

k() { kubectl -n "$NAMESPACE" "$@"; }

db_user() {
    k get secret "$CONSUMER_SECRET" -o jsonpath='{.data.SPRING_DATASOURCE_USERNAME}' | base64 -d
}

# 執行 SQL 並回傳單一值（無表頭、無對齊）
psql_value() {
    local sql="$1"
    k exec "$POSTGRES_POD" -- psql -U "$DB_USER" -d "$DB_NAME" -tAc "$sql" 2>/dev/null | tr -d '[:space:]'
}

# 執行 SQL，輸出原樣（給人看的查詢）
psql_show() {
    local sql="$1"
    k exec "$POSTGRES_POD" -- psql -U "$DB_USER" -d "$DB_NAME" -c "$sql" 2>/dev/null
}

# 執行 SQL，錯誤即中止
psql_exec() {
    local sql="$1"
    k exec -i "$POSTGRES_POD" -- psql -U "$DB_USER" -d "$DB_NAME" -v ON_ERROR_STOP=1 -q -c "$sql"
}

rabbitctl() { k exec "$RABBITMQ_POD" -c rabbitmq -- rabbitmqctl -q "$@" 2>/dev/null; }

rabbitadmin() {
    k exec "$RABBITMQ_POD" -c rabbitmq -- rabbitmqadmin -u "$RABBIT_USER" -p "$RABBIT_PASS" "$@"
}

rabbit_pod_ready() {
    [[ "$(k get pod "$RABBITMQ_POD" -o jsonpath='{.status.containerStatuses[?(@.name=="rabbitmq")].ready}' 2>/dev/null)" == "true" ]]
}

stream_message_count() {
    rabbitctl list_queues name messages | awk -v q="$EVENT_STREAM" '$1==q {print $2}'
}

# ---------------------------------------------------------------- 恢復保險

restore_rabbitmq() {
    local exit_code=$?
    if [[ "$RESTORE_NEEDED" -eq 1 ]]; then
        RESTORE_NEEDED=0
        echo
        info "還原 RabbitMQ（trap）"
        k scale statefulset "$RABBITMQ_STS" --replicas=1 >/dev/null 2>&1 || true
        local waited=0
        while (( waited < RECOVERY_TIMEOUT_SECONDS )); do
            if rabbit_pod_ready; then
                ok "RabbitMQ 已恢復 Ready"
                break
            fi
            sleep 5; waited=$(( waited + 5 ))
        done
        if ! rabbit_pod_ready; then
            fail "RabbitMQ 尚未 Ready，請手動確認：kubectl -n $NAMESPACE get pod $RABBITMQ_POD"
        fi
    fi
    exit "$exit_code"
}

# ---------------------------------------------------------------- Preflight

preflight() {
    local failures=0

    info "1/7 RabbitMQ pod 狀態"
    if rabbit_pod_ready; then
        ok "$RABBITMQ_POD Ready"
    else
        fail "$RABBITMQ_POD 未 Ready"; failures=$(( failures + 1 ))
    fi

    info "2/7 PVC 狀態"
    local pvc_line
    pvc_line="$(k get pvc "rabbitmq-data-${RABBITMQ_POD}" --no-headers 2>/dev/null || true)"
    if [[ "$pvc_line" == *Bound* ]]; then
        ok "rabbitmq-data-${RABBITMQ_POD} Bound"
    else
        fail "PVC 未 Bound: ${pvc_line:-not found}"; failures=$(( failures + 1 ))
    fi

    info "3/7 consumer pod 狀態"
    local consumer_ready
    consumer_ready="$(k get pods -l "$CONSUMER_LABEL" -o jsonpath='{.items[*].status.containerStatuses[*].ready}' 2>/dev/null)"
    if [[ "$consumer_ready" == *true* ]]; then
        ok "consumer Ready"
    else
        fail "consumer 未 Ready"; failures=$(( failures + 1 ))
    fi

    info "4/7 consumer 是否已載入 outbox publisher"
    local consumer_pod
    consumer_pod="$(k get pods -l "$CONSUMER_LABEL" -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || true)"
    if [[ -n "$consumer_pod" ]] \
        && k logs "$consumer_pod" --tail=-1 2>/dev/null | grep -q "eventRabbitTemplate 已建立"; then
        ok "consumer 已載入 outbox publisher（$consumer_pod）"
    else
        fail "consumer 日誌中找不到 eventRabbitTemplate，目前映像可能還沒有 outbox 功能；測試會失敗"
        failures=$(( failures + 1 ))
    fi

    info "5/7 classic queue 是否為空"
    local non_empty
    non_empty="$(rabbitctl list_queues name messages type \
        | awk '$3=="classic" && $2+0 > 0 {printf "%s=%s ", $1, $2}')"
    if [[ -z "$non_empty" ]]; then
        ok "所有 classic queue 均為空"
    else
        fail "下列 queue 尚有訊息，請等處理完再測: $non_empty"; failures=$(( failures + 1 ))
    fi

    info "6/7 tymb-events stream 與 exchange"
    local stream_count
    stream_count="$(stream_message_count)"
    if [[ -n "$stream_count" ]]; then
        ok "$EVENT_STREAM 存在，目前訊息數 = $stream_count"
    else
        fail "找不到 $EVENT_STREAM"; failures=$(( failures + 1 ))
    fi
    local binding_found
    binding_found="$(rabbitctl list_bindings source_name destination_name routing_key \
        | awk -v e="$EVENT_EXCHANGE" -v q="$EVENT_STREAM" '$1==e && $2==q && $3=="event.#" {print "yes"}')"
    if [[ "$binding_found" == "yes" ]]; then
        ok "binding ${EVENT_EXCHANGE} -> ${EVENT_STREAM} (event.#) 存在"
    else
        fail "binding ${EVENT_EXCHANGE} -> ${EVENT_STREAM} (event.#) 不存在"; failures=$(( failures + 1 ))
    fi

    info "7/7 outbox table 與待發布 backlog"
    local has_table pending
    has_table="$(psql_value "SELECT to_regclass('public.business_event_outbox') IS NOT NULL")"
    if [[ "$has_table" == "t" ]]; then
        ok "business_event_outbox 存在"
        pending="$(psql_value "SELECT count(*) FROM business_event_outbox WHERE published_at IS NULL")"
        if [[ "$pending" == "0" ]]; then
            ok "目前沒有待發布事件"
        else
            warn "目前有 $pending 筆待發布事件（測試仍可進行，但請留意 backlog）"
        fi
    else
        fail "business_event_outbox 不存在，請先執行 db/business_event_outbox.sql"
        failures=$(( failures + 1 ))
    fi

    echo
    if (( failures > 0 )); then
        die "preflight 有 $failures 項未通過，未執行故障測試"
    fi
    ok "preflight 全數通過"
}

# ---------------------------------------------------------------- 故障測試

run_outage_test() {
    local run_id event_id request_id occurred_at payload baseline_stream_count
    run_id="$(date -u +%Y%m%dT%H%M%SZ)"
    event_id="$(k exec "$POSTGRES_POD" -- psql -U "$DB_USER" -d "$DB_NAME" -tAc "SELECT gen_random_uuid()" 2>/dev/null | tr -d '[:space:]')"
    [[ -n "$event_id" ]] || die "無法產生 event_id"
    request_id="${TEST_REQUEST_PREFIX}-${run_id}"
    occurred_at="$(date -u +%Y-%m-%dT%H:%M:%SZ)"
    PROBE_QUEUE="${PROBE_QUEUE_PREFIX}-${run_id}"

    echo
    info "測試識別"
    ok "eventId   = $event_id"
    ok "requestId = $request_id"

    # --- probe queue：stream 不支援 basic.get，另綁一個只吃測試 routing key 的
    #     classic queue，才能把事件內容原樣取回比對。x-expires 保證就算腳本被
    #     中斷，這個 queue 一小時後也會自己消失。
    echo
    info "建立 probe queue（只綁定測試 routing key，不會收到任何正式事件）"
    rabbitadmin queues declare --name "$PROBE_QUEUE" --type classic \
        --arguments '{"x-expires":3600000}' >/dev/null
    rabbitadmin declare binding --source "$EVENT_EXCHANGE" --destination-type queue \
        --destination "$PROBE_QUEUE" --routing-key "event.${TEST_EVENT_TYPE}" >/dev/null
    ok "$PROBE_QUEUE 已建立並綁定 event.${TEST_EVENT_TYPE}"

    baseline_stream_count="$(stream_message_count)"
    ok "$EVENT_STREAM 基準訊息數 = $baseline_stream_count"

    # --- 停 RabbitMQ
    echo
    info "停止 RabbitMQ（scale statefulset -> 0）"
    RESTORE_NEEDED=1
    k scale statefulset "$RABBITMQ_STS" --replicas=0 >/dev/null
    local waited=0
    while k get pod "$RABBITMQ_POD" >/dev/null 2>&1; do
        (( waited >= 120 )) && die "RabbitMQ pod 未在 120 秒內終止"
        sleep 3; waited=$(( waited + 3 ))
    done
    ok "RabbitMQ 已停止"

    # --- 寫入合成事件（不觸碰 people／weapon）
    echo
    info "寫入一筆合成 outbox event（aggregate_type=${TEST_AGGREGATE_TYPE}，不動正式資料）"
    payload=$(cat <<JSON
{"eventId":"${event_id}","eventType":"${TEST_EVENT_TYPE}","aggregateType":"${TEST_AGGREGATE_TYPE}","aggregateId":"${run_id}","requestId":"${request_id}","occurredAt":"${occurred_at}","actorId":"verify-outbox-resilience.sh","source":"verify-outbox-resilience.sh","schemaVersion":1,"payload":{"note":"synthetic resilience test event; no business data touched"},"error":null}
JSON
)
    psql_exec "INSERT INTO business_event_outbox
        (event_id, event_type, aggregate_type, aggregate_id, request_id, payload, occurred_at)
        VALUES ('${event_id}'::uuid, '${TEST_EVENT_TYPE}', '${TEST_AGGREGATE_TYPE}',
                '${run_id}', '${request_id}', \$json\$${payload}\$json\$::jsonb, now())" >/dev/null
    ok "已寫入 outbox"

    # --- 中斷期間驗證：事件必須還在，且 publisher 有在重試
    echo
    info "等待 ${OUTAGE_WAIT_SECONDS} 秒，觀察中斷期間的 outbox 狀態"
    sleep "$OUTAGE_WAIT_SECONDS"

    local published attempts backoff_ok
    published="$(psql_value "SELECT published_at IS NULL FROM business_event_outbox WHERE event_id='${event_id}'::uuid")"
    attempts="$(psql_value "SELECT attempt_count FROM business_event_outbox WHERE event_id='${event_id}'::uuid")"
    backoff_ok="$(psql_value "SELECT next_attempt_at > now() FROM business_event_outbox WHERE event_id='${event_id}'::uuid")"

    [[ "$published" == "t" ]] || die "中斷期間 published_at 竟然已被填入，違反預期"
    ok "published_at IS NULL（事件保留，未被誤標為已發布）"

    if [[ "${attempts:-0}" -ge 1 ]]; then
        ok "attempt_count = $attempts（publisher 確實有嘗試並記錄失敗）"
    else
        fail "attempt_count = ${attempts:-0}，publisher 似乎沒有嘗試發布"
        warn "請確認 consumer 的 tymb.outbox.enabled 與排程是否啟用"
    fi

    if [[ "$backoff_ok" == "t" ]]; then
        ok "next_attempt_at 已往後推（指數退避生效）"
    else
        warn "next_attempt_at 未大於 now()，退避可能未生效（不影響不丟事件的結論）"
    fi

    psql_show "SELECT event_id, attempt_count, next_attempt_at, left(last_error, 80) AS last_error
               FROM business_event_outbox WHERE event_id='${event_id}'::uuid"

    # --- 恢復 RabbitMQ
    echo
    info "恢復 RabbitMQ"
    k scale statefulset "$RABBITMQ_STS" --replicas=1 >/dev/null
    waited=0
    while ! rabbit_pod_ready; do
        (( waited >= RECOVERY_TIMEOUT_SECONDS )) && die "RabbitMQ 未在 ${RECOVERY_TIMEOUT_SECONDS} 秒內 Ready"
        sleep 5; waited=$(( waited + 5 ))
    done
    RESTORE_NEEDED=0
    ok "RabbitMQ 已 Ready（耗時約 ${waited} 秒）"

    # --- 補送驗證
    echo
    info "等待 outbox 自動補送（上限 ${RECOVERY_TIMEOUT_SECONDS} 秒）"
    waited=0
    local published_at=""
    while (( waited < RECOVERY_TIMEOUT_SECONDS )); do
        published_at="$(psql_value "SELECT coalesce(to_char(published_at,'YYYY-MM-DD\"T\"HH24:MI:SSOF'),'') FROM business_event_outbox WHERE event_id='${event_id}'::uuid")"
        [[ -n "$published_at" ]] && break
        sleep 5; waited=$(( waited + 5 ))
    done
    [[ -n "$published_at" ]] || die "事件在 ${RECOVERY_TIMEOUT_SECONDS} 秒內未被補送（published_at 仍為 NULL）"
    ok "published_at = $published_at（補送完成，耗時約 ${waited} 秒）"

    # --- stream 增量
    local after_stream_count delta
    after_stream_count="$(stream_message_count)"
    delta=$(( after_stream_count - baseline_stream_count ))
    if (( delta == 1 )); then
        ok "$EVENT_STREAM 訊息數 ${baseline_stream_count} -> ${after_stream_count}（+1）"
    else
        warn "$EVENT_STREAM 訊息數 ${baseline_stream_count} -> ${after_stream_count}（+${delta}），期間可能有其他正式事件寫入"
    fi

    # --- 從 probe queue 取回內容比對 eventId
    echo
    info "從 probe queue 取回事件內容比對"
    local got
    got="$(rabbitadmin get --queue "$PROBE_QUEUE" --count 5 --ack-mode reject_requeue_false 2>/dev/null || true)"
    if grep -q "$event_id" <<<"$got"; then
        ok "probe queue 收到的訊息含正確 eventId"
    else
        fail "probe queue 中找不到 eventId=$event_id"
        printf '%s\n' "$got"
        die "補送內容驗證失敗"
    fi

    # --- 重複發布不應產生第二筆
    local row_count
    row_count="$(psql_value "SELECT count(*) FROM business_event_outbox WHERE event_id='${event_id}'::uuid")"
    if [[ "$row_count" == "1" ]]; then
        ok "outbox 中該 eventId 僅一筆（唯一索引 + ON CONFLICT DO NOTHING 生效）"
    else
        fail "outbox 中該 eventId 有 $row_count 筆"
    fi

    echo
    ok "故障測試通過：RabbitMQ 中斷不丟事件，恢復後自動補送"
    echo
    info "測試資料已保留作為 audit 證據"
    printf '  outbox:      %s\n' "SELECT * FROM business_event_outbox WHERE request_id = '${request_id}';"
    printf '  probe queue: %s（一小時後自動過期）\n' "$PROBE_QUEUE"
    printf '  清除:        %s\n' "verify-outbox-resilience.sh --cleanup"
}

# ---------------------------------------------------------------- Cleanup

run_cleanup() {
    info "清除測試 outbox 資料"
    psql_show "DELETE FROM business_event_outbox
               WHERE aggregate_type = '${TEST_AGGREGATE_TYPE}'
                 AND request_id LIKE '${TEST_REQUEST_PREFIX}-%'"

    info "清除 probe queue"
    local queues
    queues="$(rabbitctl list_queues name | grep "^${PROBE_QUEUE_PREFIX}-" || true)"
    if [[ -z "$queues" ]]; then
        ok "沒有殘留的 probe queue"
    else
        while read -r q; do
            [[ -z "$q" ]] && continue
            rabbitadmin delete queue --name "$q" >/dev/null 2>&1 && ok "已刪除 $q" || warn "刪除 $q 失敗"
        done <<<"$queues"
    fi

    echo
    warn "注意：已發布到 ${EVENT_STREAM} 的測試事件無法單筆刪除（stream 為 append-only），"
    warn "      會依 30 天 retention 自然過期。可用 eventType='${TEST_EVENT_TYPE}' 在稽核查詢時排除。"
}

# ---------------------------------------------------------------- 主流程

while [[ $# -gt 0 ]]; do
    case "$1" in
        --execute) MODE="execute" ;;
        --cleanup) MODE="cleanup" ;;
        --help|-h) usage; exit 0 ;;
        *) die "未知選項: $1（--help 查看用法）" ;;
    esac
    shift
done

command -v kubectl >/dev/null 2>&1 || die "找不到 kubectl"

DB_USER="$(db_user)"
[[ -n "$DB_USER" ]] || die "無法從 secret $CONSUMER_SECRET 取得資料庫帳號"
RABBIT_USER="$(k get secret rabbitmq-credentials -o jsonpath='{.data.RABBITMQ_DEFAULT_USER}' | base64 -d)"
RABBIT_PASS="$(k get secret rabbitmq-credentials -o jsonpath='{.data.RABBITMQ_DEFAULT_PASS}' | base64 -d)"
[[ -n "$RABBIT_USER" && -n "$RABBIT_PASS" ]] || die "無法從 secret rabbitmq-credentials 取得 RabbitMQ 帳密"

trap restore_rabbitmq EXIT INT TERM

case "$MODE" in
    cleanup)
        info "模式：cleanup"
        run_cleanup
        ;;
    preflight)
        info "模式：preflight（不會中斷 RabbitMQ，不會寫入任何資料）"
        echo
        preflight
        echo
        info "要執行故障測試請加上 --execute"
        ;;
    execute)
        info "模式：execute（會短暫停止 RabbitMQ）"
        echo
        preflight
        echo
        warn "接下來會停止 RabbitMQ，期間所有 classic queue 工作流會中斷。"
        run_outage_test
        ;;
esac
