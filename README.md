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

### 9. gRPC Service Architecture

```mermaid
classDiagram
    class GrpcServerConfig {
        +@Configuration
        +@ConditionalOnProperty
        +grpcPort: int
        +grpcPeopleService: GrpcPeopleServiceImpl
        +grpcServer: Server
        +start()
        +stop()
        +grpcServer() Bean
    }

    class GrpcPeopleServiceImpl {
        +@Service
        +@extends PeopleServiceGrpc.PeopleServiceImplBase
        +peopleService: PeopleService
        +getAllPeople()
        +getPeopleByName()
        +insertPeople()
        +updatePeople()
        +deletePeople()
        +convertToPeopleData()
        +convertToPeopleEntity()
    }

    class PeopleServiceProto {
        +@com.google.protobuf.Generated
        +PeopleService service
        +PeopleData message
        +GetAllPeopleRequest message
        +GetAllPeopleResponse message
        +PeopleResponse message
        +UpdatePeopleRequest message
        +DeletePeopleRequest message
        +DeletePeopleResponse message
    }

    class ProtobufMavenPlugin {
        +protocArtifact: com.google.protobuf:protoc:4.32.1
        +pluginArtifact: io.grpc:protoc-gen-grpc-java:1.58.0
        +protoSourceRoot: src/main/proto
        +generate-sources phase
    }

    GrpcServerConfig --> GrpcPeopleServiceImpl : @Autowired
    GrpcPeopleServiceImpl --> PeopleServiceProto : extends
    ProtobufMavenPlugin --> PeopleServiceProto : generates
```

### 9.1. Protocol Buffers 定義

```protobuf
// people_service.proto
syntax = "proto3";

option java_multiple_files = true;
option java_package = "tw.com.tymbackend.grpc.people";
option java_outer_classname = "PeopleServiceProto";

package tymbackend.people;

// People Service - 人物服務
service PeopleService {
  // 獲取所有人物
  rpc GetAllPeople(GetAllPeopleRequest) returns (GetAllPeopleResponse);

  // 根據名稱獲取人物
  rpc GetPeopleByName(GetPeopleByNameRequest) returns (PeopleResponse);

  // 插入人物
  rpc InsertPeople(PeopleData) returns (PeopleResponse);

  // 更新人物
  rpc UpdatePeople(UpdatePeopleRequest) returns (PeopleResponse);

  // 刪除人物
  rpc DeletePeople(DeletePeopleRequest) returns (DeletePeopleResponse);
}

// People 數據模型 - 完整字段映射
message PeopleData {
  string name = 1;                    // 主鍵
  string name_original = 2;           // 原始名稱
  string code_name = 3;               // 代號

  // 力量屬性
  int32 physic_power = 4;             // 物理力量
  int32 magic_power = 5;              // 魔法力量
  int32 utility_power = 6;            // 實用能力

  // 基本信息
  string dob = 7;                     // 出生日期
  string race = 8;                    // 種族
  string attributes = 9;              // 屬性
  string gender = 10;                 // 性別

  // 身體特徵
  string ass_size = 11;               // 臀部尺寸
  string boobs_size = 12;             // 胸部尺寸
  int32 height_cm = 13;               // 身高(cm)
  int32 weight_kg = 14;               // 體重(kg)

  // 職業和技能
  string profession = 15;             // 職業
  string combat = 16;                 // 戰鬥能力
  string job = 17;                    // 工作
  string physics = 18;                // 物理特性

  // 個性特徵
  string known_as = 19;               // 別名
  string personality = 20;            // 個性
  string interest = 21;               // 興趣
  string likes = 22;                  // 喜好
  string dislikes = 23;               // 厭惡
  string favorite_foods = 24;         // 喜愛的食物

  // 關係和組織
  string concubine = 25;              // 後宮
  string faction = 26;                // 派系
  int32 army_id = 27;                 // 軍隊編號
  string army_name = 28;              // 軍隊名稱
  int32 dept_id = 29;                 // 部門編號
  string dept_name = 30;              // 部門名稱
  int32 origin_army_id = 31;          // 原始軍隊編號
  string origin_army_name = 32;       // 原始軍隊名稱

  // 其他信息
  bool gave_birth = 33;               // 是否生育
  string email = 34;                  // 電子郵件
  int32 age = 35;                     // 年齡
  string proxy = 36;                  // 代理

  // JSON 屬性
  string base_attributes = 37;        // 基礎屬性(JSON)
  string bonus_attributes = 38;       // 加成屬性(JSON)
  string state_attributes = 39;       // 狀態屬性(JSON)

  // 元數據
  string created_at = 40;             // 創建時間
  string updated_at = 41;             // 更新時間
  int64 version = 42;                 // 版本號(樂觀鎖)
}

// 請求和響應消息
message GetAllPeopleRequest {}

message GetAllPeopleResponse {
  repeated PeopleData people = 1;
}

message GetPeopleByNameRequest {
  string name = 1;
}

message UpdatePeopleRequest {
  string name = 1;
  PeopleData people = 2;
}

message DeletePeopleRequest {
  string name = 1;
}

message PeopleResponse {
  bool success = 1;
  string message = 2;
  PeopleData people = 3;
}

message DeletePeopleResponse {
  bool success = 1;
  string message = 2;
}
```

