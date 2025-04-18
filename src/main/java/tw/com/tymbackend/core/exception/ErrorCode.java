package tw.com.tymbackend.core.exception;

import org.springframework.http.HttpStatus;

/**
 * 錯誤代碼枚舉
 * 使用標準 HTTP 狀態碼
 */
public enum ErrorCode {
    
    // 通用錯誤
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "內部伺服器錯誤"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "無效的請求"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "找不到資源"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "未授權"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "禁止訪問"),
    CONFLICT(HttpStatus.CONFLICT, "資源衝突"),
    
    // 資料庫錯誤
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "實體不存在"),
    DUPLICATE_ENTRY(HttpStatus.CONFLICT, "重複的資料"),
    OPTIMISTIC_LOCKING_FAILURE(HttpStatus.CONFLICT, "資料已被其他使用者修改"),
    
    // 業務邏輯錯誤
    INVALID_OPERATION(HttpStatus.BAD_REQUEST, "無效的操作"),
    INVALID_STATE(HttpStatus.BAD_REQUEST, "無效的狀態"),
    BUSINESS_RULE_VIOLATION(HttpStatus.BAD_REQUEST, "違反業務規則"),
    
    // 外部服務錯誤
    EXTERNAL_SERVICE_ERROR(HttpStatus.BAD_GATEWAY, "外部服務錯誤"),
    EXTERNAL_SERVICE_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "外部服務超時"),
    
    // 檔案操作錯誤
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "檔案不存在"),
    FILE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "檔案上傳錯誤"),
    FILE_DOWNLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "檔案下載錯誤"),
    INVALID_FILE_FORMAT(HttpStatus.BAD_REQUEST, "無效的檔案格式"),
    FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, "檔案太大");
    
    private final HttpStatus httpStatus;
    private final String message;
    
    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
    
    public int getCode() {
        return httpStatus.value();
    }
    
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
    
    public String getMessage() {
        return message;
    }
} 