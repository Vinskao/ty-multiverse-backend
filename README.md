# TY-Multiverse-Backend
這是我的個人網站的 Backend。

## 架構設計

### 1. 核心架構
```mermaid
classDiagram
    class ApplicationCore {
        +IoC Container
        +Bean Management
        +Lifecycle Control
    }
    
    class DependencyInjection {
        +@Autowired
        +@Qualifier
        +@Primary
        +Constructor Injection
    }
    
    class BeanManagement {
        +Singleton Scope
        +Prototype Scope
        +Request Scope
        +Session Scope
    }
    
    class AspectOriented {
        +@Aspect
        +@Pointcut
        +@Around
        +@Before
        +@After
    }
    
    class ConfigurationManagement {
        +@Configuration
        +@Bean
        +@ComponentScan
        +@PropertySource
    }
    
    ApplicationCore --> DependencyInjection
    ApplicationCore --> BeanManagement
    ApplicationCore --> AspectOriented
    ApplicationCore --> ConfigurationManagement
```

### 2. 領域驅動設計 (DDD) 架構

#### 2.1 DDD 分層架構
```mermaid
classDiagram
    class PresentationLayer {
        +REST Controllers
        +WebSocket Controllers
        +Request/Response DTOs
        +API Documentation
        +Authentication
        +Authorization
    }
    
    class ApplicationLayer {
        +Application Services
        +Command Handlers
        +Query Handlers
        +Event Handlers
        +Transaction Management
        +Orchestration
    }
    
    class DomainLayer {
        +Domain Entities
        +Value Objects
        +Aggregates
        +Domain Services
        +Domain Events
        +Business Rules
        +Repository Interfaces
    }
    
    class InfrastructureLayer {
        +Repository Implementations
        +Database Access
        +External Services
        +Message Brokers
        +File Storage
        +Caching
    }
    
    class CrossCuttingConcerns {
        +Logging
        +Security
        +Validation
        +Error Handling
        +Monitoring
        +Configuration
    }
    
    PresentationLayer --> ApplicationLayer
    ApplicationLayer --> DomainLayer
    DomainLayer --> InfrastructureLayer
    CrossCuttingConcerns --> PresentationLayer
    CrossCuttingConcerns --> ApplicationLayer
    CrossCuttingConcerns --> DomainLayer
    CrossCuttingConcerns --> InfrastructureLayer
```

- **表現層 (Presentation Layer)**
  - 控制器 (`Controller`) 處理 HTTP 請求
  - 資料傳輸物件 (`DTO`) 用於 API 請求/響應
  - 負責與外部系統的通信

- **應用層 (Application Layer)**
  - 服務類 (`Service`) 協調領域物件
  - 處理事務和協調多個領域物件
  - 不包含業務規則，只負責流程協調

- **領域層 (Domain Layer)**
  - 實體 (`Entity`) 和值物件 (`ValueObject`) 包含業務邏輯
  - 領域服務 (`DomainService`) 處理跨實體的業務邏輯
  - 儲存庫介面 (`Repository`) 定義資料存取契約
  - 聚合根 (`Aggregate`) 確保資料一致性

- **基礎設施層 (Infrastructure Layer)**
  - 儲存庫實現 (`RepositoryImpl`) 提供資料持久化
  - 資料存取器 (`DataAccessor`) 抽象化資料存取
  - 外部服務整合

