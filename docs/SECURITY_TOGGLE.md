# Security Toggle Configuration

## Overview

Backend 提供安全性開關功能，用於本地開發和測試環境快速禁用所有安全性檢查。

**⚠️ WARNING: 此功能僅限本地開發使用，生產環境必須保持關閉！**

## Configuration

### Application Configuration

在 `application.yml` 中定義開關：

```yaml
# Security Configuration
security:
  # 開發測試用：完全禁用安全性檢查（僅限本地開發）
  # WARNING: 生產環境必須設為 false
  disable-all: ${SECURITY_DISABLE_ALL:false}
```

### Environment-Specific Settings

#### Local Development (啟用)

在 `src/main/resources/env/local.properties` 中：

```properties
# Security Configuration for Local Development
# WARNING: Only enable this for local testing, NEVER in production
SECURITY_DISABLE_ALL=true
```

#### Production (禁用 - 預設)

在生產環境配置中**不要設定**或明確設為 `false`：

```properties
# Production - Security MUST be enabled
SECURITY_DISABLE_ALL=false
```

或直接不設定該變數（預設為 `false`）

## Implementation

### SecurityConfig.java

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${security.disable-all:false}")
    private boolean disableAllSecurity;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        // 開發測試模式：完全禁用安全性
        if (disableAllSecurity) {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }

        // 正常安全性配置：基于 AGENTS.md 的端点定义
        http.authorizeHttpRequests(auth -> auth
            // ... 完整的端點權限控制
        );

        return http.build();
    }
}
```

## Usage

### Enable Security Toggle (Local Testing)

1. 設定環境變數：
```bash
export SECURITY_DISABLE_ALL=true
```

2. 或在 `local.properties` 中設定：
```properties
SECURITY_DISABLE_ALL=true
```

3. 啟動應用：
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Disable Security Toggle (Normal Mode)

1. 移除或設為 false：
```bash
export SECURITY_DISABLE_ALL=false
```

2. 或在配置文件中：
```properties
SECURITY_DISABLE_ALL=false
```

## Testing

### With Security Disabled

```bash
# All endpoints accessible without authentication
curl -X POST http://localhost:8080/tymb/people/get-all
# Response: 202 Accepted

curl -X GET http://localhost:8080/tymb/weapons
# Response: 200 OK with data
```

### With Security Enabled

```bash
# Endpoints require authentication
curl -X POST http://localhost:8080/tymb/people/get-all
# Response: 401 Unauthorized

curl -X GET http://localhost:8080/tymb/weapons
# Response: 200 OK (GET is permitAll)
```

## Benefits

1. **快速測試**: 本地開發時無需處理 JWT Token
2. **保持完整配置**: 不需要刪除或註解安全性配置程式碼
3. **環境隔離**: 透過環境變數控制，不影響其他環境
4. **安全預設**: 預設為 `false`，確保生產環境安全

## Security Considerations

### ⚠️ Critical Warnings

1. **絕對不要在生產環境啟用此開關**
2. **不要將 `SECURITY_DISABLE_ALL=true` 提交到生產配置**
3. **使用後記得關閉並測試完整安全性流程**
4. **定期檢查生產環境配置確保此開關為 false**

### Best Practices

1. **僅在本地開發環境使用**
2. **測試完成後立即關閉**
3. **CI/CD 流程中強制檢查此設定**
4. **生產部署前必須驗證安全性配置**

## Monitoring

### Check Current Security Status

```bash
# Check if security is disabled
curl http://localhost:8080/tymb/actuator/env | grep SECURITY_DISABLE_ALL

# Test with unauthenticated request
curl -X POST http://localhost:8080/tymb/people/get-all
# If returns 401: Security is enabled ✅
# If returns 202: Security is disabled ⚠️
```

## Related Documentation

- `AGENTS.md` - 完整的端點權限定義
- `SECURITY_CONFIG.md` - 詳細的安全性配置說明
- `SecurityConfig.java` - 安全性配置實作

