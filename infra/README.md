# 本地基礎設施環境 (MQ + Redis)

## 🚀 快速啟動

### 啟動完整環境（RabbitMQ + Consumer + Redis）
```bash
docker compose up -d
```

### 啟動特定服務
```bash
# 只啟動 RabbitMQ
docker compose up -d rabbitmq

# 只啟動 Redis
docker compose up -d redis

# 啟動本地 Consumer
docker compose up -d rabbitmq-consumer
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

# Redis 日誌
docker compose logs redis

# 本地 Consumer 日誌
docker compose logs rabbitmq-consumer

# 所有服務日誌
docker compose logs -f
```

## 🌐 訪問地址與連接信息

### RabbitMQ 管理界面
- **URL**: http://localhost:15672
- **用戶名**: admin
- **密碼**: admin123

### RabbitMQ (MQ連線)
- **主機**: localhost
- **端口**: 5672
- **用戶名**: admin
- **密碼**: admin123
- **虛擬主機**: /

### Redis
- **主機**: localhost
- **端口**: 6379

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
- **Image**: rabbitmq:4.1-management
- **用途**: 作為 Message Queue 並且帶有 Management UI 介面。
- **端口**: 5672 (MQ 連線), 15672 (管理介面)
- **健康檢查**: 自動檢測服務狀態

### rabbitmq-consumer
- **Image**: eclipse-temurin:17-jre
- **用途**: 模擬或執行消費者程式 (consumer-app)，會等待 RabbitMQ 就緒後連線。

### redis
- **Image**: redis:7
- **用途**: 作為快取與限流等機制的記憶體資料庫。
- **端口**: 6379
