package tw.com.tymbackend.core.exception;

import org.springframework.http.HttpStatus;

/**
 * 統一錯誤代碼枚舉
 * 
 * 整合了通用業務錯誤和安全相關錯誤，提供統一的錯誤碼管理。
 * 每個錯誤碼都包含對應的 HTTP 狀態碼、錯誤代碼和中文錯誤訊息。
 * 
 * @author TY Backend Team
 * @version 2.0
 * @since 2024
 */
public enum ErrorCode {
    
    // ==================== 通用業務錯誤 ====================
    
    /**
     * 內部伺服器錯誤
     * 
     * 當系統發生未預期的內部錯誤時使用此錯誤碼。
     * 通常表示系統內部邏輯錯誤、資料庫連接問題或未處理的異常。
     * 
     * @see HttpStatus#INTERNAL_SERVER_ERROR
     */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYS_001", "內部伺服器錯誤"),
    
    /**
     * 無效的請求
     * 
     * 當客戶端發送的請求格式不正確、缺少必要參數或參數值無效時使用。
     * 例如：缺少必填欄位、參數類型錯誤、JSON 格式錯誤等。
     * 
     * @see HttpStatus#BAD_REQUEST
     */
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "SYS_002", "無效的請求"),
    
    /**
     * 找不到資源
     * 
     * 當請求的資源不存在時使用此錯誤碼。
     * 適用於查詢不存在的實體、檔案或 API 端點。
     * 
     * @see HttpStatus#NOT_FOUND
     */
    NOT_FOUND(HttpStatus.NOT_FOUND, "SYS_003", "找不到資源"),
    
    /**
     * 資源衝突
     * 
     * 當請求的操作與當前資源狀態衝突時使用此錯誤碼。
     * 例如：嘗試創建已存在的資源、並發修改衝突等。
     * 
     * @see HttpStatus#CONFLICT
     */
    CONFLICT(HttpStatus.CONFLICT, "SYS_004", "資源衝突"),
    
    /**
     * 實體不存在
     * 
     * 當嘗試操作（更新、刪除、查詢）一個不存在的資料庫實體時使用此錯誤碼。
     * 通常發生在根據 ID 查詢實體但找不到對應記錄的情況。
     * 
     * @see HttpStatus#NOT_FOUND
     */
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "SYS_005", "實體不存在"),
    
    /**
     * 重複的資料
     * 
     * 當嘗試創建或插入已存在的唯一資料時使用此錯誤碼。
     * 例如：重複的用戶名、電子郵件、業務編號等。
     * 
     * @see HttpStatus#CONFLICT
     */
    DUPLICATE_ENTRY(HttpStatus.CONFLICT, "SYS_006", "重複的資料"),
    
    /**
     * 樂觀鎖定失敗
     * 
     * 當使用樂觀鎖定機制時，資料已被其他用戶修改導致當前操作失敗時使用此錯誤碼。
     * 通常發生在並發更新同一個實體的情況。
     * 
     * @see HttpStatus#CONFLICT
     */
    OPTIMISTIC_LOCKING_FAILURE(HttpStatus.CONFLICT, "SYS_007", "資料已被其他使用者修改"),
    
    /**
     * 無效的操作
     * 
     * 當請求的操作在當前業務邏輯下不被允許時使用此錯誤碼。
     * 例如：在錯誤的狀態下執行特定操作、操作順序錯誤等。
     * 
     * @see HttpStatus#BAD_REQUEST
     */
    INVALID_OPERATION(HttpStatus.BAD_REQUEST, "SYS_008", "無效的操作"),
    
    /**
     * 無效的狀態
     * 
     * 當實體或系統處於不允許執行特定操作的狀態時使用此錯誤碼。
     * 例如：已完成的訂單無法再次修改、已停用的帳戶無法登入等。
     * 
     * @see HttpStatus#BAD_REQUEST
     */
    INVALID_STATE(HttpStatus.BAD_REQUEST, "SYS_009", "無效的狀態"),
    
    /**
     * 違反業務規則
     * 
     * 當操作違反系統定義的業務規則時使用此錯誤碼。
     * 例如：餘額不足、配額超限、時間限制等業務邏輯限制。
     * 
     * @see HttpStatus#BAD_REQUEST
     */
    BUSINESS_RULE_VIOLATION(HttpStatus.BAD_REQUEST, "SYS_010", "違反業務規則"),
    
    /**
     * 外部服務錯誤
     * 
     * 當依賴的外部服務（如第三方 API、微服務等）發生錯誤時使用此錯誤碼。
     * 表示外部服務無法正常響應或返回錯誤。
     * 
     * @see HttpStatus#BAD_GATEWAY
     */
    EXTERNAL_SERVICE_ERROR(HttpStatus.BAD_GATEWAY, "SYS_011", "外部服務錯誤"),
    
    /**
     * 外部服務超時
     * 
     * 當調用外部服務時發生超時錯誤時使用此錯誤碼。
     * 表示外部服務在預期時間內未響應。
     * 
     * @see HttpStatus#GATEWAY_TIMEOUT
     */
    EXTERNAL_SERVICE_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "SYS_012", "外部服務超時"),
    
    // ==================== 檔案相關錯誤 ====================
    
    /**
     * 檔案不存在
     * 
     * 當嘗試訪問或下載不存在的檔案時使用此錯誤碼。
     * 適用於檔案系統操作中的檔案查詢失敗。
     * 
     * @see HttpStatus#NOT_FOUND
     */
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "FILE_001", "檔案不存在"),
    
    /**
     * 檔案上傳錯誤
     * 
     * 當檔案上傳過程中發生錯誤時使用此錯誤碼。
     * 例如：磁碟空間不足、檔案系統權限問題、網路中斷等。
     * 
     * @see HttpStatus#INTERNAL_SERVER_ERROR
     */
    FILE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_002", "檔案上傳錯誤"),
    
    /**
     * 檔案下載錯誤
     * 
     * 當檔案下載過程中發生錯誤時使用此錯誤碼。
     * 例如：檔案損壞、讀取權限問題、網路中斷等。
     * 
     * @see HttpStatus#INTERNAL_SERVER_ERROR
     */
    FILE_DOWNLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_003", "檔案下載錯誤"),
    
    /**
     * 無效的檔案格式
     * 
     * 當上傳的檔案格式不被系統支援時使用此錯誤碼。
     * 例如：只允許圖片格式但上傳了文檔檔案。
     * 
     * @see HttpStatus#BAD_REQUEST
     */
    INVALID_FILE_FORMAT(HttpStatus.BAD_REQUEST, "FILE_004", "無效的檔案格式"),
    
    /**
     * 檔案太大
     * 
     * 當上傳的檔案大小超過系統限制時使用此錯誤碼。
     * 適用於檔案大小限制的業務規則。
     * 
     * @see HttpStatus#BAD_REQUEST
     */
    FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, "FILE_005", "檔案太大"),
    
    // ==================== 認證相關錯誤 ====================
    
    /**
     * 認證失敗
     * 
     * 當用戶未提供有效的認證憑證時使用此錯誤碼。
     * 例如：缺少 JWT Token、Token 已過期或格式錯誤。
     * 
     * @see HttpStatus#UNAUTHORIZED
     */
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_001", "認證失敗：請提供有效的 JWT Token"),
    
    /**
     * 未授權
     * 
     * 當用戶未提供有效的認證憑證時使用此錯誤碼。
     * 例如：缺少 JWT Token、Token 已過期或格式錯誤。
     * 
     * @see HttpStatus#UNAUTHORIZED
     */
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_002", "未授權"),
    
    /**
     * Token 過期
     * 
     * 當 JWT Token 已過期時使用此錯誤碼。
     * 
     * @see HttpStatus#UNAUTHORIZED
     */
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_003", "JWT Token 已過期"),
    
    /**
     * Token 無效
     * 
     * 當 JWT Token 格式無效時使用此錯誤碼。
     * 
     * @see HttpStatus#UNAUTHORIZED
     */
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH_004", "JWT Token 格式無效"),
    
    /**
     * 缺少 Token
     * 
     * 當請求中缺少 JWT Token 時使用此錯誤碼。
     * 
     * @see HttpStatus#UNAUTHORIZED
     */
    TOKEN_MISSING(HttpStatus.UNAUTHORIZED, "AUTH_005", "缺少 JWT Token"),
    
    /**
     * Token 內省失敗
     * 
     * 當 Token 內省檢查失敗時使用此錯誤碼。
     * 
     * @see HttpStatus#UNAUTHORIZED
     */
    TOKEN_INTROSPECT_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_006", "Token 內省失敗"),
    
    /**
     * Token 刷新失敗
     * 
     * 當 Token 刷新失敗時使用此錯誤碼。
     * 
     * @see HttpStatus#UNAUTHORIZED
     */
    TOKEN_REFRESH_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_007", "Token 刷新失敗"),
    
    /**
     * Token 無效或刷新失敗
     * 
     * 當 Token 無效或刷新失敗時使用此錯誤碼。
     * 
     * @see HttpStatus#UNAUTHORIZED
     */
    TOKEN_INVALID_OR_REFRESH_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_008", "Token 無效或刷新失敗"),
    
    /**
     * Token 檢查失敗
     * 
     * 當 Token 檢查過程中發生錯誤時使用此錯誤碼。
     * 
     * @see HttpStatus#INTERNAL_SERVER_ERROR
     */
    TOKEN_CHECK_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH_009", "Token 檢查失敗"),
    
    // ==================== 授權相關錯誤 ====================
    
    /**
     * 授權失敗
     * 
     * 當用戶已認證但沒有足夠權限訪問特定資源時使用此錯誤碼。
     * 例如：普通用戶嘗試訪問管理員功能。
     * 
     * @see HttpStatus#FORBIDDEN
     */
    AUTHORIZATION_FAILED(HttpStatus.FORBIDDEN, "AUTHZ_001", "授權失敗：您沒有權限訪問此資源"),
    
    /**
     * 禁止訪問
     * 
     * 當用戶已認證但沒有足夠權限訪問特定資源時使用此錯誤碼。
     * 例如：普通用戶嘗試訪問管理員功能。
     * 
     * @see HttpStatus#FORBIDDEN
     */
    FORBIDDEN(HttpStatus.FORBIDDEN, "AUTHZ_002", "禁止訪問"),
    
    /**
     * 權限不足
     * 
     * 當用戶權限不足以執行特定操作時使用此錯誤碼。
     * 
     * @see HttpStatus#FORBIDDEN
     */
    INSUFFICIENT_PERMISSIONS(HttpStatus.FORBIDDEN, "AUTHZ_003", "權限不足"),
    
    /**
     * 需要特定角色
     * 
     * 當操作需要特定角色才能執行時使用此錯誤碼。
     * 
     * @see HttpStatus#FORBIDDEN
     */
    ROLE_REQUIRED(HttpStatus.FORBIDDEN, "AUTHZ_004", "需要特定角色才能訪問"),
    
    // ==================== 登出相關錯誤 ====================
    
    /**
     * 登出成功
     * 
     * 當用戶成功登出時使用此錯誤碼。
     * 
     * @see HttpStatus#OK
     */
    LOGOUT_SUCCESS(HttpStatus.OK, "LOGOUT_001", "登出成功"),
    
    /**
     * 登出失敗
     * 
     * 當用戶登出過程中發生錯誤時使用此錯誤碼。
     * 
     * @see HttpStatus#INTERNAL_SERVER_ERROR
     */
    LOGOUT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "LOGOUT_002", "登出失敗"),
    
    // ==================== 會話相關錯誤 ====================
    
    /**
     * 會話過期
     * 
     * 當用戶會話已過期時使用此錯誤碼。
     * 
     * @see HttpStatus#UNAUTHORIZED
     */
    SESSION_EXPIRED(HttpStatus.UNAUTHORIZED, "SESSION_001", "會話已過期"),
    
    /**
     * 會話無效
     * 
     * 當用戶會話無效時使用此錯誤碼。
     * 
     * @see HttpStatus#UNAUTHORIZED
     */
    SESSION_INVALID(HttpStatus.UNAUTHORIZED, "SESSION_002", "會話無效"),
    
    /**
     * 會話未找到
     * 
     * 當用戶會話未找到時使用此錯誤碼。
     * 
     * @see HttpStatus#UNAUTHORIZED
     */
    SESSION_NOT_FOUND(HttpStatus.UNAUTHORIZED, "SESSION_003", "Session not found"),
    
    // ==================== 應用層認證錯誤 ====================
    
    /**
     * 用戶未登入
     * 
     * 當用戶未登入時使用此錯誤碼。
     * 
     * @see HttpStatus#UNAUTHORIZED
     */
    USER_NOT_LOGGED_IN(HttpStatus.UNAUTHORIZED, "APP_001", "User not logged in"),
    
    /**
     * 無活躍遊戲
     * 
     * 當沒有活躍的遊戲時使用此錯誤碼。
     * 
     * @see HttpStatus#BAD_REQUEST
     */
    NO_ACTIVE_GAME(HttpStatus.BAD_REQUEST, "APP_002", "No active game"),
    
    // ==================== 安全配置相關 ====================
    
    /**
     * CSRF 保護已禁用
     * 
     * 當 CSRF 保護已禁用時使用此錯誤碼。
     * 
     * @see HttpStatus#OK
     */
    CSRF_DISABLED(HttpStatus.OK, "CONFIG_001", "CSRF 保護已禁用"),
    
    /**
     * CORS 已啟用
     * 
     * 當 CORS 已啟用時使用此錯誤碼。
     * 
     * @see HttpStatus#OK
     */
    CORS_ENABLED(HttpStatus.OK, "CONFIG_002", "CORS 已啟用"),
    
    // ==================== Resilience 相關錯誤 ====================
    
    /**
     * 請求頻率過高
     * 
     * 當客戶端請求頻率超過系統限制時使用此錯誤碼。
     * 通常發生在 Rate Limiter 觸發限制時。
     * 
     * @see HttpStatus#TOO_MANY_REQUESTS
     */
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "RESIL_001", "請求頻率過高，請稍後再試"),
    
    /**
     * 系統並發處理繁忙
     * 
     * 當系統並發處理能力達到上限時使用此錯誤碼。
     * 通常發生在 Bulkhead 模式觸發時。
     * 
     * @see HttpStatus#TOO_MANY_REQUESTS
     */
    BULKHEAD_FULL(HttpStatus.TOO_MANY_REQUESTS, "RESIL_002", "系統並發處理繁忙，請稍後再試");
    
    /** HTTP 狀態碼 */
    private final HttpStatus httpStatus;
    
    /** 錯誤代碼 */
    private final String code;
    
    /** 錯誤訊息 */
    private final String message;
    
    /**
     * 建構子
     * 
     * @param httpStatus HTTP 狀態碼
     * @param code 錯誤代碼
     * @param message 錯誤訊息
     */
    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
    
    /**
     * 取得錯誤代碼的數值
     * 
     * 返回對應的 HTTP 狀態碼數值，例如 400、404、500 等。
     * 
     * @return HTTP 狀態碼的整數值
     */
    public int getCode() {
        return httpStatus.value();
    }
    
    /**
     * 取得 HTTP 狀態碼
     * 
     * 返回完整的 HttpStatus 物件，包含狀態碼和狀態訊息。
     * 
     * @return HTTP 狀態碼物件
     */
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
    
    /**
     * 取得錯誤代碼
     * 
     * 返回自定義的錯誤代碼，例如 AUTH_001、SYS_001 等。
     * 
     * @return 錯誤代碼字串
     */
    public String getErrorCode() {
        return code;
    }
    
    /**
     * 取得錯誤訊息
     * 
     * 返回此錯誤碼對應的中文錯誤訊息。
     * 
     * @return 錯誤訊息字串
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * 根據錯誤代碼查找錯誤
     * 
     * @param code 錯誤代碼
     * @return 對應的錯誤，如果找不到則返回 null
     */
    public static ErrorCode findByErrorCode(String code) {
        for (ErrorCode error : values()) {
            if (error.getErrorCode().equals(code)) {
                return error;
            }
        }
        return null;
    }
    
    /**
     * 根據 HTTP 狀態碼查找錯誤
     * 
     * @param httpStatus HTTP 狀態碼
     * @return 對應的錯誤列表
     */
    public static java.util.List<ErrorCode> findByHttpStatus(HttpStatus httpStatus) {
        java.util.List<ErrorCode> result = new java.util.ArrayList<>();
        for (ErrorCode error : values()) {
            if (error.getHttpStatus().equals(httpStatus)) {
                result.add(error);
            }
        }
        return result;
    }
    
    /**
     * 獲取所有認證相關錯誤
     * 
     * @return 認證錯誤列表
     */
    public static java.util.List<ErrorCode> getAuthenticationErrors() {
        java.util.List<ErrorCode> result = new java.util.ArrayList<>();
        for (ErrorCode error : values()) {
            if (error.getErrorCode().startsWith("AUTH_") && !error.getErrorCode().startsWith("AUTHZ_")) {
                result.add(error);
            }
        }
        return result;
    }
    
    /**
     * 獲取所有授權相關錯誤
     * 
     * @return 授權錯誤列表
     */
    public static java.util.List<ErrorCode> getAuthorizationErrors() {
        java.util.List<ErrorCode> result = new java.util.ArrayList<>();
        for (ErrorCode error : values()) {
            if (error.getErrorCode().startsWith("AUTHZ_")) {
                result.add(error);
            }
        }
        return result;
    }
    
    /**
     * 獲取所有系統相關錯誤
     * 
     * @return 系統錯誤列表
     */
    public static java.util.List<ErrorCode> getSystemErrors() {
        java.util.List<ErrorCode> result = new java.util.ArrayList<>();
        for (ErrorCode error : values()) {
            if (error.getErrorCode().startsWith("SYS_")) {
                result.add(error);
            }
        }
        return result;
    }
    
    /**
     * 獲取所有檔案相關錯誤
     * 
     * @return 檔案錯誤列表
     */
    public static java.util.List<ErrorCode> getFileErrors() {
        java.util.List<ErrorCode> result = new java.util.ArrayList<>();
        for (ErrorCode error : values()) {
            if (error.getErrorCode().startsWith("FILE_")) {
                result.add(error);
            }
        }
        return result;
    }
} 