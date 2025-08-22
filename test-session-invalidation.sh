#!/bin/bash

echo "🧪 測試 Session 無效處理..."

# 測試 1: 正常請求
echo "📝 測試 1: 正常請求"
curl -s -o /dev/null -w "HTTP Status: %{http_code}\n" http://localhost:8080/tymb/actuator/health

# 測試 2: 帶 Session 的請求
echo "📝 測試 2: 帶 Session 的請求"
curl -s -o /dev/null -w "HTTP Status: %{http_code}\n" \
  -H "Cookie: TYMB-SESSION=test-session-id" \
  http://localhost:8080/tymb/actuator/health

# 測試 3: 模擬 Session 無效的請求
echo "📝 測試 3: 模擬 Session 無效的請求"
curl -s -o /dev/null -w "HTTP Status: %{http_code}\n" \
  -H "Cookie: TYMB-SESSION=invalid-session-id" \
  http://localhost:8080/tymb/actuator/health

echo "✅ 測試完成！"
