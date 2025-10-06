/**
 * 核心包 - 基礎設施和通用功能
 * 
 * <p>此包包含應用程序的基礎設施代碼和通用功能，為所有業務模組提供支持。
 * 採用分層架構設計，確保代碼的可維護性和可擴展性。</p>
 * 
 * <h2>主要組件</h2>
 * <ul>
 *   <li><strong>config</strong>：配置類，包含各種框架和組件的配置</li>
 *   <li><strong>controller</strong>：控制器層，處理 HTTP 請求和 WebSocket 消息</li>
 *   <li><strong>datasource</strong>：數據源配置，支持多數據源</li>
 *   <li><strong>entity</strong>：實體類，定義基礎實體</li>
 *   <li><strong>exception</strong>：異常處理，統一的錯誤處理機制</li>
 *   <li><strong>repository</strong>：數據訪問層，提供數據庫操作接口</li>
 *   <li><strong>service</strong>：服務層，業務邏輯處理</li>
 *   <li><strong>util</strong>：工具類，提供通用功能</li>
 * </ul>
 * 
 * <h2>設計原則</h2>
 * <ul>
 *   <li>單一職責原則：每個類都有明確的職責</li>
 *   <li>依賴注入：使用 Spring 的 IoC 容器</li>
 *   <li>異常處理：統一的異常處理機制</li>
 *   <li>日誌記錄：完整的日誌記錄體系</li>
 * </ul>
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
package tw.com.tymbackend.core; 