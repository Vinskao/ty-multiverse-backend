# TY-Multiverse-Backend
個人網站後端系統

## 架構設計

### 1. Redis Session 架構
```mermaid
classDiagram
    class RedisConfig {
        +@Configuration
        +@EnableRedisHttpSession
        +redisConnectionFactory()
        +redisTemplate()
        +configureSessionRepository()
    }
    
    class RedisConnectionFactory {
        +LettuceConnectionFactory
        +RedisStandaloneConfiguration
        +RedisPassword
        +RedisSentinelConfiguration
    }
    
    class RedisTemplate {
        +StringRedisTemplate
        +GenericJackson2JsonRedisSerializer
        +RedisSerializer
        +configureSerializers()
    }
    
    class SessionRepository {
        +RedisIndexedSessionRepository
        +SessionEventRegistry
        +SessionRepositoryFilter
        +configureSessionEvents()
    }
    
    class SessionManagement {
        +SessionCreationPolicy
        +SessionTimeout
        +SessionFixation
        +SessionConcurrency
    }
    
    RedisConfig --> RedisConnectionFactory
    RedisConfig --> RedisTemplate
    RedisConfig --> SessionRepository
    SessionRepository --> SessionManagement
```

### 2. Spring Boot 核心架構
```mermaid
classDiagram
    class SpringBootApplication {
        +@SpringBootApplication
        +@ComponentScan
        +@EnableAutoConfiguration
        +@Configuration
    }
    
    class IoCContainer {
        +ApplicationContext
        +BeanFactory
        +DefaultListableBeanFactory
        +AnnotationConfigApplicationContext
    }
    
    class DependencyInjection {
        +@Autowired
        +@Qualifier
        +@Primary
        +Constructor Injection
        +Field Injection
        +Setter Injection
    }
    
    class BeanManagement {
        +Singleton Scope
        +Prototype Scope
        +Request Scope
        +Session Scope
        +@Component
        +@Service
        +@Repository
        +@Controller
    }
    
    class ConfigurationManagement {
        +@Configuration
        +@Bean
        +@ComponentScan
        +@PropertySource
        +@Profile
        +@Conditional
    }
    
    class AspectOriented {
        +@Aspect
        +@Pointcut
        +@Around
        +@Before
        +@After
        +@Transactional
    }
    
    SpringBootApplication --> IoCContainer
    IoCContainer --> DependencyInjection
    IoCContainer --> BeanManagement
    IoCContainer --> ConfigurationManagement
    IoCContainer --> AspectOriented
```

### 3. 領域驅動設計 (DDD) 架構
```mermaid
classDiagram
    class PresentationLayer {
        +REST Controllers
        +WebSocket Controllers
        +Request/Response DTOs
        +API Documentation
        +Authentication
        +Authorization
        +Guardian.java
        +KeycloakController.java
        +MetricsWSController.java
    }
    
    class ApplicationLayer {
        +Application Services
        +Command Handlers
        +Query Handlers
        +Event Handlers
        +Transaction Management
        +Orchestration
        +ScheduledTaskService.java
    }
    
    class DomainLayer {
        +Domain Entities
        +Value Objects
        +Aggregates
        +Domain Services
        +Domain Events
        +Business Rules
        +Repository Interfaces
        +People.java
        +Weapon.java
        +Gallery.java
        +Livestock.java
    }
    
    class InfrastructureLayer {
        +Repository Implementations
        +Database Access
        +External Services
        +Message Brokers
        +File Storage
        +Caching
        +PeopleRepository.java
        +WeaponRepository.java
        +GalleryRepository.java
        +LivestockRepository.java
    }
    
    class CrossCuttingConcerns {
        +Logging
        +Security
        +Validation
        +Error Handling
        +Monitoring
        +Configuration
        +GlobalExceptionHandler.java
        +SecurityConfig.java
        +MetricsConfig.java
    }
    
    PresentationLayer --> ApplicationLayer
    ApplicationLayer --> DomainLayer
    DomainLayer --> InfrastructureLayer
    CrossCuttingConcerns --> PresentationLayer
    CrossCuttingConcerns --> ApplicationLayer
    CrossCuttingConcerns --> DomainLayer
    CrossCuttingConcerns --> InfrastructureLayer
```

