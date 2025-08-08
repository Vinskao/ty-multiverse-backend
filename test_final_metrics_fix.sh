#!/bin/bash

echo "=== 最終測試 Metrics 端點修復 ==="

# 檢查應用程式是否運行
echo "1. 檢查應用程式狀態..."
curl -s http://localhost:8080/tymb/actuator/health | head -5

echo -e "\n2. 測試所有修復後的 metrics 端點..."

echo "測試 jvm.memory.used:"
curl -s http://localhost:8080/tymb/actuator/metrics/jvm.memory.used | jq '.name, .measurements[0].value' 2>/dev/null || echo "需要安裝 jq 來格式化 JSON"

echo -e "\n測試 jvm.memory.max:"
curl -s http://localhost:8080/tymb/actuator/metrics/jvm.memory.max | jq '.name, .measurements[0].value' 2>/dev/null || echo "需要安裝 jq 來格式化 JSON"

echo -e "\n測試 process.cpu.usage:"
curl -s http://localhost:8080/tymb/actuator/metrics/process.cpu.usage | jq '.name, .measurements[0].value' 2>/dev/null || echo "需要安裝 jq 來格式化 JSON"

echo -e "\n測試 hikaricp.connections.active:"
curl -s http://localhost:8080/tymb/actuator/metrics/hikaricp.connections.active | jq '.name, .measurements[0].value' 2>/dev/null || echo "需要安裝 jq 來格式化 JSON"

echo -e "\n測試 system.cpu.usage:"
curl -s http://localhost:8080/tymb/actuator/metrics/system.cpu.usage | jq '.name, .measurements[0].value' 2>/dev/null || echo "需要安裝 jq 來格式化 JSON"

echo -e "\n3. 檢查日誌中是否還有 404 錯誤..."
echo "如果沒有 404 錯誤，說明修復成功！"

echo -e "\n=== 測試完成 ==="
