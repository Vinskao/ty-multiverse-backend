package tw.com.tymbackend.core.exception;

import org.springframework.http.HttpStatus;

/**
 * 錯誤代碼枚舉
 * 
 * 定義系統中所有可能的錯誤代碼，使用標準 HTTP 狀態碼。
 * 每個錯誤碼都包含對應的 HTTP 狀態碼和中文錯誤訊息。
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
public enum ErrorCode {
    
    /**
     * 內部伺服器錯誤
     * 
     * 當系統發生未預期的內部錯誤時使用此錯誤碼。
     * 通常表示系統內部邏輯錯誤、資料庫連接問題或未處理的異常。
     * 
     * @see HttpStatus#INTERNAL_SERVER_ERROR
     */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "內部伺服器錯誤"),
    
    /**
     * 無效的請求
     * 
     * 當客戶端發送的請求格式不正確、缺少必要參數或參數值無效時使用。
     * 例如：缺少必填欄位、參數類型錯誤、JSON 格式錯誤等。
     * 
     * @see HttpStatus#BAD_REQUEST
     */
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "無效的請求"),
    
    /**
     * 找不到資源
     * 
     * 當請求的資源不存在時使用此錯誤碼。
     * 適用於查詢不存在的實體、檔案或 API 端點。
     * 
     * @see HttpStatus#NOT_FOUND
     */
    NOT_FOUND(HttpStatus.NOT_FOUND, "找不到資源"),
    
    /**
     * 未授權
     * 
     * 當用戶未提供有效的認證憑證時使用此錯誤碼。
     * 例如：缺少 JWT Token、Token 已過期或格式錯誤。
     * 
     * @see HttpStatus#UNAUTHORIZED
     */
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "未授權"),
    
    /**
     * 禁止訪問
     * 
     * 當用戶已認證但沒有足夠權限訪問特定資源時使用此錯誤碼。
     * 例如：普通用戶嘗試訪問管理員功能。
     * 
     * @see HttpStatus#FORBIDDEN
     */
    FORBIDDEN(HttpStatus.FORBIDDEN, "禁止訪問"),
    
    /**
     * 資源衝突
     * 
     * 當請求的操作與當前資源狀態衝突時使用此錯誤碼。
     * 例如：嘗試創建已存在的資源、並發修改衝突等。
     * 
     * @see HttpStatus#CONFLICT
     */
    CONFLICT(HttpStatus.CONFLICT, "資源衝突"),
    
    /**
     * 實體不存在
     * 
     * 當嘗試操作（更新、刪除、查詢）一個不存在的資料庫實體時使用此錯誤碼。
     * 通常發生在根據 ID 查詢實體但找不到對應記錄的情況。
     * 
     * @see HttpStatus#NOT_FOUND
     */
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "實體不存在"),
    
    /**
     * 重複的資料
     * 
     * 當嘗試創建或插入已存在的唯一資料時使用此錯誤碼。
     * 例如：重複的用戶名、電子郵件、業務編號等。
     * 
     * @see HttpStatus#CONFLICT
     */
    DUPLICATE_ENTRY(HttpStatus.CONFLICT, "重複的資料"),
    
    /**
     * 樂觀鎖定失敗
     * 
     * 當使用樂觀鎖定機制時，資料已被其他用戶修改導致當前操作失敗時使用此錯誤碼。
     * 通常發生在並發更新同一個實體的情況。
     * 
     * @see HttpStatus#CONFLICT
     */
    OPTIMISTIC_LOCKING_FAILURE(HttpStatus.CONFLICT, "資料已被其他使用者修改"),
    
    /**
     * 無效的操作
     * 
     * 當請求的操作在當前業務邏輯下不被允許時使用此錯誤碼。
     * 例如：在錯誤的狀態下執行特定操作、操作順序錯誤等。
     * 
     * @see HttpStatus#BAD_REQUEST
     */
    INVALID_OPERATION(HttpStatus.BAD_REQUEST, "無效的操作"),
    
    /**
     * 無效的狀態
     * 
     * 當實體或系統處於不允許執行特定操作的狀態時使用此錯誤碼。
     * 例如：已完成的訂單無法再次修改、已停用的帳戶無法登入等。
     * 
     * @see HttpStatus#BAD_REQUEST
     */
    INVALID_STATE(HttpStatus.BAD_REQUEST, "無效的狀態"),
    
    /**
     * 違反業務規則
     * 
     * 當操作違反系統定義的業務規則時使用此錯誤碼。
     * 例如：餘額不足、配額超限、時間限制等業務邏輯限制。
     * 
     * @see HttpStatus#BAD_REQUEST
     */
    BUSINESS_RULE_VIOLATION(HttpStatus.BAD_REQUEST, "違反業務規則"),
    
    /**
     * 外部服務錯誤
     * 
     * 當依賴的外部服務（如第三方 API、微服務等）發生錯誤時使用此錯誤碼。
     * 表示外部服務無法正常響應或返回錯誤。
     * 
     * @see HttpStatus#BAD_GATEWAY
     */
    EXTERNAL_SERVICE_ERROR(HttpStatus.BAD_GATEWAY, "外部服務錯誤"),
    
    /**
     * 外部服務超時
     * 
     * 當調用外部服務時發生超時錯誤時使用此錯誤碼。
     * 表示外部服務在預期時間內未響應。
     * 
     * @see HttpStatus#GATEWAY_TIMEOUT
     */
    EXTERNAL_SERVICE_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "外部服務超時"),
    
    /**
     * 檔案不存在
     * 
     * 當嘗試訪問或下載不存在的檔案時使用此錯誤碼。
     * 適用於檔案系統操作中的檔案查詢失敗。
     * 
     * @see HttpStatus#NOT_FOUND
     */
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "檔案不存在"),
    
    /**
     * 檔案上傳錯誤
     * 
     * 當檔案上傳過程中發生錯誤時使用此錯誤碼。
     * 例如：磁碟空間不足、檔案系統權限問題、網路中斷等。
     * 
     * @see HttpStatus#INTERNAL_SERVER_ERROR
     */
    FILE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "檔案上傳錯誤"),
    
    /**
     * 檔案下載錯誤
     * 
     * 當檔案下載過程中發生錯誤時使用此錯誤碼。
     * 例如：檔案損壞、讀取權限問題、網路中斷等。
     * 
     * @see HttpStatus#INTERNAL_SERVER_ERROR
     */
    FILE_DOWNLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "檔案下載錯誤"),
    
    /**
     * 無效的檔案格式
     * 
     * 當上傳的檔案格式不被系統支援時使用此錯誤碼。
     * 例如：只允許圖片格式但上傳了文檔檔案。
     * 
     * @see HttpStatus#BAD_REQUEST
     */
    INVALID_FILE_FORMAT(HttpStatus.BAD_REQUEST, "無效的檔案格式"),
    
    /**
     * 檔案太大
     * 
     * 當上傳的檔案大小超過系統限制時使用此錯誤碼。
     * 適用於檔案大小限制的業務規則。
     * 
     * @see HttpStatus#BAD_REQUEST
     */
    FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, "檔案太大");
    
    /** HTTP 狀態碼 */
    private final HttpStatus httpStatus;
    
    /** 錯誤訊息 */
    private final String message;
    
    /**
     * 建構子
     * 
     * @param httpStatus HTTP 狀態碼
     * @param message 錯誤訊息
     */
    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
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
     * 取得錯誤訊息
     * 
     * 返回此錯誤碼對應的中文錯誤訊息。
     * 
     * @return 錯誤訊息字串
     */
    public String getMessage() {
        return message;
    }
} 