#### 2.2 領域模型示例
```mermaid
classDiagram
    class DomainEntity {
        <<abstract>>
        +Long id
        +Long version
        +LocalDateTime createdAt
        +LocalDateTime updatedAt
        +equals()
        +hashCode()
        +toString()
    }

    class People {
        +String name
        +String nameOriginal
        +String codeName
        +String race
        +String attributes
        +String baseAttributes
        +String bonusAttributes
        +String stateAttributes
        +Integer physicPower
        +Integer magicPower
        +Integer utilityPower
    }

    class PeopleImage {
        +String id
        +String codeName
        +String image
    }

    class Weapon {
        +String name
        +String owner
        +String attributes
        +Integer baseDamage
        +Integer bonusDamage
        +List~String~ bonusAttributes
        +List~String~ stateAttributes
    }

    class Livestock {
        +Integer id
        +String livestock
        +Double height
        +Double weight
        +Integer melee
        +Integer magicka
        +Integer ranged
        +BigDecimal sellingPrice
        +BigDecimal buyingPrice
        +BigDecimal dealPrice
    }

    class Gallery {
        +Integer id
        +String imageBase64
        +LocalDateTime uploadTime
    }

    class EditContentVO {
        +String editor
        +String content
    }

    class Repository {
        <<interface>>
        +save()
        +findById()
        +findAll()
        +delete()
        +count()
    }

    class PeopleRepository {
        <<interface>>
        +findByName()
        +deleteByName()
        +deleteAllPeople()
    }

    class WeaponRepository {
        <<interface>>
        +findByName()
        +findByAttributes()
        +findByBaseDamageBetween()
        +findByOwner()
    }

    class LivestockRepository {
        <<interface>>
        +findByOwner()
        +findByBuyer()
        +findByLivestock()
    }

    class GalleryRepository {
        <<interface>>
    }

    class EditContentRepository {
        <<interface>>
        +findByEditor()
    }

    DomainEntity <|-- People
    DomainEntity <|-- Weapon
    DomainEntity <|-- Livestock
    DomainEntity <|-- Gallery
    Repository <|-- PeopleRepository
    Repository <|-- WeaponRepository
    Repository <|-- LivestockRepository
    Repository <|-- GalleryRepository
    Repository <|-- EditContentRepository
    PeopleRepository --> People
    WeaponRepository --> Weapon
    LivestockRepository --> Livestock
    GalleryRepository --> Gallery
    EditContentRepository --> EditContentVO
    People "1" -- "*" PeopleImage : images
```

#### 2.3 領域驅動設計原則

1. **統一語言 (Ubiquitous Language)**
   - 在代碼和文檔中使用一致的術語
   - 領域專家、開發人員和業務人員共享相同的語言

2. **限界上下文 (Bounded Context)**
   - 每個模組 (people, weapon, livestock, gallery, deckofcards, ckeditor) 代表一個限界上下文
   - 上下文之間通過明確的介面進行通信

3. **實體與值物件 (Entity vs Value Object)**
   - 實體：具有唯一標識的物件 (如 People, Weapon)
   - 值物件：描述事物特徵的物件，無唯一標識

4. **聚合 (Aggregate)**
   - 聚合根：People, Weapon 等
   - 聚合邊界：確保資料一致性

5. **領域服務 (Domain Service)**
   - 處理跨實體的業務邏輯
   - 不屬於任何單一實體的業務規則

6. **儲存庫 (Repository)**
   - 提供資料持久化抽象
   - 隱藏資料存取細節

7. **工廠 (Factory)**
   - 複雜物件的創建邏輯
   - 確保物件創建的完整性

### 3. 設計模式與工廠架構

#### 3.1 Singleton 模式
```mermaid
classDiagram
    class SpringContainer {
        +ApplicationContext
        +BeanFactory
        +DefaultListableBeanFactory
        +AnnotationConfigApplicationContext
    }
    
    class SingletonRegistry {
        +ConcurrentHashMap~String, Object~
        +registerBean()
        +getBean()
        +containsBean()
    }
    
    class BeanLifecycle {
        +Initialization
        +PostConstruct
        +PreDestroy
        +DependencyInjection
    }
    
    class SingletonBeans {
        +Configuration Beans
        +Service Beans
        +Controller Beans
        +Repository Beans
        +Component Beans
    }
    
    SpringContainer --> SingletonRegistry
    SingletonRegistry --> BeanLifecycle
    BeanLifecycle --> SingletonBeans
```

