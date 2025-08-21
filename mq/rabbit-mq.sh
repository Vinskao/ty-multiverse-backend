#!/bin/bash

# 顏色定義
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}🐰 開始部署 RabbitMQ 到 Kubernetes...${NC}"

# 檢查 kubectl 是否可用
if ! command -v kubectl &> /dev/null; then
    echo -e "${RED}❌ kubectl 未安裝或不在 PATH 中${NC}"
    exit 1
fi

# 檢查當前 context
echo -e "${YELLOW}📋 當前 Kubernetes Context:${NC}"
kubectl config current-context

# 部署 RabbitMQ
echo -e "${YELLOW}🐰 部署 RabbitMQ...${NC}"
cat <<EOF | kubectl apply -f -
apiVersion: apps/v1
kind: Deployment
metadata:
  name: rabbitmq
  namespace: default
  labels:
    app: rabbitmq
spec:
  replicas: 1
  selector:
    matchLabels:
      app: rabbitmq
  template:
    metadata:
      labels:
        app: rabbitmq
    spec:
      containers:
      - name: rabbitmq
        image: rabbitmq:3.12-management
        ports:
        - containerPort: 5672
          name: amqp
        - containerPort: 15672
          name: management
        env:
        - name: RABBITMQ_DEFAULT_USER
          value: "admin"
        - name: RABBITMQ_DEFAULT_PASS
          value: "admin123"
        - name: RABBITMQ_DEFAULT_VHOST
          value: "/"
        resources:
          requests:
            memory: "256Mi"
            cpu: "50m"
          limits:
            memory: "512Mi"
            cpu: "200m"
        volumeMounts:
        - name: rabbitmq-data
          mountPath: /var/lib/rabbitmq
      volumes:
      - name: rabbitmq-data
        emptyDir: {}
---
apiVersion: v1
kind: Service
metadata:
  name: rabbitmq-service
  namespace: default
  labels:
    app: rabbitmq
spec:
  selector:
    app: rabbitmq
  ports:
  - name: amqp
    port: 5672
    targetPort: 5672
  - name: management
    port: 15672
    targetPort: 15672
  type: ClusterIP
---
apiVersion: v1
kind: ConfigMap
metadata:
  name: rabbitmq-config
  namespace: default
data:
  rabbitmq.conf: |
    # 基本配置
    listeners.tcp.default = 5672
    management.tcp.port = 15672
    
    # 記憶體和磁碟限制
    vm_memory_high_watermark.relative = 0.6
    disk_free_limit.relative = 2.0
    
    # 連接限制
    tcp_listen_options.backlog = 128
    tcp_listen_options.nodelay = true
    
    # 日誌配置
    log.console = true
    log.console.level = info
    
    # 集群配置（單節點）
    cluster_formation.peer_discovery_backend = rabbit_peer_discovery_classic_config
    cluster_formation.classic_config.nodes.1 = rabbit@rabbitmq-0
EOF

# 等待 RabbitMQ 就緒
echo -e "${YELLOW}⏳ 等待 RabbitMQ 就緒...${NC}"
kubectl wait --for=condition=available --timeout=300s deployment/rabbitmq -n default

# 顯示服務狀態
echo -e "${GREEN}✅ RabbitMQ 部署完成！${NC}"
echo -e "${YELLOW}📊 服務狀態:${NC}"
kubectl get pods -n default -l app=rabbitmq
kubectl get services -n default -l app=rabbitmq

# 顯示 RabbitMQ 管理界面訪問信息
echo -e "${GREEN}🌐 RabbitMQ 管理界面:${NC}"
echo -e "   URL: http://localhost:15672"
echo -e "   用戶名: admin"
echo -e "   密碼: admin123"
echo -e ""
echo -e "${YELLOW}🔗 端口轉發命令:${NC}"
echo -e "   kubectl port-forward service/rabbitmq-service 15672:15672 -n default"
echo -e "   kubectl port-forward service/rabbitmq-service 5672:5672 -n default"