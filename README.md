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

### 6. Spring IoC 工廠模式架構
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

### 1. Spring Security Filter Chain 架構
```mermaid
classDiagram
    class SecurityFilterChain {
        <<interface>>
        +getFilters() List~Filter~
        +matches(HttpServletRequest) boolean
    }
    
    class HttpSecurity {
        +authorizeHttpRequests()
        +oauth2ResourceServer()
        +csrf()
        +sessionManagement()
        +build() SecurityFilterChain
    }
    
    class SecurityConfig {
        +@Configuration
        +@EnableWebSecurity
        +@EnableMethodSecurity
        +securityFilterChain(HttpSecurity)
        +jwtAuthenticationConverter()
        +corsConfigurer()
        +passwordEncoder()
    }
    
    class JwtAuthenticationConverter {
        +setJwtGrantedAuthoritiesConverter()
        +convert(Jwt) Authentication
    }
    
    class CustomJwtGrantedAuthoritiesConverter {
        +convert(Jwt) Collection~GrantedAuthority~
        +processRealmRoles()
        +processResourceRoles()
        +addGuestRole()
    }
    
    class FilterChain {
        +CsrfFilter
        +CorsFilter
        +OAuth2ResourceServerFilter
        +AuthorizationFilter
        +SessionManagementFilter
    }
    
    class AuthorizationRules {
        +/javadoc/** permitAll
        +/static/** permitAll
        +/guardian/admin hasRole("manage-users")
        +/guardian/user authenticated
        +/guardian/token-info authenticated
        +/guardian/test-default authenticated
        +anyRequest() permitAll
    }
    
    class OAuth2ResourceServer {
        +JWT Token Validation
        +JWK Set URI
        +JWT Authentication Converter
        +Keycloak Integration
    }
    
    class SessionManagement {
        +SessionCreationPolicy.STATELESS
        +No Session Creation
        +JWT-based Authentication
    }
    
    SecurityConfig --> HttpSecurity
    HttpSecurity --> SecurityFilterChain
    SecurityFilterChain --> FilterChain
    SecurityConfig --> JwtAuthenticationConverter
    JwtAuthenticationConverter --> CustomJwtGrantedAuthoritiesConverter
    HttpSecurity --> AuthorizationRules
    HttpSecurity --> OAuth2ResourceServer
    HttpSecurity --> SessionManagement
```

### 2. Spring Security Filter Chain 處理流程
```mermaid
sequenceDiagram
    participant Client
    participant CsrfFilter
    participant CorsFilter
    participant OAuth2ResourceServerFilter
    participant JwtAuthenticationConverter
    participant AuthorizationFilter
    participant Controller
    
    Client->>CsrfFilter: HTTP Request
    Note over CsrfFilter: CSRF 保護 (已禁用)
    CsrfFilter->>CorsFilter: 請求通過
    Note over CorsFilter: CORS 預檢處理
    CorsFilter->>OAuth2ResourceServerFilter: 請求通過
    Note over OAuth2ResourceServerFilter: JWT Token 驗證
    OAuth2ResourceServerFilter->>JwtAuthenticationConverter: 轉換 JWT
    Note over JwtAuthenticationConverter: 提取角色權限
    JwtAuthenticationConverter->>AuthorizationFilter: 認證信息
    Note over AuthorizationFilter: 權限檢查
    alt 需要認證
        AuthorizationFilter->>Controller: 已認證請求
    else 公開端點
        AuthorizationFilter->>Controller: 直接通過
    end
    Controller-->>Client: HTTP Response
```

### 3. JWT 權限轉換流程
```mermaid
flowchart TD
    A[JWT Token] --> B{檢查 realm_access.roles}
    B -->|存在| C[提取基本角色]
    B -->|不存在| D{檢查 resource_access}
    C --> E[轉換為 ROLE_ 格式]
    D -->|存在| F[提取 realm-management.roles]
    D -->|不存在| G[添加 ROLE_GUEST]
    F --> H[轉換為 ROLE_ 格式]
    E --> I{檢查是否有 manage-users}
    H --> I
    G --> I
    I -->|有| J[保留 manage-users 權限]
    I -->|沒有| K[添加 ROLE_GUEST]
    J --> L[返回權限集合]
    K --> L
    L --> M[Spring Security Authorities]
```