- **目的**: 確保系統資源的唯一性和一致性
- **應用**: 配置類、服務類、控制器等核心組件
- **優點**: 資源共享、狀態一致性、內存優化

#### 3.2 Factory 模式
```mermaid
classDiagram
    class AbstractFactory {
        <<interface>>
        +createProduct()
        +createProductA()
        +createProductB()
    }
    
    class RepositoryFactory {
        <<interface>>
        +getCustomRepository()
        +getRepository()
        +getSpecificationRepository()
        +getJpaRepository()
    }
    
    class QueryConditionFactory {
        <<interface>>
        +createEqualsCondition()
        +createLikeCondition()
        +createRangeCondition()
        +createCompositeCondition()
        +createInCondition()
        +createNullCondition()
    }
    
    class ServiceFactory {
        <<interface>>
        +createService()
        +createServiceWithDependencies()
        +getServiceInstance()
    }
    
    class ConfigurationFactory {
        <<interface>>
        +createConfiguration()
        +createDataSource()
        +createTransactionManager()
    }
    
    AbstractFactory <|-- RepositoryFactory
    AbstractFactory <|-- QueryConditionFactory
    AbstractFactory <|-- ServiceFactory
    AbstractFactory <|-- ConfigurationFactory
```

- **目的**: 提供統一的對象創建接口
- **應用**: Repository工廠、查詢條件工廠
- **優點**: 解耦對象創建、統一管理實例、支持擴展

#### 3.3 工廠方法實現
```mermaid
classDiagram
    class RepositoryFactoryImpl {
        -ApplicationContext applicationContext
        -Map<Class<?>, Object> repositoryCache
        -ConcurrentHashMap<String, Object> customRepositories
        +getCustomRepository()
        +getRepository()
        +getSpecificationRepository()
        +createRepositoryInstance()
        +cacheRepository()
    }
    
    class QueryConditionFactoryImpl {
        -Map<String, QueryCondition> conditionCache
        +createEqualsCondition()
        +createLikeCondition()
        +createRangeCondition()
        +createCompositeCondition()
        +createDynamicCondition()
        +buildCondition()
        +validateCondition()
    }
    
    class ServiceFactoryImpl {
        -ApplicationContext applicationContext
        -Map<Class<?>, Object> serviceCache
        +createService()
        +createServiceWithDependencies()
        +getServiceInstance()
        +injectDependencies()
    }
    
    class ConfigurationFactoryImpl {
        -Environment environment
        -Properties properties
        +createConfiguration()
        +createDataSource()
        +createTransactionManager()
        +loadProperties()
        +validateConfiguration()
    }
    
    RepositoryFactory <|.. RepositoryFactoryImpl
    QueryConditionFactory <|.. QueryConditionFactoryImpl
    ServiceFactory <|.. ServiceFactoryImpl
    ConfigurationFactory <|.. ConfigurationFactoryImpl
```

#### 3.4 工作原理
```mermaid
sequenceDiagram
    participant Service
    participant Factory
    participant Cache
    participant ApplicationContext
    participant Repository
    
    Service->>Factory: 請求實例
    Factory->>Cache: 檢查緩存
    alt 緩存命中
        Cache-->>Factory: 返回緩存實例
    else 緩存未命中
        Factory->>ApplicationContext: 獲取Bean
        ApplicationContext-->>Factory: 返回Bean
        Factory->>Cache: 存入緩存
    end
    Factory-->>Service: 返回實例
    Service->>Repository: 使用實例
```

- **註冊流程**: Spring容器掃描並註冊工廠Bean
- **實例化**: 按需創建並緩存實例
- **依賴注入**: 通過構造器注入工廠實例
- **使用方式**: 服務層通過工廠獲取所需實例

### 4. IoC/AOP 架構

