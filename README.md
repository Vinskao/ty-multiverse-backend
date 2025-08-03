# TY-Multiverse-Backend
個人網站後端系統

## 架構設計

### 1. Redis Session 架構
```mermaid
classDiagram
    class RedisSessionConfig {
        +@Configuration
        +@EnableRedisHttpSession
        +configureRedisConnectionFactory()
        +configureRedisTemplate()
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
    
    RedisSessionConfig --> RedisConnectionFactory
    RedisSessionConfig --> RedisTemplate
    RedisSessionConfig --> SessionRepository
    SessionRepository --> SessionManagement
```

### 2. 核心架構
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

### 4. 傷害計算策略模式架構
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

### 5. 設計模式與工廠架構
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

### 6. IoC/AOP 架構
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

## 錯誤處理架構
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

## 監控與健康檢查
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
    A[GitHub Repository] --> B[Clone and Setup]
    B --> C[Build]
    C --> D[Test]
    D --> E[Build Docker Image with BuildKit]
    E --> F[Debug Environment]
    F --> G[Deploy to Kubernetes]
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