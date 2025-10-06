package tw.com.tymbackend.core.config.security;

import org.springframework.http.HttpStatus;

/**
 * Spring Security 常量枚舉
 * 
 * 定義所有與安全相關的常量，包括錯誤訊息、狀態碼、URL 路徑等。
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
public enum SecurityConstants {

    // ==================== 認證相關 ====================
    AUTHENTICATION_FAILED("認證失敗：請提供有效的 JWT Token", "AUTH_001", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("JWT Token 已過期", "AUTH_002", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID("JWT Token 格式無效", "AUTH_003", HttpStatus.UNAUTHORIZED),
    TOKEN_MISSING("缺少 JWT Token", "AUTH_004", HttpStatus.UNAUTHORIZED),

    // ==================== 授權相關 ====================
    AUTHORIZATION_FAILED("授權失敗：您沒有權限訪問此資源", "AUTHZ_001", HttpStatus.FORBIDDEN),
    INSUFFICIENT_PERMISSIONS("權限不足", "AUTHZ_002", HttpStatus.FORBIDDEN),
    ROLE_REQUIRED("需要特定角色才能訪問", "AUTHZ_003", HttpStatus.FORBIDDEN),

    // ==================== 登出相關 ====================
    LOGOUT_SUCCESS("登出成功", "LOGOUT_001", HttpStatus.OK),
    LOGOUT_FAILED("登出失敗", "LOGOUT_002", HttpStatus.INTERNAL_SERVER_ERROR),

    // ==================== 會話相關 ====================
    SESSION_EXPIRED("會話已過期", "SESSION_001", HttpStatus.UNAUTHORIZED),
    SESSION_INVALID("會話無效", "SESSION_002", HttpStatus.UNAUTHORIZED),

    // ==================== 安全配置相關 ====================
    CSRF_DISABLED("CSRF 保護已禁用", "CONFIG_001", HttpStatus.OK),
    CORS_ENABLED("CORS 已啟用", "CONFIG_002", HttpStatus.OK),

    // ==================== 角色相關 ====================
    ROLE_GUEST("GUEST", "ROLE_GUEST"),
    ROLE_MANAGE_USERS("manage-users", "ROLE_manage-users"),
    ROLE_ADMIN("admin", "ROLE_admin"),
    ROLE_USER("user", "ROLE_user");

    private final String message;
    private final String code;
    private final HttpStatus httpStatus;

    /**
     * 構造函數
     * 
     * @param message 錯誤訊息
     * @param code 錯誤代碼
     * @param httpStatus HTTP 狀態碼
     */
    SecurityConstants(String message, String code, HttpStatus httpStatus) {
        this.message = message;
        this.code = code;
        this.httpStatus = httpStatus;
    }

    /**
     * 構造函數 (僅用於角色常量)
     * 
     * @param message 角色名稱
     * @param code 角色代碼
     */
    SecurityConstants(String message, String code) {
        this.message = message;
        this.code = code;
        this.httpStatus = null;
    }

    /**
     * 獲取錯誤訊息
     * 
     * @return 錯誤訊息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 獲取錯誤代碼
     * 
     * @return 錯誤代碼
     */
    public String getCode() {
        return code;
    }

    /**
     * 獲取 HTTP 狀態碼
     * 
     * @return HTTP 狀態碼
     */
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    /**
     * 檢查是否為角色常量
     * 
     * @return true 如果是角色常量，false 否則
     */
    public boolean isRole() {
        return httpStatus == null;
    }

    /**
     * 根據錯誤代碼查找常量
     * 
     * @param code 錯誤代碼
     * @return 對應的常量，如果找不到則返回 null
     */
    public static SecurityConstants findByCode(String code) {
        for (SecurityConstants constant : values()) {
            if (constant.getCode().equals(code)) {
                return constant;
            }
        }
        return null;
    }

    /**
     * 根據 HTTP 狀態碼查找常量
     * 
     * @param httpStatus HTTP 狀態碼
     * @return 對應的常量列表
     */
    public static java.util.List<SecurityConstants> findByHttpStatus(HttpStatus httpStatus) {
        java.util.List<SecurityConstants> result = new java.util.ArrayList<>();
        for (SecurityConstants constant : values()) {
            if (constant.getHttpStatus() != null && constant.getHttpStatus().equals(httpStatus)) {
                result.add(constant);
            }
        }
        return result;
    }

    /**
     * 獲取所有認證相關錯誤
     * 
     * @return 認證錯誤常量列表
     */
    public static java.util.List<SecurityConstants> getAuthenticationErrors() {
        java.util.List<SecurityConstants> result = new java.util.ArrayList<>();
        for (SecurityConstants constant : values()) {
            if (constant.getCode().startsWith("AUTH_") && !constant.getCode().startsWith("AUTHZ_")) {
                result.add(constant);
            }
        }
        return result;
    }

    /**
     * 獲取所有授權相關錯誤
     * 
     * @return 授權錯誤常量列表
     */
    public static java.util.List<SecurityConstants> getAuthorizationErrors() {
        java.util.List<SecurityConstants> result = new java.util.ArrayList<>();
        for (SecurityConstants constant : values()) {
            if (constant.getCode().startsWith("AUTHZ_")) {
                result.add(constant);
            }
        }
        return result;
    }

    /**
     * 獲取所有角色常量
     * 
     * @return 角色常量列表
     */
    public static java.util.List<SecurityConstants> getRoles() {
        java.util.List<SecurityConstants> result = new java.util.ArrayList<>();
        for (SecurityConstants constant : values()) {
            if (constant.isRole()) {
                result.add(constant);
            }
        }
        return result;
    }
} 