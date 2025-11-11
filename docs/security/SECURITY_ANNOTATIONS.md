# Backend Security 注解使用指南 (已废弃)

## ⚠️ 重要提醒

**此文档已废弃！**

从 2025-11-10 开始，TY Multiverse Backend 采用集中式安全配置，不再使用方法级别的 `@PreAuthorize` 注解。

**新的配置方式**: `ty-multiverse-backend/src/main/java/tw/com/tymbackend/core/SecurityConfig.java`

**详细说明**: 请参考 `SECURITY_CONFIG.md`

---

## 历史版本：注解方式（已废弃）

根据 `AGENTS.md` 的端点定义，Backend 曾经使用方法级别的 `@PreAuthorize` 注解来实现细粒度的权限控制。

## 权限策略

### 1. SELECT 系列：全部开放

所有查询操作（GET 请求）都允许已认证用户访问，无需特殊权限。

```java
@GetMapping("/get-all")
@PreAuthorize("isAuthenticated()")
public ResponseEntity<BackendApiResponse<List<People>>> getAllPeople() {
    // 任何已认证用户都可以查询
}
```

### 2. INSERT/UPDATE/DELETE 系列：需要认证

所有修改操作都需要用户已认证。

```java
@PostMapping("/insert")
@PreAuthorize("isAuthenticated()")
public ResponseEntity<BackendApiResponse<People>> insertPeople(@RequestBody People people) {
    // 需要认证才能插入
}

@PostMapping("/update")
@PreAuthorize("isAuthenticated()")
public ResponseEntity<BackendApiResponse<People>> updatePeople(@RequestBody People people) {
    // 需要认证才能更新
}

@DeleteMapping("/delete/{id}")
@PreAuthorize("isAuthenticated()")
public ResponseEntity<BackendApiResponse<Void>> deletePeople(@PathVariable Long id) {
    // 需要认证才能删除
}
```

### 3. 管理员专用操作：需要 ADMIN 角色

某些敏感操作只允许管理员执行。

```java
@DeleteMapping("/delete-all")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<BackendApiResponse<Void>> deleteAllPeople() {
    // 只有管理员可以批量删除
}
```

## 注解参考

### 常用注解

| 注解 | 说明 | 使用场景 |
|------|------|---------|
| `@PreAuthorize("isAuthenticated()")` | 需要认证 | SELECT/INSERT/UPDATE/DELETE |
| `@PreAuthorize("hasRole('ADMIN')")` | 需要 ADMIN 角色 | 批量删除、系统配置 |
| `@PreAuthorize("hasRole('USER')")` | 需要 USER 角色 | 普通用户操作 |
| `@PreAuthorize("permitAll()")` | 允许所有人 | 公共端点 |
| `@PreAuthorize("hasAnyRole('ADMIN', 'USER')")` | 需要任一角色 | 多角色共享操作 |

### 复杂权限表达式

```java
// 管理员或资源所有者
@PreAuthorize("hasRole('ADMIN') or @peopleService.isOwner(#id, authentication.name)")
public ResponseEntity<BackendApiResponse<Void>> deletePeople(@PathVariable Long id) {
    // ...
}

// 多条件组合
@PreAuthorize("isAuthenticated() and hasRole('USER') and @securityService.canAccess(#id)")
public ResponseEntity<BackendApiResponse<People>> getPeople(@PathVariable Long id) {
    // ...
}
```

## Controller 示例

### People Controller

```java
@RestController
@RequestMapping("/tymb/people")
public class PeopleController {

    // ========================================
    // SELECT 系列：已认证即可访问
    // ========================================

    @GetMapping("/get-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BackendApiResponse<List<People>>> getAllPeople() {
        // 任何已认证用户都可以查询所有人员
    }

    @GetMapping("/names")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BackendApiResponse<List<String>>> getNames() {
        // 任何已认证用户都可以查询名称列表
    }

    @PostMapping("/get-by-name")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BackendApiResponse<People>> getPersonByName(@RequestBody Map<String, String> request) {
        // 任何已认证用户都可以按名称查询
    }

    // ========================================
    // INSERT 系列：需要认证
    // ========================================

    @PostMapping("/insert")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BackendApiResponse<People>> insertPeople(@RequestBody People people) {
        // 需要认证才能插入
    }

    // ========================================
    // UPDATE 系列：需要认证
    // ========================================

    @PostMapping("/update")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BackendApiResponse<People>> updatePeople(@RequestBody People people) {
        // 需要认证才能更新
    }

    // ========================================
    // DELETE 系列：需要认证或管理员权限
    // ========================================

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BackendApiResponse<Void>> deletePeople(@PathVariable Long id) {
        // 需要认证才能删除单个
    }

    @DeleteMapping("/delete-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BackendApiResponse<Void>> deleteAllPeople() {
        // 只有管理员可以批量删除
    }
}
```

