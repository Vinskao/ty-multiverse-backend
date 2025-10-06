# PostgreSQL 數據庫設置

這個目錄包含了本地 PostgreSQL 數據庫的 Docker 配置。

## 🚀 快速啟動

### 方法 1：使用啟動腳本（推薦）
```bash
# 在項目根目錄執行
./start-db.sh
```

### 方法 2：手動啟動
```bash
cd db
docker-compose up -d
```

## 🛑 停止數據庫

### 方法 1：使用停止腳本
```bash
# 在項目根目錄執行
./stop-db.sh
```

### 方法 2：手動停止
```bash
cd db
docker-compose down -v
```

## 📊 數據庫信息

- **數據庫名稱**: peoplesystem
- **用戶名**: postgres
- **密碼**: postgres123
- **端口**: 5432
- **主機**: localhost

## 🔗 連接方式

### 1. 使用 Docker 命令
```bash
cd db
docker-compose exec postgres psql -U postgres -d peoplesystem
```

### 2. 使用外部工具
```bash
psql -h localhost -p 5432 -U postgres -d peoplesystem
```

### 3. 使用 JDBC URL
```
jdbc:postgresql://localhost:5432/peoplesystem
```

## 📋 數據表

啟動後會自動創建以下數據表：

- `people` - 人物表（包含向量嵌入）
- `weapon` - 武器表（包含向量嵌入）
- `ckeditor` - 編輯器內容表
- `gallery` - 圖片庫表
- `livestock` - 牲畜表
- `people_image` - 人物圖片表

## 🔧 配置說明

### Docker Compose 配置
- 使用 `pgvector/pgvector:pg16` 鏡像（支持向量操作）
- 自動初始化 SQL 腳本
- 健康檢查配置
- 數據持久化存儲

### 初始化腳本
- `db/init/01-init.sql` - 包含所有表結構和索引
- 自動啟用 pgvector 擴展
- 創建必要的索引和觸發器

## 🚨 注意事項

1. **端口衝突**: 確保 5432 端口未被其他 PostgreSQL 實例佔用
2. **Docker 權限**: 確保有足夠的 Docker 權限
3. **數據持久化**: 數據存儲在 Docker volume 中，停止容器不會丟失數據
4. **pgvector 擴展**: 支持向量相似性搜索

## 🔍 故障排除

### 1. 端口被佔用
```bash
# 檢查端口使用情況
lsof -i :5432

# 停止佔用端口的進程
sudo kill -9 <PID>
```

### 2. Docker 權限問題
```bash
# 將用戶添加到 docker 組
sudo usermod -aG docker $USER
# 重新登錄後生效
```

### 3. 數據庫連接失敗
```bash
# 檢查容器狀態
docker-compose ps

# 查看容器日誌
docker-compose logs postgres
```

## 📝 常用命令

```bash
# 查看容器狀態
cd db && docker-compose ps

# 查看日誌
cd db && docker-compose logs postgres

# 重啟數據庫
cd db && docker-compose restart postgres

# 備份數據庫
docker-compose exec postgres pg_dump -U postgres peoplesystem > backup.sql

# 恢復數據庫
docker-compose exec -T postgres psql -U postgres -d peoplesystem < backup.sql
```