#### 4.1 IoC 容器
```mermaid
classDiagram
    class SpringContainer {
        +BeanFactory
        +ApplicationContext
        +ConfigurableApplicationContext
    }
    
    class BeanDefinition {
        +Class<?> beanClass
        +String scope
        +boolean lazyInit
        +String[] dependsOn
        +ConstructorArgumentValues
        +PropertyValues
    }
    
    class BeanLifecycle {
        +Instantiation
        +Population
        +Initialization
        +Destruction
        +DependencyInjection
    }
    
    class BeanPostProcessor {
        +postProcessBeforeInitialization()
        +postProcessAfterInitialization()
        +postProcessBeforeInstantiation()
        +postProcessAfterInstantiation()
    }
    
    class DependencyResolver {
        +resolveDependencies()
        +injectDependencies()
        +validateDependencies()
        +createProxy()
    }
    
    SpringContainer --> BeanDefinition
    BeanDefinition --> BeanLifecycle
    BeanLifecycle --> BeanPostProcessor
    BeanPostProcessor --> DependencyResolver
```

#### 4.2 AOP 切面
```mermaid
classDiagram
    class AOPFramework {
        +AspectJ
        +Spring AOP
        +Proxy Creation
        +Weaving
    }
    
    class AspectDefinition {
        +@Aspect
        +@Pointcut
        +@Around
        +@Before
        +@After
        +@AfterReturning
        +@AfterThrowing
    }
    
    class TransactionAspect {
        +@Transactional
        +TransactionManager
        +TransactionDefinition
        +TransactionStatus
        +beginTransaction()
        +commitTransaction()
        +rollbackTransaction()
    }
    
    class ExceptionAspect {
        +@ControllerAdvice
        +@ExceptionHandler
        +GlobalExceptionHandler
        +handleException()
        +logException()
        +createErrorResponse()
    }
    
    class ObservationAspect {
        +ObservationHandler
        +Metrics
        +Tracing
        +Performance Monitoring
        +recordMetrics()
        +startTimer()
        +stopTimer()
    }
    
    class SecurityAspect {
        +@PreAuthorize
        +@PostAuthorize
        +@Secured
        +Authentication
        +Authorization
        +validateAccess()
        +checkPermissions()
    }
    
    class ValidationAspect {
        +@Valid
        +@Validated
        +ConstraintValidator
        +ValidationResult
        +validateInput()
        +handleValidationErrors()
    }
    
    AOPFramework --> AspectDefinition
    AspectDefinition --> TransactionAspect
    AspectDefinition --> ExceptionAspect
    AspectDefinition --> ObservationAspect
    AspectDefinition --> SecurityAspect
    AspectDefinition --> ValidationAspect
```

### 5. 架構優勢

1. **解耦與內聚**
   - IoC 實現依賴反轉
   - AOP 處理橫切關注點
   - 工廠模式管理對象創建

2. **可維護性**
   - 清晰的職責分離
   - 統一的異常處理
   - 集中的配置管理

3. **可擴展性**
   - 模塊化設計
   - 接口導向
   - 鬆散耦合

4. **性能優化**
   - 單例資源共享
   - 工廠對象緩存
   - AOP 性能監控

## 錯誤處理架構