### Weapon Controller

```java
@RestController
@RequestMapping("/tymb/weapons")
public class WeaponController {

    // SELECT 系列
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BackendApiResponse<List<Weapon>>> getAllWeapons() {
        // 查询所有武器
    }

    @GetMapping("/{name}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BackendApiResponse<Weapon>> getWeaponById(@PathVariable String name) {
        // 按名称查询武器
    }

    @GetMapping("/owner/{ownerName}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BackendApiResponse<List<Weapon>>> getWeaponsByOwner(@PathVariable String ownerName) {
        // 按所有者查询武器
    }

    // INSERT/UPDATE 系列
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BackendApiResponse<Weapon>> saveWeapon(@RequestBody Weapon weapon) {
        // 保存武器
    }

    // DELETE 系列
    @DeleteMapping("/{name}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BackendApiResponse<Void>> deleteWeapon(@PathVariable String name) {
        // 删除武器
    }

    @DeleteMapping("/delete-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BackendApiResponse<Void>> deleteAllWeapons() {
        // 批量删除（仅管理员）
    }
}
```

### Gallery Controller

```java
@RestController
@RequestMapping("/tymb/gallery")
public class GalleryController {

    // SELECT 系列
    @PostMapping("/getAll")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BackendApiResponse<List<Gallery>>> getAllImages() {
        // 查询所有图片
    }

    @PostMapping("/getById")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BackendApiResponse<Gallery>> getImageById(@RequestBody Map<String, String> request) {
        // 按 ID 查询图片
    }

    // INSERT/UPDATE 系列
    @PostMapping("/save")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BackendApiResponse<Gallery>> saveImage(@RequestBody Gallery gallery) {
        // 保存图片
    }

    @PostMapping("/update")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BackendApiResponse<Gallery>> updateImage(@RequestBody Gallery gallery) {
        // 更新图片
    }

    // DELETE 系列
    @PostMapping("/delete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BackendApiResponse<Void>> deleteImage(@RequestBody Map<String, String> request) {
        // 删除图片
    }
}
```

## 最佳实践

### 1. 一致性

所有 Controller 都应遵循相同的权限策略：
- SELECT → `@PreAuthorize("isAuthenticated()")`
- INSERT/UPDATE/DELETE → `@PreAuthorize("isAuthenticated()")`
- 批量删除 → `@PreAuthorize("hasRole('ADMIN')")`

### 2. 文档化

在每个 Controller 类上添加注释说明权限策略：

```java
/**
 * People Controller
 *
 * <p>权限策略：</p>
 * <ul>
 *   <li>SELECT 系列：已认证即可访问</li>
 *   <li>INSERT/UPDATE/DELETE：需要认证</li>
 *   <li>批量删除：仅管理员</li>
 * </ul>
 */
@RestController
@RequestMapping("/tymb/people")
public class PeopleController {
    // ...
}
```

### 3. 测试

为每个权限级别编写测试：

```java
@Test
@WithMockUser(roles = "USER")
void testGetAllPeople_WithUser_ShouldSucceed() {
    // 测试普通用户可以查询
}

@Test
@WithMockUser(roles = "USER")
void testDeleteAllPeople_WithUser_ShouldFail() {
    // 测试普通用户不能批量删除
}

@Test
@WithMockUser(roles = "ADMIN")
void testDeleteAllPeople_WithAdmin_ShouldSucceed() {
    // 测试管理员可以批量删除
}
```

## 故障排除

### 问题 1: 403 Forbidden

**原因**: 用户没有所需的角色或权限

**解决**:
1. 检查 JWT Token 中的角色信息
2. 确认 `@PreAuthorize` 注解的表达式正确
3. 验证 Keycloak 中的角色映射

### 问题 2: 401 Unauthorized

**原因**: Token 无效或已过期

**解决**:
1. 检查 Token 是否在请求头中
2. 验证 Token 是否已过期
3. 确认 Keycloak 配置正确

### 问题 3: 注解不生效

**原因**: `@EnableMethodSecurity` 未启用

**解决**:
确保 Security 配置类上有 `@EnableMethodSecurity(prePostEnabled = true)` 注解

## 参考文档

- `ty-multiverse-backend/AGENTS.md` - Backend 端点定义
- `ty-multiverse-frontend/AGENTS.md` - API 架构说明
- `ty-multiverse-common/SECURITY_GUIDE.md` - 安全配置指南

