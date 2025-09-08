# RabbitMQ 本地環境

## 🚀 快速啟動

### 啟動完整環境（RabbitMQ + Consumer）
```bash
docker compose up -d
```

### 只啟動 RabbitMQ
```bash
docker compose up -d rabbitmq
```

### 啟動本地 Consumer
```bash
docker compose up -d rabbitmq-consumer-local
```

## 📊 服務狀態

### 查看所有服務
```bash
docker compose ps
```

### 查看日誌
```bash
# RabbitMQ 日誌
docker compose logs rabbitmq

# 本地 Consumer 日誌
docker compose logs rabbitmq-consumer-local

# 所有服務日誌
docker compose logs -f
```

## 🌐 訪問地址

### RabbitMQ 管理界面
- **URL**: http://localhost:15672
- **用戶名**: admin
- **密碼**: admin123

### 連接信息
- **主機**: localhost
- **端口**: 5672
- **用戶名**: admin
- **密碼**: admin123
- **虛擬主機**: /

## 🛑 停止服務

### 停止所有服務
```bash
docker compose down
```

### 停止並清理數據
```bash
docker compose down -v
```

## 🔧 後端應用連接

### 啟動後端應用
```bash
cd ..
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### 健康檢查
```bash
curl http://localhost:8080/tymb/actuator/health
```

## 📋 服務說明

### rabbitmq
- **用途**: 消息隊列服務
- **端口**: 5672 (AMQP), 15672 (管理界面)
- **健康檢查**: 自動檢測服務狀態

### rabbitmq-consumer-local
- **用途**: 本地開發環境 Consumer
- **Profile**: local
- **依賴**: 等待 RabbitMQ 健康檢查通過後啟動

### rabbitmq-consumer
- **用途**: 生產環境 Consumer
- **Profile**: k8s
- **副本數**: 3
