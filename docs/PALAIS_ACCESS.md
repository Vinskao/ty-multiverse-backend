# Palais access and review

An authenticated member can request Palais access and the ability to review applications. In single-review mode only the configured initial Checker can decide requests. Two-review mode requires two distinct active Checkers. The quorum is recorded on submission, so enabling dual review affects newly submitted applications.

```mermaid
flowchart LR
    M["Account settings"] --> G["Gateway JWT and routing"]
    G --> B["Backend application and live review rules"]
    B --> P["PostgreSQL applications and audit"]
    B --> K["Keycloak role verification and provisioning"]
```

```mermaid
stateDiagram-v2
    [*] --> PENDING
    PENDING --> PENDING: first vote in dual review
    PENDING --> WITHDRAWN
    PENDING --> REJECTED
    PENDING --> SYNCING: quorum confirmed
    SYNCING --> SYNCING: retry with backoff
    SYNCING --> ACTIVE: role read back
    SYNCING --> FAILED: five attempts exhausted
    FAILED --> SYNCING: administrator retries
    ACTIVE --> REVOKED: administrator removes role
```

## 開發方法

1. Run the review-policy tests and Backend controller tests with `./mvnw test`. The controller tests use a mocked Keycloak client. Exercise the SQL migration and worker in a staging PostgreSQL instance before production activation.
2. Apply `db/palais_access.sql` using the existing database migration identity. Grant the runtime identity the request-table and sequence privileges, plus only SELECT and INSERT on the audit table. Keep the feature disabled until the migration is verified.
3. In an isolated realm, create the direct realm role `palais-checker`, configure the API audience claim on the login client, and create a Backend service account for role lookup and mapping. Verify assignment, read-back and removal, including rejected attempts to map other roles. Use the existing PeopleSystem SSO in production and pin the exact issuer and audience observed in signed access tokens. Assign the role directly to the verified Chiaki account for bootstrap.
4. Configure the Backend with `PALAIS_ACCESS_INITIAL_CHECKER_ID`, `PALAIS_ACCESS_ISSUER`, `PALAIS_ACCESS_AUDIENCE` and `PALAIS_ACCESS_CLIENT_ID`. Supply `PALAIS_ACCESS_CLIENT_SECRET` from Jenkins into the optional Kubernetes Secret `palais-access-credentials` in the Backend namespace. Put non-secret settings in `palais-access-config`. Keep the value out of Git and test fixtures. The worker logs failed provisioning by request ID and records the event in the administrator audit view.
5. Deploy API routing and client changes. Enable with `PALAIS_ACCESS_ENABLED=true` after configuration and migration verification. Test submission, approval, opening Palais, manager-only actions, and revocation using a pre-revocation token. Verify the Palais workflow before publishing its BPMN diagram.


The initial Checker ID is the realm user UUID carried in the JWT subject. If that account loses the direct role, a realm administrator must restore it after reviewing the audit history. Before enabling the feature, verify that the role service account cannot map unrelated roles. Maya uses the internal Backend verifier for sensitive reads made with the new role, so a revoked Checker gets denied even with an older signed token. Set PALAIS_ACCESS_VERIFICATION_URL in Maya for deployments with a different Backend service address.
## 正式環境開通步驟（2026-11-22）

功能預設關閉（`PALAIS_ACCESS_ENABLED=false`）時控制器不存在，`/palais-access/me` 回 404，帳號設定頁顯示「尚未開放」，Maker 的申請表單不會出現。依下列順序開通：

