# TY-Multiverse-Backend
Personal Website Backend System

## Architecture Design

### 1. Core Architecture
```mermaid
classDiagram
    %% Application Layer
    class TYMBackendApplication {
        +@SpringBootApplication
        +@EnableWebSocket
        +@EnableRetry
        +@EnableAsync
        +@EnableScheduling
    }
    
    %% Security Layer
    class SecurityConfig {
        +@EnableWebSecurity
        +@EnableMethodSecurity
        +JWT Authentication
        +OAuth2 Resource Server
        +CORS Configuration
    }
    
    class KeycloakController {
        +OAuth2 Redirect
        +Token Exchange
        +User Info
        +Token Introspect
    }
    
    class Guardian {
        +User Management
        +Admin Operations
        +Token Validation
    }
    
    %% Configuration Layer
    class RedisConfig {
        +@EnableCaching
        +RedisConnectionFactory
        +RedisTemplate
        +damage-calculations
        +tymb:sessions
        +Distributed Lock
    }
    
    class PrimaryDataSourceConfig {
        +PrimaryHikariCP (5 connections)
        +maximum-pool-size: 5
        +minimum-idle: 1
        +connection-timeout: 30s
        +leak-detection-threshold: 60s
    }
    
    class PeopleDataSourceConfig {
        +PeopleHikariCP (5 connections)
        +maximum-pool-size: 5
        +minimum-idle: 1
        +connection-timeout: 30s
        +leak-detection-threshold: 60s
    }
    
    class SessionConfig {
        +@EnableRedisHttpSession
        +Redis Session Store
        +Session Timeout
        +Session Fixation
    }
    
    %% Infrastructure Layer
    class Database {
        +PostgreSQL Primary
        +PostgreSQL People
        +Indexed Queries
        +Batch Operations
    }
    
    class Redis {
        +Cache Storage
        +Session Store
        +Distributed Lock
        +Concurrency Control
    }
    
    %% Layer Relationships
    TYMBackendApplication --> SecurityConfig
    TYMBackendApplication --> RedisConfig
    TYMBackendApplication --> PrimaryDataSourceConfig
    TYMBackendApplication --> PeopleDataSourceConfig
    TYMBackendApplication --> SessionConfig
    
    SecurityConfig --> KeycloakController
    SecurityConfig --> Guardian
    SecurityConfig --> SessionConfig
    
    SessionConfig --> RedisConfig
    RedisConfig --> Redis
    
    PrimaryDataSourceConfig --> Database
    PeopleDataSourceConfig --> Database
```

### 2. Module Architecture
```mermaid
classDiagram
    class PeopleModule {
        +PeopleController
        +PeopleService
        +WeaponDamageService
        +PeopleImageService
        +PeopleRepository
        +PeopleImageRepository
        +People.java
        +PeopleImage.java
        +DamageStrategy Pattern
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
    
    class CKEditorModule {
        +FileUploadController
        +EditContentService
        +EditContentRepository
        +EditContentVO.java
    }
  
    
    %% Module Dependencies
    PeopleModule --> WeaponModule
    PeopleModule --> GalleryModule
    PeopleModule --> CKEditorModule
```

### 3. Database Optimization Architecture
```mermaid
classDiagram
    class DatabaseOptimization {
        +B-Tree Indexes
        +Composite Indexes
        +Vector Indexes (pgvector)
        +Batch Queries
        +Connection Pooling
    }
    
    class PrimaryDatabase {
        +ckeditor Table
        +gallery Table
        +people_image Table
    }
    
    class PeopleDatabase {
        +people Table
        +weapon Table
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
        +@Cacheable
    }
    
    DatabaseOptimization --> PrimaryDatabase
    DatabaseOptimization --> PeopleDatabase
    PeopleDatabase --> PeopleTable
    PeopleDatabase --> WeaponTable
    DatabaseOptimization --> QueryOptimization
```

