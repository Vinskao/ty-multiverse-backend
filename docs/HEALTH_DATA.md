# Apple 健康資料匯入與公開統計

## 做什麼、何時觸發
手動匯出 iPhone「健康」資料，在本機解析後送到後端；首頁公開跑步與活動統計。

## 架構圖
```mermaid
flowchart LR
    P["iPhone 健康：輸出所有健康資料"] --> S["本機 import_apple_health.py"]
    S -->|"運動紀錄、每日彙總；X-Health-Token"| B["POST /tymb/health-data/workouts、/daily"]
    B --> D["health_workout、health_daily"]
    D -->|"跑步統計、運動總計、白名單指標"| G["GET /tymb/health-data/summary（公開）"]
    D -->|"其他指標"| O["GET /metrics、/daily（manage-users）"]
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
3. 公開採白名單（`health-data.public-metrics`），預設只有活動類。心率、睡眠、體重等只給擁有者；要公開需逐項加入，且只支援有每日總量的指標。
4. 手機和手錶會重複計步，累積型指標每天取總量最高的單一來源，是近似值。
5. 同一趟運動若由兩個 App 寫入，重疊超過一半視為同一筆，優先 Apple Watch。
6. 最快配速至少 3 km；週從星期一算（Asia/Taipei）。
7. 上線順序：`db/health_data.sql`、Secret、部署後端、匯入（先不加 `--apply`）、部署前端。重複匯入會覆寫，不會重複；來源刪除的資料不會自動刪。

## iPhone 捷徑：每日自動更新活動數值
手機上的 Claude 不能代建捷徑或排程，需自己設定。動作名稱會因 iOS 版本略有不同，以下步驟尚未在實機驗證。

1. 捷徑 App → ＋，命名「上傳健康資料」。
2. 加「日期」（目前日期）→「調整日期」減 1 天 →「格式化日期」選自訂 `yyyy-MM-dd`，設為變數「昨天」。送昨天，因為當天還沒結束。
3. 加「尋找健康樣本」：類型＝步數，開始日期＝昨天，分組＝日。再加「計算統計資料」→ 總和，設為變數「步數」。
4. 對「步行＋跑步距離」「爬樓層數」「運動分鐘」「動態能量」重複步驟 3。
5. 加「文字」，填入並把變數插進去（數字不能有千分位逗號）：
   `[{"metric":"StepCount","day":"昨天","unit":"count","total":步數,"samples":1},{"metric":"DistanceWalkingRunning","day":"昨天","unit":"km","total":距離,"samples":1}]`
   其他 metric／unit：`FlightsClimbed`/count、`AppleExerciseTime`/min、`ActiveEnergyBurned`/kcal。
6. 加「取得 URL 內容」：`https://tymultiverse.cc/tymb/health-data/daily`，方法 POST，標頭 `Content-Type: application/json` 與 `X-Health-Token`（token 只存在捷徑內，不要分享該捷徑），要求內文選「檔案」＝上一步文字。
7. 加「顯示通知」顯示結果，手動跑一次，允許健康與網路權限。
8. 自動化 → ＋ →「App」→ 打開常用的 App（或「充電器」），選「立即執行」。健康資料在**鎖定時讀不到**，固定時間觸發可能失敗。

跑步「單次紀錄」（最快、單日高峰、週累積）不走這個捷徑，用匯出檔重新匯入更新；重送會覆寫同一天，不會重複。
