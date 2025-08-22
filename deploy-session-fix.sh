#!/bin/bash

echo "🚀 部署 Session 修復方案到 Prod 環境..."

# 1. 構建應用程序
echo "📦 構建應用程序..."
./mvnw clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ 構建失敗"
    exit 1
fi

echo "✅ 構建成功"

# 2. 部署到 K8s
echo "🌐 部署到 Kubernetes..."
kubectl apply -f k8s/deployment.yaml

if [ $? -ne 0 ]; then
    echo "❌ 部署失敗"
    exit 1
fi

echo "✅ 部署成功"

# 3. 等待 Pod 重啟
echo "⏳ 等待 Pod 重啟..."
kubectl rollout status deployment/ty-multiverse-backend

if [ $? -ne 0 ]; then
    echo "❌ Pod 重啟失敗"
    exit 1
fi

echo "✅ Pod 重啟成功"

# 4. 獲取 Pod 名稱
POD_NAME=$(kubectl get pods -l app.kubernetes.io/name=ty-multiverse-backend -o jsonpath='{.items[0].metadata.name}')

echo "🔍 Pod 名稱: $POD_NAME"

# 5. 監控日誌
echo "📊 開始監控 Session 相關日誌..."
echo "按 Ctrl+C 停止監控"
echo ""

kubectl logs -f $POD_NAME | grep -E "(Session|session|🔍|⚠️|✅|❌)"