### 4. Cache Architecture
```mermaid
classDiagram
    class RedisConfig {
        +@EnableCaching
        +@EnableRedisHttpSession
        +RedisConnectionFactory
        +RedisTemplate
        +Connection Pool
    }
    
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
        +User Sessions
        +Session Timeout
    }
    
    class DistributedLock {
        +lock:content:save
        +lock:metrics:export:lock
        +lock:scheduled:cleanup:old:data:lock
        +lock:scheduled:generate:weekly:report:lock
        +lock:scheduled:backup:data:lock
        +lock:scheduled:health:check:lock
    }
    
    class MessageQueue {
        +qa_tymb_queue
        +Async Processing
        +Message Persistence
    }
    
    RedisConfig --> CacheStrategy
    CacheStrategy --> DamageCache
    CacheStrategy --> SessionCache
    CacheStrategy --> DistributedLock
    RedisConfig --> MessageQueue
```
### 4.2. Lua Script Flow
```mermaid
sequenceDiagram
    participant App as Application Layer
    participant Redis as Redis Server
    participant Lua as Lua Script Engine
    
    App->>Redis: Execute Lua Script
    Note over Redis: Distributed Lock Release Script
    Redis->>Lua: Load Script
    Lua->>Redis: redis.call('get', KEYS[1])
    Redis-->>Lua: Return Lock Value
    Lua->>Lua: Compare Lock Value
    alt Lock Value Matches
        Lua->>Redis: redis.call('del', KEYS[1])
        Redis-->>Lua: Delete Success
        Lua-->>App: Return 1
    else Lock Value Mismatch
        Lua-->>App: Return 0
    end
```

### 4.2.1. Distributed Lock Usage Scenario
```mermaid
sequenceDiagram
    participant Client as Client
    participant Service as Service Layer
    participant LockUtil as DistributedLockUtil
    participant Redis as Redis
    
    Client->>Service: Request Operation
    Service->>LockUtil: executeWithLock(lockKey, timeout, operation)
    LockUtil->>Redis: SETNX lockKey value
    alt Lock Acquisition Success
        Redis-->>LockUtil: true
        LockUtil->>Service: Execute Operation
        Service-->>LockUtil: Operation Result
        LockUtil->>Redis: DEL lockKey
        LockUtil-->>Service: Return Result
        Service-->>Client: Success Response
    else Lock Acquisition Failed
        Redis-->>LockUtil: false
        LockUtil-->>Service: Throw Exception
        Service-->>Client: Operation Skipped
    end
```

### 5. Connection Pool Architecture
```mermaid
classDiagram
    class HikariCPConfig {
        +PrimaryDataSourceConfig
        +PeopleDataSourceConfig
        +Connection Management
        +destroyMethod="close"
    }
    
    class PrimaryPool {
        +maximum-pool-size: 5
        +minimum-idle: 1
        +connection-timeout: 30s
        +leak-detection-threshold: 60s
        +idle-timeout: 600000
        +max-lifetime: 1800000
        +pool-name: PrimaryHikariCP
    }
    
    class PeoplePool {
        +maximum-pool-size: 5
        +minimum-idle: 1
        +connection-timeout: 30s
        +leak-detection-threshold: 60s
        +idle-timeout: 300000
        +max-lifetime: 1800000
        +pool-name: PeopleHikariCP
    }
    
    class PoolMonitoring {
        +ActiveConnections
        +IdleConnections
        +WaitingThreads
        +ConnectionTimeout
        +register-mbeans: true
        +auto-commit: false
    }
    
    HikariCPConfig --> PrimaryPool
    HikariCPConfig --> PeoplePool
    HikariCPConfig --> PoolMonitoring
```

### 6. Security Authentication Architecture
```mermaid
classDiagram
    class SecurityConfig {
        +@EnableWebSecurity
        +@EnableMethodSecurity
        +JWT Authentication
        +OAuth2 Resource Server
        +CORS Configuration
        +Stateless Keycloak Endpoints
    }
    
    class JWTValidation {
        +OAuth2ResourceServer
        +JwtDecoder
        +CustomJwtGrantedAuthoritiesConverter
        +Bearer Token Resolver
    }
    
    class Authorization {
        +ROLE_GUEST
        +ROLE_MANAGE_USERS
        +ROLE_ADMIN
        +ROLE_USER
        +permitAll() for /people/names
        +authenticated() for protected endpoints
    }
    
    class SessionManagement {
        +Redis Session Storage
        +Session Timeout
        +Session Fixation
        +@EnableRedisHttpSession
        +tymb:sessions namespace
    }
    
    class KeycloakController {
        +OAuth2 Redirect
        +Token Exchange
        +User Info
        +Token Introspect
        +Dynamic redirectUri
    }
    
    SecurityConfig --> JWTValidation
    SecurityConfig --> Authorization
    SecurityConfig --> SessionManagement
    SecurityConfig --> KeycloakController
```