### 4. Spring Security 組件關係圖
```mermaid
classDiagram
    class SecurityConfig {
        +@Configuration
        +@EnableWebSecurity
        +@EnableMethodSecurity
        +securityFilterChain()
        +jwtAuthenticationConverter()
        +corsConfigurer()
        +passwordEncoder()
    }
    
    class HttpSecurity {
        +authorizeHttpRequests()
        +oauth2ResourceServer()
        +csrf()
        +sessionManagement()
        +build()
    }
    
    class SecurityFilterChain {
        <<interface>>
        +getFilters()
        +matches()
    }
    
    class JwtAuthenticationConverter {
        +setJwtGrantedAuthoritiesConverter()
        +convert()
    }
    
    class CustomJwtGrantedAuthoritiesConverter {
        +convert(Jwt)
        +processRealmRoles()
        +processResourceRoles()
        +addGuestRole()
    }
    
    class WebMvcConfigurer {
        <<interface>>
        +addCorsMappings()
    }
    
    class PasswordEncoder {
        <<interface>>
        +encode()
        +matches()
    }
    
    class BCryptPasswordEncoder {
        +encode(String)
        +matches(String, String)
    }
    
    class CorsRegistry {
        +addMapping()
        +allowedOrigins()
        +allowedMethods()
        +allowedHeaders()
    }
    
    class AuthorizationManager {
        +check()
        +verify()
    }
    
    class SessionManagement {
        +sessionCreationPolicy()
        +maximumSessions()
        +sessionFixation()
    }
    
    SecurityConfig --> HttpSecurity
    SecurityConfig --> JwtAuthenticationConverter
    SecurityConfig --> WebMvcConfigurer
    SecurityConfig --> PasswordEncoder
    HttpSecurity --> SecurityFilterChain
    JwtAuthenticationConverter --> CustomJwtGrantedAuthoritiesConverter
    WebMvcConfigurer --> CorsRegistry
    PasswordEncoder <|.. BCryptPasswordEncoder
    HttpSecurity --> AuthorizationManager
    HttpSecurity --> SessionManagement
```

### 5. Spring Security Filter Chain 完整架構
```mermaid
classDiagram
    class SecurityFilterChain {
        <<interface>>
        +getFilters() List~Filter~
        +matches(HttpServletRequest) boolean
    }
    
    class HttpSecurity {
        +csrf()
        +authorizeHttpRequests()
        +oauth2ResourceServer()
        +sessionManagement()
        +exceptionHandling()
        +logout()
        +build() SecurityFilterChain
    }
    
    class SecurityConfig {
        +@Configuration
        +@EnableWebSecurity
        +@EnableMethodSecurity
        +securityFilterChain()
        +authenticationEntryPoint()
        +accessDeniedHandler()
        +logoutSuccessHandler()
        +jwtAuthenticationConverter()
        +corsConfigurer()
        +passwordEncoder()
    }
    
    class FilterChain {
        +CsrfFilter (已禁用)
        +CorsFilter
        +SecurityContextPersistenceFilter
        +OAuth2ResourceServerFilter
        +AuthorizationFilter
        +SessionManagementFilter
        +LogoutFilter
    }
    
    class AuthenticationEntryPoint {
        +commence(HttpServletRequest, HttpServletResponse, AuthenticationException)
        +handleUnauthorized()
        +returnErrorResponse()
    }
    
    class AccessDeniedHandler {
        +handle(HttpServletRequest, HttpServletResponse, AccessDeniedException)
        +handleForbidden()
        +returnErrorResponse()
    }
    
    class LogoutSuccessHandler {
        +onLogoutSuccess(HttpServletRequest, HttpServletResponse, Authentication)
        +handleLogoutSuccess()
        +returnSuccessResponse()
    }
    
    class ErrorHandlerModule {
        +GlobalExceptionHandler
        +BusinessApiExceptionHandler
        +DataIntegrityApiExceptionHandler
        +ValidationApiExceptionHandler
        +DefaultApiExceptionHandler
    }
    
    SecurityConfig --> HttpSecurity
    HttpSecurity --> SecurityFilterChain
    SecurityFilterChain --> FilterChain
    SecurityConfig --> AuthenticationEntryPoint
    SecurityConfig --> AccessDeniedHandler
    SecurityConfig --> LogoutSuccessHandler
    AuthenticationEntryPoint --> ErrorHandlerModule
    AccessDeniedHandler --> ErrorHandlerModule
    LogoutSuccessHandler --> ErrorHandlerModule
```

