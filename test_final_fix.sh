#!/bin/bash

echo "=== 最終修復驗證 ==="

echo "1. 檢查應用程式狀態..."
curl -s http://localhost:8080/tymb/actuator/health | head -5

echo -e "\n2. 測試修復後的 metrics 端點..."

echo "測試 jvm.memory.used:"
curl -s http://localhost:8080/tymb/actuator/metrics/jvm.memory.used | head -3

echo -e "\n測試 process.cpu.usage:"
curl -s http://localhost:8080/tymb/actuator/metrics/process.cpu.usage | head -3

echo -e "\n測試 hikaricp.connections.active:"
curl -s http://localhost:8080/tymb/actuator/metrics/hikaricp.connections.active | head -3

echo -e "\n3. 檢查應用程式日誌..."
echo "請查看應用程式日誌中的以下信息："
echo "- 'Actuator 度量 URL 初始化為'"
echo "- '請求度量數據 URL'"
echo "- 是否還有 404 錯誤"

echo -e "\n4. 如果沒有 404 錯誤，說明修復成功！"

echo -e "\n=== 驗證完成 ==="
