#!/bin/bash

echo "🧪 測試樂觀鎖定修復..."

# 等待應用程序啟動
sleep 10

echo "📡 測試 1: 簡單更新請求"
curl -X POST http://localhost:8080/tymb/people/update \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Wavo",
    "physicPower": 5,
    "magicPower": 13111,
    "utilityPower": 5,
    "attributes": "Strength",
    "version": 0
  }' \
  -w "\nHTTP Status: %{http_code}\n"

echo ""
echo "📡 測試 2: 完整更新請求"
curl -X POST http://localhost:8080/tymb/people/update \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Wavo",
    "physicPower": 5,
    "magicPower": 13111,
    "utilityPower": 5,
    "race": "Interstellar Prophetic Singularity Human",
    "attributes": "Strength",
    "gender": "M",
    "heightCm": 177,
    "weightKg": 60,
    "profession": "King",
    "combat": "lewd",
    "favoriteFoods": "Hot Soup",
    "job": "Chef",
    "knownAs": "Boss",
    "personality": "Dominant and passionate",
    "interest": "Public humiliation, forcing, blackmailing",
    "likes": "Power and control",
    "dislikes": "Disloyalty and resistance",
    "faction": "Lily Palais",
    "armyId": 1,
    "armyName": "King",
    "deptId": 1,
    "deptName": "Royal",
    "originArmyId": 1,
    "originArmyName": "King",
    "gaveBirth": false,
    "email": "",
    "age": 31,
    "proxy": "Royal Will",
    "baseAttributes": "Strength",
    "bonusAttributes": "Intelligence",
    "stateAttributes": "Normal",
    "version": 0
  }' \
  -w "\nHTTP Status: %{http_code}\n"

echo ""
echo "✅ 測試完成！" 