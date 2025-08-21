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
            cpu: "7m"
          limits:
            memory: "128Mi"
            cpu: "20m"
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