### 7. Error Handling Architecture
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

### 8. Monitoring Architecture
```mermaid
classDiagram
    class ActuatorEndpoints {
        +/actuator/health
        +/actuator/metrics
        +/actuator/prometheus
        +/actuator/info
        +/actuator/loggers
        +/actuator/env
        +/actuator/beans
        +/actuator/mappings
    }
    
    class MetricsConfig {
        +MeterRegistry
        +HikariCP Metrics
        +Custom Metrics
        +Micrometer Configuration
        +Prometheus Metrics
        +@EnableMetrics
        +Metrics Export
    }
    
    class MetricsWSController {
        +@Scheduled(fixedRate = 5000)
        +WebSocket Broadcast
        +Distributed Lock
        +Real-time Metrics
        +@MessageMapping("/metrics")
        +@SendTo("/topic/metrics")
    }
    
    class HealthChecks {
        +Database Health
        +Redis Health
        +Application Health
        +Connection Pool Health
        +Disk Space Health
        +Custom Health Indicators
    }
    
    class WebSocketConfig {
        +@EnableWebSocket
        +@EnableWebSocketMessageBroker
        +ServerEndpointExporter
        +Real-time Communication
        +STOMP Configuration
    }
    
    class ScheduledTaskService {
        +@Scheduled Tasks
        +Distributed Lock
        +Health Monitoring
        +Performance Metrics
        +Cleanup Operations
    }
    
    class LoggingConfig {
        +Logback Configuration
        +Structured Logging
        +Log Levels
        +Performance Logging
    }
    
    ActuatorEndpoints --> MetricsConfig
    MetricsConfig --> MetricsWSController
    MetricsConfig --> HealthChecks
    MetricsWSController --> WebSocketConfig
    ScheduledTaskService --> MetricsConfig
    ScheduledTaskService --> HealthChecks
    LoggingConfig --> ActuatorEndpoints
```

### 9. RabbitMQ Data Flow Architecture
```mermaid
graph LR
    A[Backend<br/>Spring Boot<br/>REST Endpoints] --> B[RabbitMQ<br/>Message Queue]
    B --> C[Consumer<br/>Spring Boot<br/>JDBC Processing]
    C --> D[PostgreSQL<br/>Database]
    
    A --> E[Redis<br/>Session & Cache]
    C --> E
    
    classDef producer fill:#e1f5fe
    classDef mq fill:#f3e5f5
    classDef consumer fill:#e8f5e8
    classDef database fill:#ffebee
    classDef cache fill:#fff3e0
    
    class A producer
    class B mq
    class C consumer
    class D database
    class E cache
```

**架構說明：**
- **Backend (Producer)**: Spring Boot 應用程式，提供 REST API 端點，負責接收請求並發送訊息到 RabbitMQ
- **Consumer**: Spring Boot 應用程式，使用 JDBC 處理訊息並將數據寫入 PostgreSQL 資料庫
- **RabbitMQ**: 訊息佇列，實現非同步處理和解耦
- **PostgreSQL**: 主要資料庫，儲存處理後的數據
- **Redis**: 會話儲存、快取和分散式鎖，支援 `tymb:sessions` 和 `damage-calculations` 命名空間
- **Session 使用**: 目前僅 CKEditor 和 DeckOfCards 模組使用 Session 認證
- **其他模組**: 使用 JWT 無狀態認證

## Documentation and Tools

### Swagger UI
- Local Environment: `http://localhost:8080/tymb/swagger-ui/index.html#/`
- Production Environment: `https://peoplesystem.tatdvsonorth.com/tymb/swagger-ui/index.html#/`

### JavaDoc Documentation
- Local Environment: `http://localhost:8080/tymb/javadoc/index.html`
- Production Environment: `https://peoplesystem.tatdvsonorth.com/tymb/javadoc/index.html`

### Docker Build
- Build Command: `docker build -t papakao/ty-multiverse-backend:latest .`
- Multi-platform Build: `docker buildx build --platform linux/arm64 -t papakao/ty-multiverse-backend:latest --push .`
