#!/bin/bash

echo "=== Metrics 端點調試測試 ==="

echo "1. 檢查應用程式狀態..."
curl -s http://localhost:8080/tymb/actuator/health | head -5

echo -e "\n2. 檢查可用的 metrics..."
curl -s http://localhost:8080/tymb/actuator/metrics | head -10

echo -e "\n3. 測試各個 metrics 端點..."

echo "測試 jvm.memory.used:"
curl -s http://localhost:8080/tymb/actuator/metrics/jvm.memory.used | head -3

echo -e "\n測試 jvm.memory.max:"
curl -s http://localhost:8080/tymb/actuator/metrics/jvm.memory.max | head -3

echo -e "\n測試 process.cpu.usage:"
curl -s http://localhost:8080/tymb/actuator/metrics/process.cpu.usage | head -3

echo -e "\n測試 hikaricp.connections.active:"
curl -s http://localhost:8080/tymb/actuator/metrics/hikaricp.connections.active | head -3

echo -e "\n測試 system.cpu.usage:"
curl -s http://localhost:8080/tymb/actuator/metrics/system.cpu.usage | head -3

echo -e "\n4. 檢查應用程式日誌中的 URL 構建信息..."
echo "請查看應用程式日誌中的 'Actuator 度量 URL 初始化為' 和 '請求度量數據 URL' 信息"

echo -e "\n=== 調試完成 ==="
