# TY Multiverse Backend Security 配置指南

## 概述

TY Multiverse Backend 采用集中式安全配置，不再使用方法级别的 `@PreAuthorize` 注解，而是通过 `SecurityConfig.java` 统一管理所有端点的权限规则。

## 配置位置

**文件路径**: `ty-multiverse-backend/src/main/java/tw/com/tymbackend/core/SecurityConfig.java`

## 权限策略

### 1. 基于 AGENTS.md 的端点分类

#### SELECT 系列（GET 请求）：已认证即可访问
```java
.requestMatchers("GET", "/tymb/people/**").authenticated()      // People 查询
.requestMatchers("GET", "/tymb/weapons/**").authenticated()      // Weapon 查询
.requestMatchers("GET", "/tymb/gallery/**").authenticated()      // Gallery 查询
.requestMatchers("GET", "/tymb/api/**").authenticated()          // Async API 查询
.requestMatchers("GET", "/tymb/people-images/**").authenticated() // People Images 查询
.requestMatchers("GET", "/tymb/blackjack/**").authenticated()    // Blackjack 查询
```

#### INSERT/UPDATE/DELETE 系列（POST/PUT/DELETE）：需要认证
```java
.requestMatchers("POST", "/tymb/people/**").authenticated()      // People 创建/更新
.requestMatchers("PUT", "/tymb/people/**").authenticated()       // People 更新
.requestMatchers("DELETE", "/tymb/people/**").authenticated()    // People 删除（单个）
```

#### 批量删除（DELETE *all）：仅管理员
```java
.requestMatchers("DELETE", "/tymb/people/delete-all").hasRole("ADMIN")   // 批量删除 People
.requestMatchers("DELETE", "/tymb/weapons/delete-all").hasRole("ADMIN")  // 批量删除 Weapons
.requestMatchers("DELETE", "/tymb/gallery/delete-all").hasRole("ADMIN")  // 批量删除 Gallery
```

### 2. 公共路径：完全开放

```java
.requestMatchers("/tymb/actuator/**").permitAll()           // Spring Boot Actuator
.requestMatchers("/tymb/health/**").permitAll()             // 健康检查
.requestMatchers("/tymb/swagger-ui/**").permitAll()          // Swagger UI
.requestMatchers("/tymb/v3/api-docs/**").permitAll()         // API 文档
.requestMatchers("/tymb/webjars/**").permitAll()             // Swagger 静态资源
.requestMatchers("/tymb/auth/visitor").permitAll()           // 访客端点
.requestMatchers("/tymb/auth/health").permitAll()            // 认证健康检查
.requestMatchers("/tymb/keycloak/introspect").permitAll()    // Token 内省
```

### 3. 认证端点：需要认证

```java
.requestMatchers("/tymb/auth/admin").authenticated()         // Admin 测试端点
.requestMatchers("/tymb/auth/user").authenticated()          // User 测试端点
.requestMatchers("/tymb/auth/test").authenticated()           // Auth 测试端点
.requestMatchers("/tymb/auth/logout-test").authenticated()    // Logout 测试端点
```

## 配置规则

### 优先级顺序

配置的顺序很重要，从具体到通用：

1. **permitAll()** - 公共路径（最高优先级）
2. **authenticated()** - 需要认证
3. **hasRole("ADMIN")** - 需要管理员角色
4. **anyRequest().authenticated()** - 默认规则（最低优先级）

### HTTP 方法匹配

使用精确的 HTTP 方法匹配：
- `GET` - 查询操作
- `POST` - 创建操作
- `PUT` - 更新操作
- `DELETE` - 删除操作

### 路径匹配

使用 Ant 路径模式：
- `/tymb/people/**` - 匹配 `/tymb/people/` 下的所有路径
- `/tymb/people/delete-all` - 精确匹配批量删除路径

## 架构优势

### 1. 集中管理

✅ **优点**：
- 所有权限规则在一个文件中
- 容易维护和审计
- 权限策略一目了然

❌ **之前的方法注解方式**：
- 权限分散在各个 Controller 中
- 难以整体把握权限策略
- 容易遗漏或不一致

### 2. 性能优化

- 路由级别权限检查（比方法级别更快）
- 避免不必要的 AOP 拦截
- 减少 Spring Security 的处理开销

### 3. 深度防御

- Gateway 验证 Token（粗粒度）
- Backend 验证权限（细粒度）
- 即使 Gateway 被绕过仍有保护

## Controller 清理

### 需要移除的注解

所有 Controller 方法上的 `@PreAuthorize` 注解都可以移除：

