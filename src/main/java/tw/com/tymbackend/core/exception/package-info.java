/**
 * 異常處理包 - 統一異常管理
 * 
 * <p>此包包含應用程序的異常處理機制，提供統一的錯誤處理和響應格式。
 * 確保應用程序的穩定性和用戶體驗。</p>
 * 
 * <h2>異常類型</h2>
 * <ul>
 *   <li><strong>BusinessException</strong>：業務邏輯異常</li>
 *   <li><strong>ErrorCode</strong>：錯誤碼定義</li>
 *   <li><strong>ErrorResponse</strong>：錯誤響應格式</li>
 *   <li><strong>GlobalExceptionHandler</strong>：全局異常處理器</li>
 * </ul>
 * 
 * <h2>設計特點</h2>
 * <ul>
 *   <li>統一格式：標準化的錯誤響應格式</li>
 *   <li>錯誤碼管理：系統化的錯誤碼定義</li>
 *   <li>日誌記錄：完整的異常日誌記錄</li>
 *   <li>用戶友好：提供用戶可理解的錯誤信息</li>
 * </ul>
 * 
 * <h2>使用方式</h2>
 * <ul>
 *   <li>拋出業務異常：<code>throw new BusinessException(ErrorCode.XXX)</code></li>
 *   <li>自定義錯誤碼：在 <code>ErrorCode</code> 中定義新的錯誤碼</li>
 *   <li>全局處理：異常會被 <code>GlobalExceptionHandler</code> 自動處理</li>
 * </ul>
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
package tw.com.tymbackend.core.exception; 