### 1. 錯誤處理架構圖
```mermaid
classDiagram
    class ErrorHandlingFramework {
        +Global Exception Handler
        +Error Response Builder
        +Exception Mapper
        +Error Logger
    }
    
    class ErrorCode {
        <<enumeration>>
        +INTERNAL_SERVER_ERROR
        +BAD_REQUEST
        +NOT_FOUND
        +UNAUTHORIZED
        +FORBIDDEN
        +CONFLICT
        +VALIDATION_ERROR
        +BUSINESS_ERROR
        +getCode()
        +getHttpStatus()
        +getMessage()
        +getDescription()
    }
    
    class BusinessException {
        -ErrorCode errorCode
        -String detail
        -Map<String, Object> parameters
        +BusinessException(ErrorCode)
        +BusinessException(ErrorCode, String)
        +BusinessException(ErrorCode, String, Throwable)
        +getErrorCode()
        +getDetail()
        +getParameters()
    }
    
    class ErrorResponse {
        -int code
        -String message
        -String detail
        -LocalDateTime timestamp
        -String path
        -String traceId
        -Map<String, Object> metadata
        +ErrorResponse(int, String, String, String)
        +fromErrorCode(ErrorCode, String, String)
        +fromBusinessException(BusinessException, String)
        +addMetadata()
        +setTraceId()
    }
    
    class GlobalExceptionHandler {
        -Logger logger
        -ErrorResponseBuilder responseBuilder
        +handleBusinessException()
        +handleEntityNotFoundException()
        +handleDataIntegrityViolationException()
        +handleOptimisticLockingFailureException()
        +handleMethodArgumentNotValidException()
        +handleConstraintViolationException()
        +handleBindException()
        +handleGlobalException()
        +logException()
        +createErrorResponse()
    }
    
    class ExceptionMapper {
        +mapToErrorCode(Exception)
        +mapToHttpStatus(Exception)
        +mapToErrorMessage(Exception)
        +isBusinessException(Exception)
        +isSystemException(Exception)
    }
    
    ErrorHandlingFramework --> ErrorCode
    ErrorHandlingFramework --> BusinessException
    ErrorHandlingFramework --> ErrorResponse
    ErrorHandlingFramework --> GlobalExceptionHandler
    ErrorHandlingFramework --> ExceptionMapper
    BusinessException --> ErrorCode
    ErrorResponse --> ErrorCode
    GlobalExceptionHandler --> BusinessException
    GlobalExceptionHandler --> ErrorResponse
    GlobalExceptionHandler --> ExceptionMapper
```

### 2. 模組特定異常
```mermaid
classDiagram
    class BusinessException {
        <<abstract>>
        -ErrorCode errorCode
        -String detail
        -Map<String, Object> parameters
        +BusinessException(ErrorCode)
        +BusinessException(ErrorCode, String)
        +BusinessException(ErrorCode, String, Throwable)
        +getErrorCode()
        +getDetail()
        +getParameters()
    }
    
    class PeopleException {
        -String peopleName
        -Long peopleId
        +PeopleException(ErrorCode)
        +PeopleException(ErrorCode, String)
        +PeopleException(ErrorCode, String, Throwable)
        +setPeopleInfo()
        +getPeopleName()
        +getPeopleId()
    }
    
    class WeaponException {
        -String weaponName
        -String weaponType
        +WeaponException(ErrorCode)
        +WeaponException(ErrorCode, String)
        +WeaponException(ErrorCode, String, Throwable)
        +setWeaponInfo()
        +getWeaponName()
        +getWeaponType()
    }
    
    class LivestockException {
        -String livestockName
        -String species
        +LivestockException(ErrorCode)
        +LivestockException(ErrorCode, String)
        +LivestockException(ErrorCode, String, Throwable)
        +setLivestockInfo()
        +getLivestockName()
        +getSpecies()
    }
    
    class GalleryException {
        -String galleryTitle
        -String category
        +GalleryException(ErrorCode)
        +GalleryException(ErrorCode, String)
        +GalleryException(ErrorCode, String, Throwable)
        +setGalleryInfo()
        +getGalleryTitle()
        +getCategory()
    }
    
    class ValidationException {
        -List<String> validationErrors
        -String fieldName
        +ValidationException(ErrorCode)
        +ValidationException(ErrorCode, String)
        +addValidationError()
        +getValidationErrors()
        +getFieldName()
    }
    
    BusinessException <|-- PeopleException
    BusinessException <|-- WeaponException
    BusinessException <|-- LivestockException
    BusinessException <|-- GalleryException
    BusinessException <|-- ValidationException
```

