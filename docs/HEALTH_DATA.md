# Apple 健康資料匯入與公開統計

## 做什麼、何時觸發
手動匯出 iPhone「健康」資料，在本機解析後送到後端；首頁公開跑步與活動統計。

## 架構圖
```mermaid
flowchart LR
    P["iPhone 健康：輸出所有健康資料"] --> S["本機 import_apple_health.py"]
    S -->|"運動紀錄、每日彙總；X-Health-Token"| B["POST /tymb/health-data/workouts、/daily"]
    B --> D["health_workout、health_daily"]
    D -->|"只有跑步統計"| G["GET /tymb/health-data/summary（公開）"]
    D -->|"其他運動與所有指標"| O["GET /overview、/metrics、/daily（manage-users 或 Palais Checker，即時驗證）"]
    G --> F["首頁：完整技術能力與工具"]
```

## 憑證
`HEALTH_INGEST_TOKEN`：K8s Secret `health-data-credentials`（deployment 以 envFrom 讀取，optional）。未設定時一律拒絕寫入。

## 測試
`./mvnw test -Dtest='RunningStatsTest,HealthDataSecurityWebTest,SecurityRulesWebTest'`；`python3 -m unittest discover -s scripts/tests -p test_import_apple_health.py`。
已知缺口：SQL 未在真實 PostgreSQL 上自動測試；解析器只用合成資料。

## 開發方法（2026-11-23）
1. 用 Apple 匯出而不用 Strava API：Strava 條款只准把資料顯示給本人，快取上限 7 天。
2. 匯入所有數值與類別紀錄，存「每日彙總」而非逐筆。不匯入 GPS 路線、心電圖、病歷檔，也不匯入生殖與月經相關資料（擁有者決定；心房顫動照存）。不存開始時間與裝置名稱。
3. （2026-11-23 改）只公開跑步統計；其他運動與所有指標給擁有者和 Palais Checker（`PalaisReadGuard` 每次向 Keycloak 確認，撤權立即生效）。畫面標註 Powered by Apple Health。
4. 手機和手錶會重複計步，累積型指標每天取總量最高的單一來源，是近似值。
5. 同一趟運動若由兩個 App 寫入，重疊超過一半視為同一筆，優先 Apple Watch。
6. 最快配速至少 3 km；週從星期一算（Asia/Taipei）。
7. 上線順序：`db/health_data.sql`、Secret、部署後端、匯入（先不加 `--apply`）、部署前端。重複匯入會覆寫，不會重複；來源刪除的資料不會自動刪。

## iPhone 捷徑：每週六 22:00 更新每日數值
步驟未在實機驗證。捷徑無法可靠讀取單次跑步（社群回報只拿得到步行＋跑步合計），所以公開的跑步統計仍靠匯出檔更新。

1. 每個指標：「尋找健康樣本」（類型、開始日期在過去 8 天、分組＝日）→「重複每個項目」→「格式化日期」（重複項目的開始日期，`yyyy-MM-dd`）→「文字」：
   `{"metric":"StepCount","day":"日期","unit":"count","total":數值,"samples":1}` →「結束重複」→「加入變數」lines（重複結果）。
   其他：`DistanceWalkingRunning`/km、`FlightsClimbed`/count、`AppleExerciseTime`/min、`ActiveEnergyBurned`/Cal。
2. 「合併文字」lines，自訂分隔 `,`；再用「文字」包成 `[合併文字]`。
3. 「取得 URL 內容」POST `https://tymultiverse.cc/tymb/health-data/daily`，標頭 `Content-Type: application/json`、`X-Health-Token`，內文＝檔案（上一步文字）。
4. 自動化：「一天中的時間」22:00、每週、星期六、立即執行。健康資料在鎖定時讀不到，執行時手機要解鎖。
   送過去 8 天是為了讓週六未完的當天在下週被補齊。