### 4. 模組架構圖
```mermaid
classDiagram
    class CoreModule {
        +config/
        +controller/
        +service/
        +exception/
        +util/
    }
    
    class PeopleModule {
        +controller/PeopleController.java
        +service/PeopleService.java
        +service/WeaponDamageService.java
        +dao/PeopleRepository.java
        +domain/vo/People.java
        +strategy/DamageStrategy.java
    }
    
    class WeaponModule {
        +controller/WeaponController.java
        +service/WeaponService.java
        +dao/WeaponRepository.java
        +domain/vo/Weapon.java
    }
    
    class GalleryModule {
        +controller/GalleryController.java
        +service/GalleryService.java
        +dao/GalleryRepository.java
        +domain/vo/Gallery.java
    }
    
    class LivestockModule {
        +controller/LivestockController.java
        +service/LivestockService.java
        +dao/LivestockRepository.java
        +domain/vo/Livestock.java
    }
    
    class CKEditorModule {
        +controller/FileUploadController.java
        +service/EditContentService.java
        +dao/EditContentRepository.java
        +domain/vo/EditContentVO.java
    }
    
    class DeckOfCardsModule {
        +controller/BlackjackController.java
    }
    
    CoreModule --> PeopleModule
    CoreModule --> WeaponModule
    CoreModule --> GalleryModule
    CoreModule --> LivestockModule
    CoreModule --> CKEditorModule
    CoreModule --> DeckOfCardsModule
    PeopleModule --> WeaponModule
```

### 5. 傷害計算策略模式架構
```mermaid
classDiagram
    class DamageStrategy {
        <<interface>>
        +calculateDamage(People, List~Weapon~) int
    }
    
    class DefaultDamageStrategy {
        +calculateDamage(People, List~Weapon~) int
        +safeInt(Integer) int
    }
    
    class DamageStrategyDecorator {
        <<abstract>>
        -DamageStrategy delegate
        +DamageStrategyDecorator(DamageStrategy)
        +calculateDamage(People, List~Weapon~) int
        +safeInt(Integer) int
    }
    
    class BonusAttributeDamageDecorator {
        +calculateDamage(People, List~Weapon~) int
    }
    
    class StateEffectDamageDecorator {
        +@Primary
        +calculateDamage(People, List~Weapon~) int
    }
    
    class WeaponDamageService {
        -DamageStrategy damageStrategy
        +calculateTotalDamage(String) int
    }
    
    DamageStrategy <|.. DefaultDamageStrategy
    DamageStrategy <|.. DamageStrategyDecorator
    DamageStrategyDecorator <|-- BonusAttributeDamageDecorator
    DamageStrategyDecorator <|-- StateEffectDamageDecorator
    BonusAttributeDamageDecorator --> DefaultDamageStrategy
    StateEffectDamageDecorator --> BonusAttributeDamageDecorator
    WeaponDamageService --> DamageStrategy
```

### 6. 設計模式與工廠架構
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

### 7. IoC/AOP 架構
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

## 安全認證架構

### 1. Keycloak JWT 認證架構
```mermaid
sequenceDiagram
    participant Client
    participant Keycloak
    participant Backend
    participant Database
    
    Client->>Keycloak: 登入請求
    Keycloak->>Database: 驗證用戶
    Keycloak-->>Client: 返回 JWT Token
    Client->>Backend: API 請求 + JWT Token
    Backend->>Keycloak: 驗證 JWT Token
    Keycloak-->>Backend: 驗證結果
    Backend-->>Client: API 響應
```