```java
@RestController
@RequestMapping("/tymb/people")
public class PeopleController {

    // ❌ 之前需要注解
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/get-all")
    public ResponseEntity<BackendApiResponse<List<People>>> getAllPeople() {
        // 业务逻辑
    }

    // ❌ 之前需要注解
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete-all")
    public ResponseEntity<BackendApiResponse<Void>> deleteAllPeople() {
        // 业务逻辑
    }
}
```

### 清理后的 Controller

```java
@RestController
@RequestMapping("/tymb/people")
public class PeopleController {

    // ✅ 权限由 SecurityConfig 统一管理
    @GetMapping("/get-all")
    public ResponseEntity<BackendApiResponse<List<People>>> getAllPeople() {
        // 业务逻辑 - GET 权限自动验证
    }

    // ✅ 权限由 SecurityConfig 统一管理
    @DeleteMapping("/delete-all")
    public ResponseEntity<BackendApiResponse<Void>> deleteAllPeople() {
        // 业务逻辑 - ADMIN 权限自动验证
    }
}
```

## 测试验证

### 权限测试

#### 1. SELECT 权限测试

```bash
# ✅ 已认证用户可以查询
curl -H "Authorization: Bearer <user-token>" \
     http://localhost:8080/tymb/people/get-all

# ❌ 未认证用户被拒绝
curl http://localhost:8080/tymb/people/get-all
```

#### 2. INSERT/UPDATE/DELETE 权限测试

```bash
# ✅ 已认证用户可以创建
curl -X POST \
     -H "Authorization: Bearer <user-token>" \
     -H "Content-Type: application/json" \
     -d '{"name":"Test"}' \
     http://localhost:8080/tymb/people/insert

# ❌ 未认证用户被拒绝
curl -X POST \
     -H "Content-Type: application/json" \
     -d '{"name":"Test"}' \
     http://localhost:8080/tymb/people/insert
```

#### 3. 批量删除权限测试

```bash
# ✅ 管理员可以批量删除
curl -X DELETE \
     -H "Authorization: Bearer <admin-token>" \
     http://localhost:8080/tymb/people/delete-all

# ❌ 普通用户被拒绝
curl -X DELETE \
     -H "Authorization: Bearer <user-token>" \
     http://localhost:8080/tymb/people/delete-all
```

### 公共路径测试

```bash
# ✅ 公共路径无需认证
curl http://localhost:8080/tymb/health
curl http://localhost:8080/tymb/swagger-ui/
```

## 维护指南

### 添加新端点

1. **确定权限级别**：
   - SELECT 操作 → `authenticated()`
   - INSERT/UPDATE/DELETE → `authenticated()`
   - 批量删除 → `hasRole("ADMIN")`

2. **添加到 SecurityConfig**：
   ```java
   .requestMatchers("GET", "/tymb/new-module/**").authenticated()      // 新模块查询
   .requestMatchers("POST", "/tymb/new-module/**").authenticated()     // 新模块创建
   .requestMatchers("DELETE", "/tymb/new-module/delete-all").hasRole("ADMIN") // 新模块批量删除
   ```

3. **测试权限**：
   - 验证已认证用户可以访问
   - 验证未认证用户被拒绝
   - 验证管理员权限正确

### 修改权限策略

如果需要调整权限策略，直接修改 `SecurityConfig.java` 中的相应规则即可。

### 角色扩展

如果需要添加新角色（除了 ADMIN 和 USER），在 Keycloak 中配置后，在 SecurityConfig 中使用：

```java
.requestMatchers("/tymb/special/**").hasRole("SPECIAL_ROLE")
```

## 故障排除

### 常见问题

#### 1. 403 Forbidden

**原因**: 用户没有所需权限
**解决**:
1. 检查 JWT Token 中的角色
2. 验证 SecurityConfig 中的路径匹配
3. 确认 Keycloak 中的角色配置

#### 2. 401 Unauthorized

**原因**: Token 无效或过期
**解决**:
1. 检查 Token 是否在请求头中
2. 验证 Token 是否已过期
3. 确认 Keycloak 配置正确

#### 3. 权限配置不生效

**原因**: 配置顺序错误
**解决**:
1. 确保 `permitAll()` 在前面
2. 确保 `authenticated()` 在中间
3. 确保 `hasRole()` 在后面

## 相关文档

- `ty-multiverse-common/SECURITY_GUIDE.md` - 安全配置基础
- `ty-multiverse-gateway/AGENTS.md` - Gateway 架构说明
- `ty-multiverse-frontend/AGENTS.md` - API 架构说明
- `SECURITY_DEPLOYMENT_SUMMARY.md` - 部署总结

## 版本历史

- **v1.0 (2025-11-10)**: 初始版本，集中式安全配置
  - 移除所有方法级别的 `@PreAuthorize` 注解
  - 在 SecurityConfig 中统一管理权限
  - 基于 AGENTS.md 的端点分类
  - 简化权限管理，提高性能