### 3. 錯誤處理流程
```mermaid
sequenceDiagram
    participant Client
    participant Controller
    participant Service
    participant Repository
    participant Exception
    participant GlobalExceptionHandler
    participant ErrorResponse
    participant Logger
    
    Client->>Controller: HTTP Request
    Controller->>Service: 調用服務方法
    Service->>Repository: 數據庫操作
    Repository-->>Service: 返回結果/異常
    alt 發生異常
        Service->>Exception: 拋出業務異常
        Exception-->>Controller: 異常傳播
        Controller->>GlobalExceptionHandler: 捕獲異常
        GlobalExceptionHandler->>Logger: 記錄異常
        GlobalExceptionHandler->>ErrorResponse: 創建錯誤響應
        GlobalExceptionHandler-->>Client: 返回錯誤響應
    else 正常流程
        Service-->>Controller: 返回結果
        Controller-->>Client: 返回成功響應
    end
```

### 4. 錯誤處理優點

1. **統一錯誤格式**
   - 所有錯誤響應格式一致
   - 前端可以統一處理

2. **標準化錯誤碼**
   - 使用標準 HTTP 狀態碼

3. **詳細錯誤信息**
   - 包含錯誤代碼、消息和詳情
   - 記錄錯誤發生時間和路徑

4. **模組化設計**
   - 每個模組有自己的異常類

## 監控與健康檢查

### 1. Actuator 端點

應用程式提供了以下 Actuator 端點用於監控：

#### 1.1 健康檢查
```
GET https://***/tymb/actuator/health
```
提供應用程式的健康狀態，包括：
- 應用程式狀態
- 資料庫連接狀態
- 磁碟空間
- 其他組件狀態

#### 1.2 指標信息
```
GET https://***/tymb/actuator/metrics
```
提供所有可用的指標列表，包括：
- JVM 指標
- 系統指標
- 應用程式指標
- 自定義指標

#### 1.3 HikariCP 連接池指標
```
GET https://***/tymb/actuator/metrics/hikaricp.connections
GET https://***/tymb/actuator/metrics/hikaricp.connections.active
GET https://***/tymb/actuator/metrics/hikaricp.connections.idle
GET https://***/tymb/actuator/metrics/hikaricp.connections.pending
```
提供連接池的詳細狀態：
- 活動連接數
- 空閒連接數
- 等待連接數
- 連接獲取時間
- 連接使用時間
- 連接泄漏檢測

#### 1.4 Prometheus 格式指標
```
GET https://***/tymb/actuator/prometheus
```
提供 Prometheus 格式的指標數據，可用於：
- 指標收集
- 監控面板
- 警報設置

### 2. 監控架構
```mermaid
classDiagram
    class ActuatorEndpoints {
        +/health
        +/metrics
        +/prometheus
    }
    
    class MetricsConfig {
        +configureMetrics()
        +MeterRegistry
        +DataSource
    }
    
    class HikariCPMetrics {
        +connections
        +active
        +idle
        +pending
    }
    
    ActuatorEndpoints --> MetricsConfig
    MetricsConfig --> HikariCPMetrics
```

### 4. 使用建議

1. **健康檢查**
   - 定期檢查應用程式健康狀態
   - 設置自動化監控
   - 配置警報閾值

2. **性能監控**
   - 監控關鍵指標
   - 分析性能瓶頸
   - 優化資源使用

3. **問題診斷**
   - 使用指標追蹤問題
   - 分析錯誤模式
   - 預防系統故障

## swagger ui

```bash
http://localhost:8080/tymb/swagger-ui/index.html#/
https://peoplesystem.tatdvsonorth.com/tymb/swagger-ui/index.html#/
```

## image 建置