### 6. Spring Security 主流 Filter 流程
```mermaid
sequenceDiagram
    participant Client
    participant CsrfFilter
    participant CorsFilter
    participant SecurityContextPersistenceFilter
    participant OAuth2ResourceServerFilter
    participant JwtAuthenticationConverter
    participant AuthorizationFilter
    participant SessionManagementFilter
    participant LogoutFilter
    participant Controller
    participant ErrorHandlerModule
    
    Client->>CsrfFilter: HTTP Request
    Note over CsrfFilter: CSRF 保護 (已禁用)
    CsrfFilter->>CorsFilter: 請求通過
    Note over CorsFilter: CORS 預檢處理
    CorsFilter->>SecurityContextPersistenceFilter: 請求通過
    Note over SecurityContextPersistenceFilter: 安全上下文持久化
    SecurityContextPersistenceFilter->>OAuth2ResourceServerFilter: 請求通過
    Note over OAuth2ResourceServerFilter: JWT Token 驗證
    OAuth2ResourceServerFilter->>JwtAuthenticationConverter: 轉換 JWT
    Note over JwtAuthenticationConverter: 提取角色權限
    JwtAuthenticationConverter->>AuthorizationFilter: 認證信息
    Note over AuthorizationFilter: 權限檢查
    alt 認證失敗
        AuthorizationFilter->>AuthenticationEntryPoint: 觸發認證失敗
        AuthenticationEntryPoint->>ErrorHandlerModule: 返回 401 錯誤
        ErrorHandlerModule-->>Client: 認證失敗響應
    else 授權失敗
        AuthorizationFilter->>AccessDeniedHandler: 觸發授權失敗
        AccessDeniedHandler->>ErrorHandlerModule: 返回 403 錯誤
        ErrorHandlerModule-->>Client: 授權失敗響應
    else 登出請求
        AuthorizationFilter->>LogoutFilter: 處理登出
        LogoutFilter->>LogoutSuccessHandler: 登出成功
        LogoutSuccessHandler->>ErrorHandlerModule: 返回登出響應
        ErrorHandlerModule-->>Client: 登出成功響應
    else 正常請求
        AuthorizationFilter->>SessionManagementFilter: 會話管理
        SessionManagementFilter->>Controller: 已認證請求
        Controller-->>Client: HTTP Response
    end
```

### 7. Spring Security Filter 處理順序
```mermaid
flowchart TD
    A[HTTP Request] --> B[CsrfFilter]
    B --> C[CorsFilter]
    C --> D[SecurityContextPersistenceFilter]
    D --> E[OAuth2ResourceServerFilter]
    E --> F[JwtAuthenticationConverter]
    F --> G[AuthorizationFilter]
    G --> H{權限檢查}
    H -->|認證失敗| I[AuthenticationEntryPoint]
    H -->|授權失敗| J[AccessDeniedHandler]
    H -->|登出請求| K[LogoutFilter]
    H -->|正常請求| L[SessionManagementFilter]
    L --> M[Controller]
    I --> N[ErrorHandlerModule]
    J --> N
    K --> O[LogoutSuccessHandler]
    O --> N
    N --> P[HTTP Response]
    M --> P
```

### 8. Spring Security 常量枚舉架構
```mermaid
classDiagram
    class SecurityConstants {
        <<enumeration>>
        +AUTHENTICATION_FAILED
        +TOKEN_EXPIRED
        +TOKEN_INVALID
        +TOKEN_MISSING
        +AUTHORIZATION_FAILED
        +INSUFFICIENT_PERMISSIONS
        +ROLE_REQUIRED
        +LOGOUT_SUCCESS
        +LOGOUT_FAILED
        +SESSION_EXPIRED
        +SESSION_INVALID
        +CSRF_DISABLED
        +CORS_ENABLED
        +ROLE_GUEST
        +ROLE_MANAGE_USERS
        +ROLE_ADMIN
        +ROLE_USER
        +getMessage() String
        +getCode() String
        +getHttpStatus() HttpStatus
        +isRole() boolean
        +findByCode(String) SecurityConstants
        +findByHttpStatus(HttpStatus) List
        +getAuthenticationErrors() List
        +getAuthorizationErrors() List
        +getRoles() List
    }
    
    class SecurityConfig {
        +@Configuration
        +@EnableWebSecurity
        +@EnableMethodSecurity
        +securityFilterChain()
        +authenticationEntryPoint()
        +accessDeniedHandler()
        +logoutSuccessHandler()
    }
    
    class AuthenticationEntryPoint {
        +commence()
        +handleUnauthorized()
    }
    
    class AccessDeniedHandler {
        +handle()
        +handleForbidden()
    }
    
    class LogoutSuccessHandler {
        +onLogoutSuccess()
        +handleLogoutSuccess()
    }
    
    class CustomJwtGrantedAuthoritiesConverter {
        +convert(Jwt)
        +processRealmRoles()
        +processResourceRoles()
        +addGuestRole()
    }
    
    SecurityConfig --> SecurityConstants
    AuthenticationEntryPoint --> SecurityConstants
    AccessDeniedHandler --> SecurityConstants
    LogoutSuccessHandler --> SecurityConstants
    CustomJwtGrantedAuthoritiesConverter --> SecurityConstants
```

