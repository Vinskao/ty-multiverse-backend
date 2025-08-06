# TY-Multiverse-Backend
個人網站後端系統

## 架構設計

### 1. 核心架構
```mermaid
classDiagram
    class SpringBootApplication {
        +@SpringBootApplication
        +@EnableCaching
        +@EnableJpaRepositories
    }
    
    class HikariCP {
        +PrimaryHikariCP (50 connections)
        +PeopleHikariCP (15 connections)
        +Connection Pool Management
    }
    
    class RedisCache {
        +@Cacheable
        +damage-calculations
        +Session Storage
        +Distributed Lock
    }
    
    class Database {
        +PostgreSQL Primary
        +PostgreSQL People
        +Indexed Queries
        +Batch Operations
    }
    
    SpringBootApplication --> HikariCP
    SpringBootApplication --> RedisCache
    HikariCP --> Database
    RedisCache --> Database
```

### 2. 模組架構
```mermaid
classDiagram
    class PeopleModule {
        +PeopleController
        +PeopleService
        +WeaponDamageService
        +PeopleRepository
        +People.java
    }
    
    class WeaponModule {
        +WeaponController
        +WeaponService
        +WeaponRepository
        +Weapon.java
    }
    
    class GalleryModule {
        +GalleryController
        +GalleryService
        +GalleryRepository
        +Gallery.java
    }
    
    class LivestockModule {
        +LivestockController
        +LivestockService
        +LivestockRepository
        +Livestock.java
    }
    
    class CKEditorModule {
        +FileUploadController
        +EditContentService
        +EditContentRepository
        +EditContentVO.java
    }
    
    PeopleModule --> WeaponModule
    PeopleModule --> GalleryModule
    PeopleModule --> LivestockModule
    PeopleModule --> CKEditorModule
```

### 3. 資料庫優化架構
```mermaid
classDiagram
    class DatabaseOptimization {
        +B-Tree Indexes
        +Composite Indexes
        +Vector Indexes (pgvector)
        +Batch Queries
    }
    
    class PeopleTable {
        +idx_people_name (Primary)
        +idx_people_race
        +idx_people_gender
        +idx_people_faction
        +idx_people_embedding
    }
    
    class WeaponTable {
        +idx_weapon_owner
        +idx_weapon_base_damage
        +idx_weapon_bonus_damage
        +idx_weapon_embedding
    }
    
    class QueryOptimization {
        +IN Clause (N+1 Fix)
        +Batch Operations
        +Selective Columns
        +Caching Strategy
    }
    
    DatabaseOptimization --> PeopleTable
    DatabaseOptimization --> WeaponTable
    DatabaseOptimization --> QueryOptimization
```

### 4. 快取架構
```mermaid
classDiagram
    class CacheStrategy {
        +@Cacheable
        +Redis Storage
        +TTL Management
        +Cache Eviction
    }
    
    class DamageCache {
        +damage-calculations::name
        +Weapon Damage Results
        +People Attributes
    }
    
    class SessionCache {
        +tymb:sessions
        +CKEditor Drafts
        +Game States
    }
    
    class DistributedLock {
        +lock:content:save
        +lock:metrics:export
        +lock:livestock:query
    }
    
    CacheStrategy --> DamageCache
    CacheStrategy --> SessionCache
    CacheStrategy --> DistributedLock
```

### 5. 連線池架構
```mermaid
classDiagram
    class HikariCPConfig {
        +PrimaryHikariCP
        +PeopleHikariCP
        +Connection Management
    }
    
    class PrimaryPool {
        +maximum-pool-size: 50
        +minimum-idle: 10
        +connection-timeout: 30s
        +leak-detection-threshold: 60s
    }
    
    class PeoplePool {
        +maximum-pool-size: 15
        +minimum-idle: 5
        +connection-timeout: 30s
        +leak-detection-threshold: 60s
    }
    
    class PoolMonitoring {
        +ActiveConnections
        +IdleConnections
        +WaitingThreads
        +ConnectionTimeout
    }
    
    HikariCPConfig --> PrimaryPool
    HikariCPConfig --> PeoplePool
    HikariCPConfig --> PoolMonitoring
```

