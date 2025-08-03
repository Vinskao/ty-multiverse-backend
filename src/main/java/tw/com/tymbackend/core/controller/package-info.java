/**
 * 控制器包 - HTTP 請求和 WebSocket 處理
 * 
 * <p>此包包含應用程序的控制器類，負責處理 HTTP 請求和 WebSocket 消息。
 * 採用 RESTful API 設計，提供統一的接口規範。</p>
 * 
 * <h2>主要功能</h2>
 * <ul>
 *   <li><strong>HTTP 控制器</strong>：處理 REST API 請求</li>
 *   <li><strong>WebSocket 控制器</strong>：處理實時通信</li>
 *   <li><strong>度量控制器</strong>：提供應用程序監控數據</li>
 *   <li><strong>認證控制器</strong>：處理身份驗證相關請求</li>
 * </ul>
 * 
 * <h2>設計特點</h2>
 * <ul>
 *   <li>統一響應格式：標準化的 API 響應結構</li>
 *   <li>異常處理：全局異常處理機制</li>
 *   <li>參數驗證：請求參數的驗證和轉換</li>
 *   <li>日誌記錄：完整的請求日誌記錄</li>
 *   <li>安全控制：基於角色的訪問控制</li>
 * </ul>
 * 
 * <h2>API 規範</h2>
 * <ul>
 *   <li>使用標準 HTTP 方法（GET, POST, PUT, DELETE）</li>
 *   <li>返回 JSON 格式的響應</li>
 *   <li>統一的錯誤碼和錯誤信息</li>
 *   <li>支持分頁和排序</li>
 * </ul>
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
package tw.com.tymbackend.core.controller; 