### 9. SecurityConstants 枚舉分類
```mermaid
graph TB
    subgraph "認證相關 AUTH_*"
        A1[AUTHENTICATION_FAILED]
        A2[TOKEN_EXPIRED]
        A3[TOKEN_INVALID]
        A4[TOKEN_MISSING]
    end
    
    subgraph "授權相關 AUTHZ_*"
        B1[AUTHORIZATION_FAILED]
        B2[INSUFFICIENT_PERMISSIONS]
        B3[ROLE_REQUIRED]
    end
    
    subgraph "登出相關 LOGOUT_*"
        C1[LOGOUT_SUCCESS]
        C2[LOGOUT_FAILED]
    end
    
    subgraph "會話相關 SESSION_*"
        D1[SESSION_EXPIRED]
        D2[SESSION_INVALID]
    end
    
    subgraph "配置相關 CONFIG_*"
        E1[CSRF_DISABLED]
        E2[CORS_ENABLED]
    end
    
    subgraph "角色相關 ROLE_*"
        F1[ROLE_GUEST]
        F2[ROLE_MANAGE_USERS]
        F3[ROLE_ADMIN]
        F4[ROLE_USER]
    end
```

### 10. OAuth2 Resource Server Token 提取流程
```mermaid
sequenceDiagram
    participant Client
    participant OAuth2ResourceServerFilter
    participant BearerTokenAuthenticationFilter
    participant JwtDecoder
    participant JwtAuthenticationConverter
    participant CustomJwtGrantedAuthoritiesConverter
    participant SecurityContextHolder
    
    Client->>OAuth2ResourceServerFilter: HTTP Request + Authorization Header
    Note over OAuth2ResourceServerFilter: 1. 檢查是否需要認證
    
    alt 需要認證的端點
        OAuth2ResourceServerFilter->>BearerTokenAuthenticationFilter: 提取 Bearer Token
        Note over BearerTokenAuthenticationFilter: 2. 從 Authorization 頭提取 Token
        
        BearerTokenAuthenticationFilter->>JwtDecoder: 驗證 JWT Token
        Note over JwtDecoder: 3. 使用 JWK Set URI 驗證簽名和有效性
        
        JwtDecoder->>JwtAuthenticationConverter: 轉換為 Authentication
        Note over JwtAuthenticationConverter: 4. 創建 JwtAuthenticationToken
        
        JwtAuthenticationConverter->>CustomJwtGrantedAuthoritiesConverter: 提取權限
        Note over CustomJwtGrantedAuthoritiesConverter: 5. 處理 realm_access.roles 和 resource_access
        
        CustomJwtGrantedAuthoritiesConverter-->>JwtAuthenticationConverter: 權限集合
        JwtAuthenticationConverter-->>BearerTokenAuthenticationFilter: 認證對象
        BearerTokenAuthenticationFilter->>SecurityContextHolder: 設置認證上下文
        SecurityContextHolder-->>OAuth2ResourceServerFilter: 認證完成
    else 公開端點
        OAuth2ResourceServerFilter-->>Client: 直接通過
    end
```

### 11. OAuth2 架構角色關係
```mermaid
graph LR
    A[Client<br/>前端應用] --> B[Authorization Server<br/>Keycloak]
    B --> A[Access Token<br/>JWT]
    A --> C[Resource Server<br/>Your Backend]
    C --> A[Protected Resources<br/>API 端點]
    
    subgraph "Token 驗證流程"
        D[Bearer Token<br/>Authorization Header] --> E[JWT Decoder<br/>簽名驗證]
        E --> F[JWT Converter<br/>權限提取]
        F --> G[Security Context<br/>認證上下文]
    end
    
    subgraph "權限管理"
        H[realm_access.roles<br/>基本角色] --> I[ROLE_ 前綴<br/>Spring Security]
        J[resource_access<br/>管理角色] --> I
        I --> K[Authorization Filter<br/>授權決策]
    end
```