```bash
# Build image
mvn clean package -DskipTests
docker buildx build --platform linux/arm64 -t papakao/ty-multiverse-backend:latest --push .
mvn -P platform install
docker build -t papakao/ty-multiverse-backend:latest .
docker push papakao/ty-multiverse-backend:latest

mvn -P platform install
docker buildx build --platform linux/arm64 -t papakao/ty-multiverse-backend:latest --push .
docker push papakao/ty-multiverse-backend:latest

mvn -P platform install
docker build -t ty-multiverse-backend .
docker run -d --name ty-multiverse-backend `
  -e "SPRING_PROFILES_ACTIVE=platform" `
  -e "URL_BACKEND=http://localhost:8080/tymb" `
  -e "SPRING_DATASOURCE_URL=jdbc:postgresql://*****:****/peoplesystem" `
  -e "SPRING_DATASOURCE_USERNAME=w*****o" `
  -e "SPRING_DATASOURCE_PASSWORD=W*****=" `
  -p 8080:8080 `
  ty-multiverse-backend

# Docker Agent
docker build -t papakao/maven-docker-agent:latest -f Dockerfile.agent .
docker push papakao/maven-docker-agent:latest
```

### 3. 並發控制

#### 3.1 樂觀鎖定
```mermaid
classDiagram
    class Entity {
        +@Version
        +version: Long
    }
    
    class Service {
        +updateEntity()
        +handleOptimisticLocking()
    }
    
    class ExceptionHandler {
        +handleOptimisticLockingFailure()
    }
    
    Entity --> Service
    Service --> ExceptionHandler
```

#### 3.2 樂觀鎖定機制
- **實現方式**: 使用 `@Version` 註解
- **觸發時機**: 並發更新衝突
- **處理策略**: 
  - 自動重試
  - 返回衝突狀態
  - 提示用戶刷新

## 單元測試架構

### 1. 測試架構概述
```mermaid
classDiagram
    class TestConfig {
        +@TestConfiguration
        +dataSource() DataSource
    }
    
    class RepositoryTests {
        +@DataJpaTest
        +RepositoryTest
    }
    
    class ServiceTests {
        +@ExtendWith(MockitoExtension)
        +ServiceTest
    }
    
    class ControllerTests {
        +@ExtendWith(MockitoExtension)
        +ControllerTest
    }
    
    TestConfig --> RepositoryTests : provides
    RepositoryTests --> ServiceTests : uses
    ServiceTests --> ControllerTests : uses
```

### 2. 測試分層設計

#### 2.1 配置層 (Configuration)
- **TestConfig.java**
  - 提供測試環境的基礎配置
  - 使用 H2 內存數據庫
  - 通過 `@TestConfiguration` 和 `@Primary` 確保測試環境隔離
  - 配置測試專用的數據源和事務管理器

#### 2.2 數據訪問層測試 (Repository Tests)
- **PeopleRepositoryTest.java**
  - 使用 `@DataJpaTest` 進行 JPA 相關測試
  - 測試基本的 CRUD 操作
  - 驗證數據庫操作的正確性
  - 使用 H2 內存數據庫進行測試

#### 2.3 服務層測試 (Service Tests)
- **PeopleServiceTest.java**
  - 使用 Mockito 進行依賴模擬
  - 測試業務邏輯的完整性
  - 驗證服務層方法的行為
  - 處理異常情況的測試

#### 2.4 控制器層測試 (Controller Tests)
- **PeopleControllerTest.java**
  - 測試 API 接口的正確性
  - 驗證 HTTP 響應和狀態碼
  - 測試請求參數的處理
  - 驗證服務層的調用

## CI/CD Pipeline

### 1. Pipeline Overview
```mermaid
graph LR
    A[GitHub Repository] --> B[Clone and Setup]
    B --> C[Build]
    C --> D[Test]
    D --> E[Build Docker Image with BuildKit]
    E --> F[Debug Environment]
    F --> G[Deploy to Kubernetes]
    

    

```

### 2. Pipeline Components

#### 2.1 Agent Configuration
- **Maven Container**: `maven:3.8.4-openjdk-17`
- **Docker Container**: `docker:23-dind`
- **Kubectl Container**: `bitnami/kubectl:1.30.7`