### 9.2. gRPC 服務實現架構

```mermaid
sequenceDiagram
    participant Client as gRPC Client
    participant Server as gRPC Server
    participant Service as GrpcPeopleServiceImpl
    participant Business as PeopleService
    participant DB as PostgreSQL

    Client->>Server: GetAllPeople(Request)
    Server->>Service: getAllPeople(Request, Observer)
    Service->>Business: getAllPeople()
    Business->>DB: SELECT * FROM people
    DB-->>Business: People List
    Business-->>Service: People List
    Service->>Service: convertToPeopleData()
    Service->>Client: GetAllPeopleResponse
```

**架構說明：**

- **Protocol Buffers**: 使用 `.proto` 文件定義服務介面和數據結構，支援跨語言通訊
- **程式碼生成**: Maven 插件自動生成 Java 類別，包含服務介面和數據模型
- **服務實現**: `GrpcPeopleServiceImpl` 繼承生成的基類並實現業務邏輯
- **服務器配置**: `GrpcServerConfig` 配置並啟動 gRPC 服務器
- **依賴注入**: Spring 容器管理服務實例和依賴關係
- **條件啟用**: 透過 `grpc.enabled=true` 環境變數控制 gRPC 服務啟用
- **錯誤處理**: 統一的異常處理和日誌記錄機制
- **效能優化**: 使用連接池和快取提升服務效能

**技術特點：**
- **雙向通訊**: 支援 Unary、Server Streaming、Client Streaming 和 Bidirectional Streaming
- **類型安全**: 編譯時類型檢查，減少運行時錯誤
- **高效序列化**: Protocol Buffers 提供高效的二進位序列化
- **語言中立**: 支援多種語言實現，方便微服務架構整合
- **服務發現**: 可與服務網格（如 Istio）整合進行服務發現和負載均衡

**環境配置：**
```properties
# 啟用 gRPC 服務
grpc.enabled=true
grpc.port=50051

# 依賴版本
grpc.version=1.58.0
protobuf.version=4.32.1
protoc.version=4.32.1
```

### 9.3. RabbitMQ Data Flow Architecture

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
- Local Environment: `http://localhost:8080/tymb/swagger-ui/index.html#/`
- Production Environment: `https://peoplesystem.tatdvsonorth.com/tymb/swagger-ui/index.html#/`

### JavaDoc Documentation
- Local Environment: `http://localhost:8080/tymb/javadoc/index.html`
- Production Environment: `https://peoplesystem.tatdvsonorth.com/tymb/javadoc/index.html`

### Docker Build
- Build Command: `docker build -t papakao/ty-multiverse-backend:latest .`
- Multi-platform Build: `docker buildx build --platform linux/arm64 -t papakao/ty-multiverse-backend:latest --push .`