### 12. 混合認證架構 (Session + JWT)
```mermaid
classDiagram
    class SecurityConfig {
        +@EnableWebSecurity
        +@EnableMethodSecurity
        +securityFilterChain()
        +混合認證策略
    }
    
    class SessionConfig {
        +@ConditionalOnProperty
        +@EnableRedisHttpSession
        +cookieSerializer()
    }
    
    class StatelessServices {
        +People CRUD
        +Weapon CRUD
        +Gallery 文件管理
        +Livestock 數據管理
        +JWT Token 認證
    }
    
    class StatefulServices {
        +CKEditor 編輯狀態
        +DeckOfCards 遊戲狀態
        +Session 認證
    }
    
    class AuthenticationStrategy {
        +JWT 認證
        +Session 認證
        +混合路由
    }
    
    SecurityConfig --> AuthenticationStrategy
    SessionConfig --> StatefulServices
    AuthenticationStrategy --> StatelessServices
    AuthenticationStrategy --> StatefulServices
```

### 13. Session 服務架構
```mermaid
sequenceDiagram
    participant Client
    participant SessionFilter
    participant CKEditor
    participant DeckOfCards
    participant Redis
    
    Client->>SessionFilter: HTTP Request + Session ID
    SessionFilter->>Redis: 驗證 Session
    Redis-->>SessionFilter: Session 數據
    
    alt CKEditor 請求
        SessionFilter->>CKEditor: 編輯狀態管理
        CKEditor->>Redis: 保存草稿
        Redis-->>CKEditor: 確認保存
        CKEditor-->>Client: 編輯結果
    else DeckOfCards 請求
        SessionFilter->>DeckOfCards: 遊戲狀態管理
        DeckOfCards->>Redis: 保存遊戲狀態
        Redis-->>DeckOfCards: 確認保存
        DeckOfCards-->>Client: 遊戲結果
    end
```

### 14. Keycloak JWT 認證架構
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

### 15. Redis 數據結構與存儲流程
```mermaid
graph TB
    subgraph "Redis 存儲架構"
        A[Session 數據] --> B[tymb:sessions:sessions:*]
        C[Session 過期] --> D[tymb:sessions:expires:*]
        E[分布式鎖] --> F[lock:*]
        G[過期時間] --> H[TTL 機制]
    end
    
    subgraph "數據類型"
        I[Session 屬性] --> J[JSON 格式]
        K[鎖值] --> L[UUID 字符串]
        M[過期時間] --> N[TTL 秒數]
    end
    
    subgraph "應用組件"
        O[CKEditor] --> P[Session 存儲]
        Q[DeckOfCards] --> R[Session 存儲]
        S[DistributedLockUtil] --> T[鎖存儲]
    end
    
    P --> A
    R --> A
    T --> E
```

### 16. Redis Session 數據存儲結構
```mermaid
flowchart TD
    A[應用層存儲] --> B[Spring Session 處理]
    B --> C[Redis 序列化]
    C --> D[Redis 存儲]
    
    subgraph "Session 數據示例"
        E[tymb:sessions:sessions:abc123] --> F[{
          "creationTime": 1640995200000,
          "lastAccessedTime": 1640995260000,
          "maxInactiveInterval": 3600,
          "attributes": {
            "user_authenticated": true,
            "last_activity": 1640995260000,
            "editor_draft_editor1": "草稿內容...",
            "blackjack_game_state": {
              "game_started": 1640995200000,
              "player_hand": ["A♠", "K♥"],
              "dealer_hand": ["Q♣"],
              "game_status": "active"
            }
          }
        }]
    end
    
    subgraph "分布式鎖示例"
        G[lock:metrics:export:lock] --> H["550e8400-e29b-41d4-a716-446655440000"]
        I[lock:content:save:lock] --> J["6ba7b810-9dad-11d1-80b4-00c04fd430c8"]
    end
    
    D --> E
    D --> G
```