### 6. 安全認證架構
```mermaid
classDiagram
    class SecurityConfig {
        +@EnableWebSecurity
        +@EnableMethodSecurity
        +JWT Authentication
        +Session Management
    }
    
    class JWTValidation {
        +OAuth2ResourceServer
        +JwtDecoder
        +CustomJwtGrantedAuthoritiesConverter
    }
    
    class Authorization {
        +ROLE_GUEST
        +ROLE_MANAGE_USERS
        +ROLE_ADMIN
        +ROLE_USER
    }
    
    class SessionManagement {
        +Redis Session Storage
        +Session Timeout
        +Session Fixation
    }
    
    SecurityConfig --> JWTValidation
    SecurityConfig --> Authorization
    SecurityConfig --> SessionManagement
```

### 7. 錯誤處理架構
```mermaid
classDiagram
    class GlobalExceptionHandler {
        +Chain of Responsibility
        +Error Response Builder
    }
    
    class BusinessApiExceptionHandler {
        +@Order(0)
        +BusinessException
    }
    
    class DataIntegrityApiExceptionHandler {
        +@Order(1)
        +DataIntegrityViolationException
    }
    
    class ValidationApiExceptionHandler {
        +@Order(2)
        +MethodArgumentNotValidException
    }
    
    class DefaultApiExceptionHandler {
        +@Order(Integer.MAX_VALUE)
        +Generic Exception
    }
    
    GlobalExceptionHandler --> BusinessApiExceptionHandler
    GlobalExceptionHandler --> DataIntegrityApiExceptionHandler
    GlobalExceptionHandler --> ValidationApiExceptionHandler
    GlobalExceptionHandler --> DefaultApiExceptionHandler
```

### 8. 監控架構
```mermaid
classDiagram
    class ActuatorEndpoints {
        +/health
        +/metrics
        +/prometheus
        +/info
        +/loggers
        +/env
    }
    
    class MetricsConfig {
        +MeterRegistry
        +HikariCP Metrics
        +Custom Metrics
    }
    
    class MetricsWSController {
        +@Scheduled
        +WebSocket Broadcast
        +Distributed Lock
    }
    
    class HealthChecks {
        +Database Health
        +Redis Health
        +Application Health
    }
    
    ActuatorEndpoints --> MetricsConfig
    MetricsConfig --> MetricsWSController
    MetricsConfig --> HealthChecks
```

### 9. 部署架構
```mermaid
classDiagram
    class DockerBuild {
        +Multi-stage Build
        +BuildKit Optimization
        +Security Scan
    }
    
    class KubernetesDeploy {
        +Deployment
        +Service
        +ConfigMap
        +Secret
    }
    
    class CI_CD {
        +Jenkins Pipeline
        +Maven Build
        +Docker Build
        +K8s Deploy
    }
    
    class Monitoring {
        +Prometheus
        +Grafana
        +Logging
    }
    
    DockerBuild --> KubernetesDeploy
    CI_CD --> DockerBuild
    CI_CD --> KubernetesDeploy
    KubernetesDeploy --> Monitoring
```

### 10. 效能優化架構
```mermaid
classDiagram
    class PerformanceOptimization {
        +Database Indexing
        +Connection Pooling
        +Caching Strategy
        +N+1 Query Fix
    }
    
    class DatabaseOptimization {
        +B-Tree Indexes
        +Composite Indexes
        +Vector Indexes
        +Batch Queries
    }
    
    class CacheOptimization {
        +Redis Caching
        +@Cacheable
        +TTL Management
    }
    
    class QueryOptimization {
        +IN Clause
        +Selective Columns
        +Batch Operations
    }
    
    PerformanceOptimization --> DatabaseOptimization
    PerformanceOptimization --> CacheOptimization
    PerformanceOptimization --> QueryOptimization
```

## 文檔與工具

### Swagger UI
- 本地環境：`http://localhost:8080/tymb/swagger-ui/index.html#/`
- 生產環境：`https://peoplesystem.tatdvsonorth.com/tymb/swagger-ui/index.html#/`

### JavaDoc 文檔
- 本地環境：`http://localhost:8080/tymb/javadoc/index.html`
- 生產環境：`https://peoplesystem.tatdvsonorth.com/tymb/javadoc/index.html`

### Docker 建置
- 建置指令：`docker build -t papakao/ty-multiverse-backend:latest .`
- 多平台建置：`docker buildx build --platform linux/arm64 -t papakao/ty-multiverse-backend:latest --push .`
