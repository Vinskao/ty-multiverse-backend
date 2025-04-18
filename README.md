# TY-Multiverse-Backend
這是我的個人網站的 Backend。

## 工廠方法架構

### 1. Repository 工廠模式

#### 1.1 配置
```mermaid
classDiagram
    class RepositoryConfig {
        +@Configuration
        +@EnableJpaRepositories
    }
    note for RepositoryConfig "配置JPA Repository掃描路徑"
```

#### 1.2 工廠接口
```mermaid
classDiagram
    class RepositoryFactory {
        <<interface>>
        +getCustomRepository(repositoryClass, entityType, idType)
        +getRepository(entityType, idType)
        +getSpecificationRepository(entityType, idType)
    }
```

#### 1.3 工廠實現
```mermaid
classDiagram
    class RepositoryFactoryImpl {
        -ApplicationContext applicationContext
        -Map<Class<?>, Object> repositoryCache
        +getCustomRepository()
        +getRepository()
        +getSpecificationRepository()
    }
    RepositoryFactory <|.. RepositoryFactoryImpl
```

### 2. 查詢條件工廠模式

#### 2.1 工廠接口
```mermaid
classDiagram
    class QueryConditionFactory {
        <<interface>>
        +createEqualsCondition(field, value)
        +createLikeCondition(field, value)
        +createRangeCondition(field, min, max)
        +createCompositeCondition(conditions)
        +createDynamicCondition(field, value, operator)
    }
```

#### 2.2 工廠實現
```mermaid
classDiagram
    class QueryConditionFactoryImpl {
        +createEqualsCondition()
        +createLikeCondition()
        +createRangeCondition()
        +createCompositeCondition()
        +createDynamicCondition()
    }
    QueryConditionFactory <|.. QueryConditionFactoryImpl
```

### 3. 工作原理

#### 3.1 Repository 註冊流程
```mermaid
sequenceDiagram
    participant Spring
    participant RepositoryConfig
    participant RepositoryFactory
    participant Service
    
    Spring->>RepositoryConfig: 掃描Repository接口
    Spring->>RepositoryFactory: 註冊Factory Bean
    Service->>RepositoryFactory: 請求Repository實例
    RepositoryFactory->>Spring: 獲取Repository Bean
    Spring-->>Service: 返回Repository實例
```

#### 3.2 Factory 註冊流程
```mermaid
sequenceDiagram
    participant Spring
    participant Factory
    participant ApplicationContext
    
    Spring->>Factory: 掃描@Component
    Spring->>ApplicationContext: 注入Context
    Factory->>ApplicationContext: 獲取Bean
```

#### 3.3 Service 使用流程
```mermaid
classDiagram
    class PeopleService {
        -PeopleRepository repository
        -QueryConditionFactory queryConditionFactory
        +constructor(repositoryFactory, queryConditionFactory)
    }
    PeopleService --> RepositoryFactory
    PeopleService --> QueryConditionFactory
```

### 4. 設計優點

1. **依賴注入**
   - 所有組件都由 Spring 管理
   - 降低組件間耦合度
   - 便於單元測試

2. **類型安全**
   - 編譯時類型檢查
   - 避免運行時類型錯誤
   - 更好的 IDE 支持

3. **緩存機制**
   - 避免重複創建實例
   - 提高性能
   - 節省資源

### 5. 使用示例
```mermaid
sequenceDiagram
    participant Service
    participant RepositoryFactory
    participant QueryConditionFactory
    participant Repository
    
    Service->>RepositoryFactory: 獲取Repository
    RepositoryFactory-->>Service: 返回Repository實例
    Service->>QueryConditionFactory: 創建查詢條件
    QueryConditionFactory-->>Service: 返回Specification
    Service->>Repository: 執行查詢
    Repository-->>Service: 返回結果
```

## swagger ui

```bash
http://localhost:8080/tymb/swagger-ui/index.html#/
https://peoplesystem.tatdvsonorth.com/tymb/swagger-ui/index.html#/
```

## image 建置

```bash
mvn clean package -DskipTests
docker buildx build --platform linux/arm64 -t papakao/ty-multiverse-backend:latest --push .
mvn -P platform install
docker build -t papakao/ty-multiverse-backend:latest .
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

```

## 錯誤處理架構

### 1. 錯誤處理架構圖
```mermaid
classDiagram
    class ErrorCode {
        <<enumeration>>
        +INTERNAL_SERVER_ERROR
        +BAD_REQUEST
        +NOT_FOUND
        +UNAUTHORIZED
        +FORBIDDEN
        +CONFLICT
        +getCode()
        +getHttpStatus()
        +getMessage()
    }
    
    class BusinessException {
        -ErrorCode errorCode
        +BusinessException(ErrorCode)
        +BusinessException(ErrorCode, String)
        +BusinessException(ErrorCode, String, Throwable)
        +getErrorCode()
    }
    
    class ErrorResponse {
        -int code
        -String message
        -String detail
        -LocalDateTime timestamp
        -String path
        +ErrorResponse(int, String, String, String)
        +fromErrorCode(ErrorCode, String, String)
        +fromBusinessException(BusinessException, String)
    }
    
    class GlobalExceptionHandler {
        -Logger logger
        +handleBusinessException()
        +handleEntityNotFoundException()
        +handleDataIntegrityViolationException()
        +handleOptimisticLockingFailureException()
        +handleMethodArgumentNotValidException()
        +handleConstraintViolationException()
        +handleBindException()
        +handleGlobalException()
    }
    
    BusinessException --> ErrorCode
    ErrorResponse --> ErrorCode
    GlobalExceptionHandler --> BusinessException
    GlobalExceptionHandler --> ErrorResponse
```

### 2. 模組特定異常
```mermaid
classDiagram
    class BusinessException {
        <<abstract>>
        -ErrorCode errorCode
        +BusinessException(ErrorCode)
        +BusinessException(ErrorCode, String)
        +BusinessException(ErrorCode, String, Throwable)
        +getErrorCode()
    }
    
    class PeopleException {
        +PeopleException(ErrorCode)
        +PeopleException(ErrorCode, String)
        +PeopleException(ErrorCode, String, Throwable)
    }
    
    class WeaponException {
        +WeaponException(ErrorCode)
        +WeaponException(ErrorCode, String)
        +WeaponException(ErrorCode, String, Throwable)
    }
    
    class LivestockException {
        +LivestockException(ErrorCode)
        +LivestockException(ErrorCode, String)
        +LivestockException(ErrorCode, String, Throwable)
    }
    
    class GalleryException {
        +GalleryException(ErrorCode)
        +GalleryException(ErrorCode, String)
        +GalleryException(ErrorCode, String, Throwable)
    }
    
    BusinessException <|-- PeopleException
    BusinessException <|-- WeaponException
    BusinessException <|-- LivestockException
    BusinessException <|-- GalleryException
```

### 3. 錯誤處理流程
```mermaid
sequenceDiagram
    participant Controller
    participant Service
    participant Exception
    participant GlobalExceptionHandler
    participant ErrorResponse
    participant Client
    
    Controller->>Service: 調用服務方法
    Service->>Exception: 拋出業務異常
    Exception-->>Controller: 異常傳播
    Controller->>GlobalExceptionHandler: 捕獲異常
    GlobalExceptionHandler->>ErrorResponse: 創建錯誤響應
    GlobalExceptionHandler-->>Client: 返回錯誤響應
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