1. **資料庫**：先在 staging 驗證，再用既有遷移帳號執行 `db/palais_access.sql`。runtime 帳號對申請表與序號完整授權，對審計表只給 SELECT 與 INSERT。
2. **Keycloak（PeopleSystem realm）**：建立直接 realm 角色 `palais-checker`；登入 client 加 API audience claim；建後端服務帳號（client credentials），只授權查使用者與讀寫 realm 角色對應。驗證指派、讀回、移除，並確認它不能對應其他角色。解碼實際 access token 記下 `iss` 與 `aud`。把角色直接指派給 Chiaki 帳號。
3. **設定值**：`PALAIS_ACCESS_INITIAL_CHECKER_ID`（Chiaki 的 JWT `sub`）、`PALAIS_ACCESS_ISSUER`、`PALAIS_ACCESS_AUDIENCE`、`PALAIS_ACCESS_CLIENT_ID`、`PALAIS_ACCESS_CLIENT_SECRET`（Jenkins credential，不進 Git）；`PALAIS_ACCESS_APPROVALS` 預設 1，設 2 為雙人審核且只影響之後新送的申請。
4. **Kubernetes**：ConfigMap `palais-access-config` 放非機密值與 `PALAIS_ACCESS_ENABLED=true`；Secret `palais-access-credentials` 放密鑰。`deployment.yaml` 已用 `envFrom` 讀取（皆 optional），不必改 YAML。
5. **重啟後端**，啟動 log 不應出現 `Incomplete Palais Keycloak configuration`（代表四個值缺一）。Maya 位址不同時才設 `PALAIS_ACCESS_VERIFICATION_URL`。
6. **驗收**：Chiaki 登入 `/account-settings` 看到待審清單；一般帳號送申請；Chiaki 放行後狀態由「同步中」變「已開通」，Maker 重新登入；確認能進 `/palais`；管理員撤權後，用撤權前簽發的舊 token 讀 Maya 敏感資料應回 403；最後確認首頁 Palais BPMN 圖顯示。

回退：`PALAIS_ACCESS_ENABLED` 改回 false 並重啟，資料表保留。初始 Checker 失去角色時，由 realm 管理員看過審計紀錄後手動補回。

```mermaid
flowchart LR
    A["資料庫遷移"] --> B["Keycloak 角色與服務帳號"]
    B --> C["取得 iss、aud、Checker UUID"]
    C --> D["ConfigMap 與 Secret"]
    D --> E["重啟後端"]
    E --> F["端到端驗收"]
    F -->|失敗| G["ENABLED 改回 false"]
```

## What a Checker can reach (inventory, 2026-11-20)

Read from the code of all four repos; nothing was run. A Checker is a logged-in account holding the realm role `palais-checker`, confirmed live by `GET /palais-access/verify-read` for every Maya read below.

| Palais piece | Calls | Role it requires | Checker |
|---|---|---|---|
| `/palais` landing: AI visibility panel | Maya `GET /market/admin/ai-visibility` (`owner_view`) | `manage-users` or `palais-checker` | allowed |
| Portfolio and history (`/api/market/portfolio`, `history`, `research-economics`) | Maya `GET /market/portfolio`, `/market/internal/portfolio/history` | `manage-users` or `palais-checker` | allowed |
| North-star reader metrics | Maya `GET /market/internal/north-star` | `manage-users` or `palais-checker` | allowed |
| Keycloak administration console card | link to the SSO admin console | Keycloak's own admin roles | hidden for Checkers (`data-palais-admin`) |
| IBKR snapshot, usage, custom dashboard, dismiss watch, automation check and the other `/market/*` admin routes | Maya | `manage-users` only | refused (403) |
| `fight`, `group`, `dance`, `party`, `levels` pages | static pages; `group` also posts to the frontend's own `/api/local-character-download` and `/api/save-character-image` | page itself has no API role; the two save routes only work on a macOS host and take no token | page opens; saving is a local-machine feature |
| `live` page | Maya `POST /maya-sawa/videos/merge-videos` | none (pre-existing, unauthenticated) | unchanged |

Follow-ups this inventory found:

- `merge-videos` has no authentication at all, for anyone; it predates this feature and is not a Checker matter.
- The pages themselves are only hidden by `NavScript` (redirect to `/account-settings`); the data is protected by the APIs above, so the table is the real boundary. Re-run this inventory whenever a Palais module gains an API call.
- The pending-request vote count shown in the UI counts every APPROVE not yet marked invalid, so a revoked Checker's vote is still displayed until the next decision recounts it. The decision itself recounts against live roles.
