#!/bin/bash

# 顏色定義
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}🧹 開始清理 RabbitMQ 和 Consumer 部署...${NC}"

# 檢查 kubectl 是否可用
if ! command -v kubectl &> /dev/null; then
    echo -e "${RED}❌ kubectl 未安裝或不在 PATH 中${NC}"
    exit 1
fi

# 刪除 Consumer 部署
echo -e "${YELLOW}🗑️  刪除 Message Consumer...${NC}"
kubectl delete -f consumer-deployment.yaml -n rabbitmq-system --ignore-not-found=true

# 刪除 RabbitMQ 部署
echo -e "${YELLOW}🗑️  刪除 RabbitMQ...${NC}"
kubectl delete -f rabbitmq-deployment.yaml -n rabbitmq-system --ignore-not-found=true

# 刪除命名空間
echo -e "${YELLOW}🗑️  刪除命名空間...${NC}"
kubectl delete namespace rabbitmq-system --ignore-not-found=true

echo -e "${GREEN}✅ 清理完成！${NC}"
