#!/bin/bash

# 顏色定義
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}📨 開始部署 Message Consumer 到 Kubernetes...${NC}"

# 檢查 kubectl 是否可用
if ! command -v kubectl &> /dev/null; then
    echo -e "${RED}❌ kubectl 未安裝或不在 PATH 中${NC}"
    exit 1
fi

# 檢查當前 context
echo -e "${YELLOW}📋 當前 Kubernetes Context:${NC}"
kubectl config current-context

# 檢查 RabbitMQ 是否已部署
echo -e "${YELLOW}🔍 檢查 RabbitMQ 是否已部署...${NC}"
if ! kubectl get deployment rabbitmq -n default &> /dev/null; then
    echo -e "${RED}❌ RabbitMQ 部署不存在，請先執行 rabbit-mq.sh${NC}"
    exit 1
fi

# 部署 Consumer
echo -e "${YELLOW}📨 部署 Message Consumer...${NC}"
cat <<EOF | kubectl apply -f -
apiVersion: apps/v1
kind: Deployment
metadata:
  name: rabbitmq-consumer
  namespace: default
  labels:
    app: rabbitmq-consumer
spec:
  replicas: 3
  selector:
    matchLabels:
      app: rabbitmq-consumer
  template:
    metadata:
      labels:
        app: rabbitmq-consumer
    spec:
      containers:
      - name: rabbitmq-consumer
        image: eclipse-temurin:17-jre
        command: ["sh", "-c", "echo 'Consumer starting...' && sleep infinity"]
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "k8s"
        - name: RABBITMQ_HOST
          value: "rabbitmq-service"
        - name: RABBITMQ_PORT
          value: "5672"
        - name: RABBITMQ_USERNAME
          value: "admin"
        - name: RABBITMQ_PASSWORD
          value: "admin123"
        - name: RABBITMQ_VIRTUAL_HOST
          value: "/"
        resources:
          requests:
            memory: "64Mi"
            cpu: "10m"
          limits:
            memory: "128Mi"
            cpu: "27m"
        volumeMounts:
        - name: consumer-app
          mountPath: /app
        # 暫時移除健康檢查，因為沒有 HTTP 服務
      volumes:
      - name: consumer-app
        emptyDir: {}
---
apiVersion: v1
kind: Service
metadata:
  name: rabbitmq-consumer-service
  namespace: default
  labels:
    app: rabbitmq-consumer
spec:
  selector:
    app: rabbitmq-consumer
  ports:
  - name: http
    port: 8080
    targetPort: 8080
  type: ClusterIP
EOF

# 等待 Consumer 就緒
echo -e "${YELLOW}⏳ 等待 Consumer 就緒...${NC}"
kubectl wait --for=condition=available --timeout=300s deployment/rabbitmq-consumer -n default

# 顯示服務狀態
echo -e "${GREEN}✅ Consumer 部署完成！${NC}"
echo -e "${YELLOW}📊 服務狀態:${NC}"
kubectl get pods -n default -l app=rabbitmq-consumer
kubectl get services -n default -l app=rabbitmq-consumer

echo -e "${GREEN}🎉 所有服務部署完成！${NC}"
echo -e "${YELLOW}📝 注意事項:${NC}"
echo -e "   1. Consumer 已部署為簡單的容器"
echo -e "   2. 可以通過 kubectl exec 進入容器進行測試"
echo -e "   3. 後續可以添加實際的 consumer.jar"