### 17. Redis 存儲流程詳解
```mermaid
sequenceDiagram
    participant App as 應用層
    participant Session as Spring Session
    participant Serializer as 序列化器
    participant Redis as Redis 存儲
    
    App->>Session: session.setAttribute("key", value)
    Session->>Serializer: 序列化 Session 對象
    Serializer->>Redis: 存儲序列化數據
    Redis-->>Serializer: 確認存儲
    Serializer-->>Session: 序列化完成
    Session-->>App: 存儲成功
    
    Note over App,Redis: 讀取流程
    App->>Session: session.getAttribute("key")
    Session->>Redis: 讀取序列化數據
    Redis-->>Session: 返回數據
    Session->>Serializer: 反序列化
    Serializer-->>Session: 反序列化完成
    Session-->>App: 返回原始數據
```

### 18. Redis 數據類型與序列化
```mermaid
graph LR
    subgraph "應用數據類型"
        A[Boolean] --> D[user_authenticated: true]
        B[Long] --> E[last_activity: 1640995260000]
        C[String] --> F[editor_draft: "草稿內容..."]
        G[Map] --> H[game_state: {...}]
    end
    
    subgraph "序列化過程"
        I[GenericJackson2JsonRedisSerializer] --> J[JSON 格式]
        K[StringRedisSerializer] --> L[純字符串]
    end
    
    subgraph "Redis 存儲"
        M[Session 數據] --> N[JSON 序列化]
        O[鎖數據] --> P[字符串序列化]
    end
    
    D --> I
    E --> I
    F --> I
    H --> I
    N --> M
    P --> O
```

### 19. 統一錯誤代碼 Enum 架構
```mermaid
classDiagram
    class ErrorCode {
        +通用業務錯誤 (SYS_*)
        +檔案相關錯誤 (FILE_*)
        +認證相關錯誤 (AUTH_*)
        +授權相關錯誤 (AUTHZ_*)
        +登出相關錯誤 (LOGOUT_*)
        +會話相關錯誤 (SESSION_*)
        +應用層認證錯誤 (APP_*)
        +安全配置相關 (CONFIG_*)
    }
    
    class ErrorResponse {
        +錯誤代碼
        +錯誤訊息
        +詳細信息
        +時間戳
    }
    
    ErrorCode --> ErrorResponse
```

### 20. 統一錯誤代碼處理流程
```mermaid
sequenceDiagram
    participant Client
    participant SecurityFilter
    participant AuthenticationEntryPoint
    participant AccessDeniedHandler
    participant ErrorCode
    participant ErrorResponse
    
    Client->>SecurityFilter: 認證/授權請求
    alt 認證失敗
        SecurityFilter->>AuthenticationEntryPoint: 觸發認證失敗
        AuthenticationEntryPoint->>ErrorCode: 獲取錯誤訊息
        ErrorCode-->>AuthenticationEntryPoint: AUTHENTICATION_FAILED
        AuthenticationEntryPoint->>ErrorResponse: 創建錯誤響應
        ErrorResponse-->>AuthenticationEntryPoint: 錯誤響應對象
        AuthenticationEntryPoint-->>Client: 401 Unauthorized
    else 授權失敗
        SecurityFilter->>AccessDeniedHandler: 觸發授權失敗
        AccessDeniedHandler->>ErrorCode: 獲取錯誤訊息
        ErrorCode-->>AccessDeniedHandler: AUTHORIZATION_FAILED
        AccessDeniedHandler->>ErrorResponse: 創建錯誤響應
        ErrorResponse-->>AccessDeniedHandler: 錯誤響應對象
        AccessDeniedHandler-->>Client: 403 Forbidden
    end
```

## 錯誤處理架構 (Chain of Responsibility Pattern)

