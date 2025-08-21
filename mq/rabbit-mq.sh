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
            cpu: "25m"
          limits:
            memory: "512Mi"
            cpu: "50m"
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