## 錯誤處理架構 (Chain of Responsibility Pattern)
```mermaid
classDiagram
    class ErrorHandlingFramework {
        +Chain of Responsibility
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
        -List~ApiExceptionHandler~ handlerChain
        +handleGlobalException(Exception)
        +handleMethodArgumentNotValid()
        +handleMaxUploadSizeExceededException()
    }
    
    class ApiExceptionHandler {
        <<interface>>
        +canHandle(Exception) boolean
        +handle(Exception, HttpServletRequest) ResponseEntity~ErrorResponse~
    }
    
    class BusinessApiExceptionHandler {
        +@Order(0)
        +canHandle(BusinessException)
        +handle(BusinessException)
    }
    
    class DataIntegrityApiExceptionHandler {
        +@Order(1)
        +canHandle(DataIntegrityViolationException)
        +canHandle(OptimisticLockingFailureException)
        +handle(DataIntegrityViolationException)
        +handle(OptimisticLockingFailureException)
    }
    
    class ValidationApiExceptionHandler {
        +@Order(2)
        +canHandle(MethodArgumentNotValidException)
        +canHandle(ConstraintViolationException)
        +handle(ValidationException)
    }
    
    class DefaultApiExceptionHandler {
        +@Order(Integer.MAX_VALUE)
        +canHandle(Exception)
        +handle(Exception)
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
    ErrorHandlingFramework --> ApiExceptionHandler
    GlobalExceptionHandler --> ApiExceptionHandler
    ApiExceptionHandler <|.. BusinessApiExceptionHandler
    ApiExceptionHandler <|.. DataIntegrityApiExceptionHandler
    ApiExceptionHandler <|.. ValidationApiExceptionHandler
    ApiExceptionHandler <|.. DefaultApiExceptionHandler
    BusinessException --> ErrorCode
    ErrorResponse --> ErrorCode
    GlobalExceptionHandler --> BusinessException
    GlobalExceptionHandler --> ErrorResponse
    GlobalExceptionHandler --> ExceptionMapper
```

#### 2. 處理流程

```mermaid
sequenceDiagram
    participant Client
    participant GlobalExceptionHandler
    participant BusinessHandler
    participant DataIntegrityHandler
    participant ValidationHandler
    participant DefaultHandler
    
    Client->>GlobalExceptionHandler: 拋出異常
    GlobalExceptionHandler->>BusinessHandler: canHandle()?
    alt 業務異常
        BusinessHandler-->>GlobalExceptionHandler: true
        BusinessHandler->>GlobalExceptionHandler: handle()
        GlobalExceptionHandler-->>Client: ErrorResponse
    else 資料完整性異常
        BusinessHandler-->>GlobalExceptionHandler: false
        GlobalExceptionHandler->>DataIntegrityHandler: canHandle()?
        DataIntegrityHandler-->>GlobalExceptionHandler: true
        DataIntegrityHandler->>GlobalExceptionHandler: handle()
        GlobalExceptionHandler-->>Client: ErrorResponse
    else 驗證異常
        DataIntegrityHandler-->>GlobalExceptionHandler: false
        GlobalExceptionHandler->>ValidationHandler: canHandle()?
        ValidationHandler-->>GlobalExceptionHandler: true
        ValidationHandler->>GlobalExceptionHandler: handle()
        GlobalExceptionHandler-->>Client: ErrorResponse
    else 其他異常
        ValidationHandler-->>GlobalExceptionHandler: false
        GlobalExceptionHandler->>DefaultHandler: canHandle()?
        DefaultHandler-->>GlobalExceptionHandler: true
        DefaultHandler->>GlobalExceptionHandler: handle()
        GlobalExceptionHandler-->>Client: ErrorResponse
    end
```

## 監控與健康檢查
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
        +configureMetrics()
        +MeterRegistry
        +DataSource
    }
    
    class MetricsWSController {
        +@Scheduled
        +exportMetrics()
        +WebSocket broadcast
        +DistributedLockUtil
    }
    
    class HikariCPMetrics {
        +connections
        +active
        +idle
        +pending
    }
    
    ActuatorEndpoints --> MetricsConfig
    MetricsConfig --> HikariCPMetrics
    MetricsWSController --> ActuatorEndpoints
```

## 單元測試架構
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
        +PeopleServiceTest
        +WeaponServiceTest
        +GalleryServiceTest
        +LivestockServiceTest
    }
    
    class ControllerTests {
        +@ExtendWith(MockitoExtension)
        +ControllerTest
    }
    
    TestConfig --> RepositoryTests : provides
    RepositoryTests --> ServiceTests : uses
    ServiceTests --> ControllerTests : uses
```

## CI/CD Pipeline
```mermaid
graph LR
    A[Jenkins Pipeline] --> B[Clone Repository]
    B --> C[Build with Maven]
    C --> D[Run Tests]
    D --> E[Build Docker Image with BuildKit]
    E --> F[Debug Environment]
    F --> G[Deploy to Kubernetes]
    
    subgraph "Jenkins Stages"
        B
        C
        D
        E
        F
        G
    end
    
    subgraph "Docker Build"
        E1[Multi-stage build]
        E2[Optimize layers]
        E3[Security scan]
    end
    
    subgraph "Kubernetes Deploy"
        G1[Apply deployment.yaml]
        G2[Health checks]
        G3[Rollback if needed]
    end
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