## 錯誤處理架構 (Chain of Responsibility Pattern)
```mermaid
classDiagram
    class ErrorHandlingFramework {
        +Chain of Responsibility
        +Error Response Builder
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

## 設計模式相互關係詳解

### 1. 策略模式 + 裝飾器模式的協作
```mermaid
classDiagram
    class DamageStrategy {
        <<interface>>
        +calculateDamage(People, List~Weapon~) int
    }
    
    class DefaultDamageStrategy {
        +calculateDamage(People, List~Weapon~) int
    }
    
    class DamageStrategyDecorator {
        <<abstract>>
        -DamageStrategy delegate
        +calculateDamage(People, List~Weapon~) int
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

### 2. 責任鏈模式 + 工廠模式的整合
```mermaid
classDiagram
    class SpringContainer {
        +ApplicationContext
        +BeanFactory
    }
    
    class GlobalExceptionHandler {
        -List~ApiExceptionHandler~ handlerChain
        +handleGlobalException(Exception)
    }
    
    class ApiExceptionHandler {
        <<interface>>
        +canHandle(Exception) boolean
        +handle(Exception, HttpServletRequest) ResponseEntity
    }
    
    class BusinessApiExceptionHandler {
        +@Order(0)
    }
    
    class DataIntegrityApiExceptionHandler {
        +@Order(1)
    }
    
    class ValidationApiExceptionHandler {
        +@Order(2)
    }
    
    class DefaultApiExceptionHandler {
        +@Order(Integer.MAX_VALUE)
    }
    
    SpringContainer --> GlobalExceptionHandler
    SpringContainer --> BusinessApiExceptionHandler
    SpringContainer --> DataIntegrityApiExceptionHandler
    SpringContainer --> ValidationApiExceptionHandler
    SpringContainer --> DefaultApiExceptionHandler
    GlobalExceptionHandler --> ApiExceptionHandler
```

### 3. 模板方法模式 + 單例模式的結合
```mermaid
classDiagram
    class BaseService {
        <<abstract>>
        +getConnection() Connection
        +executeWithAutoClose() T
        +executeInTransaction() T
    }
    
    class PeopleService {
        +getAllPeople() List~People~
        +updatePerson(People) People
    }
    
    class WeaponService {
        +getAllWeapons() List~Weapon~
        +saveWeapon(Weapon) Weapon
    }
    
    class GalleryService {
        +getAllGalleries() List~Gallery~
        +saveGallery(Gallery) Gallery
    }
    
    BaseService <|-- PeopleService
    BaseService <|-- WeaponService
    BaseService <|-- GalleryService
```

### 4. 代理模式 + 觀察者模式的協作
```mermaid
classDiagram
    class SpringAOP {
        +@Transactional
        +@Around
        +@Before
        +@After
    }
    
    class WebSocketUtil {
        +sendMessageForAll(String)
        +addSession(String, Session)
        +removeSession(String)
    }
    
    class MetricsWSController {
        +@Scheduled
        +exportMetrics()
    }
    
    class LivestockWSController {
        +broadcastLivestockUpdate(Livestock)
    }
    
    SpringAOP --> WebSocketUtil
    WebSocketUtil --> MetricsWSController
    WebSocketUtil --> LivestockWSController
```

### 5. 適配器模式 + Repository 模式的整合
```mermaid
classDiagram
    class BaseRepository {
        <<interface>>
        +findAll() List~T~
        +save(T) T
        +deleteById(ID)
    }
    
    class IntegerPkRepository {
        +findById(Integer) Optional~T~
    }
    
    class StringPkRepository {
        +findById(String) Optional~T~
    }
    
    class PeopleRepository {
        +findByName(String) Optional~People~
    }
    
    class WeaponRepository {
        +findByType(String) List~Weapon~
    }
    
    BaseRepository <|-- IntegerPkRepository
    BaseRepository <|-- StringPkRepository
    IntegerPkRepository <|-- PeopleRepository
    StringPkRepository <|-- WeaponRepository
```

### 6. 設計模式的層級關係
```mermaid
graph TB
    subgraph "表現層 (Presentation)"
        A[Controller + WebSocket + AOP]
    end
    
    subgraph "業務層 (Business)"
        B[Service + Strategy + Decorator]
    end
    
    subgraph "數據層 (Data)"
        C[Repository + Adapter + Template]
    end
    
    subgraph "基礎層 (Infrastructure)"
        D[Factory + Singleton + Chain]
    end
    
    A --> B
    B --> C
    C --> D
```

### 7. 模式協作流程
```mermaid
sequenceDiagram
    participant Client
    participant Controller
    participant Service
    participant Repository
    participant Database
    participant WebSocket
    
    Client->>Controller: HTTP Request
    Controller->>Service: 業務邏輯
    Service->>Repository: 數據操作
    Repository->>Database: SQL Query
    Database-->>Repository: Result
    Repository-->>Service: Entity
    Service-->>Controller: DTO
    Controller-->>Client: Response
    
    Note over Service,WebSocket: AOP 代理
    Service->>WebSocket: 事件通知
    WebSocket-->>Client: 實時更新
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