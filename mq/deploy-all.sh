#!/bin/bash

# 顏色定義
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}🚀 開始部署 RabbitMQ 和 Consumer 到 Kubernetes...${NC}"

# 檢查 kubectl 是否可用
if ! command -v kubectl &> /dev/null; then
    echo -e "${RED}❌ kubectl 未安裝或不在 PATH 中${NC}"
    exit 1
fi

# 檢查當前 context
echo -e "${YELLOW}📋 當前 Kubernetes Context:${NC}"
kubectl config current-context

# 創建命名空間（如果不存在）
echo -e "${YELLOW}📦 創建命名空間...${NC}"
kubectl create namespace rabbitmq-system --dry-run=client -o yaml | kubectl apply -f -

# 部署 RabbitMQ
echo -e "${YELLOW}🐰 部署 RabbitMQ...${NC}"
bash rabbit-mq.sh | kubectl apply -f - -n rabbitmq-system

# 等待 RabbitMQ 就緒
echo -e "${YELLOW}⏳ 等待 RabbitMQ 就緒...${NC}"
kubectl wait --for=condition=available --timeout=300s deployment/rabbitmq -n rabbitmq-system

# 部署 Consumer
echo -e "${YELLOW}📨 部署 Message Consumer...${NC}"
bash consumer.sh | kubectl apply -f - -n rabbitmq-system

# 等待 Consumer 就緒
echo -e "${YELLOW}⏳ 等待 Consumer 就緒...${NC}"
kubectl wait --for=condition=available --timeout=300s deployment/message-consumer -n rabbitmq-system

# 顯示服務狀態
echo -e "${GREEN}✅ 部署完成！${NC}"
echo -e "${YELLOW}📊 服務狀態:${NC}"
kubectl get pods -n rabbitmq-system
kubectl get services -n rabbitmq-system

# 顯示 RabbitMQ 管理界面訪問信息
echo -e "${GREEN}🌐 RabbitMQ 管理界面:${NC}"
echo -e "   URL: http://localhost:15672"
echo -e "   用戶名: admin"
echo -e "   密碼: admin123"
echo -e ""
echo -e "${YELLOW}🔗 端口轉發命令:${NC}"
echo -e "   kubectl port-forward service/rabbitmq-service 15672:15672 -n rabbitmq-system"
echo -e "   kubectl port-forward service/rabbitmq-service 5672:5672 -n rabbitmq-system"
