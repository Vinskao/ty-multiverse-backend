/**
 * 工具包 - 通用工具類
 * 
 * <p>此包包含應用程序的通用工具類，提供各種輔助功能和實用方法。
 * 這些工具類可以在整個應用程序中重複使用。</p>
 * 
 * <h2>主要工具</h2>
 * <ul>
 *   <li><strong>DistributedLockUtil</strong>：分布式鎖工具，用於並發控制</li>
 *   <li><strong>WebSocketUtil</strong>：WebSocket 工具，處理實時通信</li>
 *   <li><strong>DateUtil</strong>：日期時間處理工具</li>
 *   <li><strong>StringUtil</strong>：字符串處理工具</li>
 * </ul>
 * 
 * <h2>功能特點</h2>
 * <ul>
 *   <li>無狀態設計：工具類不保存狀態</li>
 *   <li>線程安全：支持並發使用</li>
 *   <li>高性能：優化的算法實現</li>
 *   <li>易於使用：簡潔的 API 設計</li>
 * </ul>
 * 
 * <h2>使用原則</h2>
 * <ul>
 *   <li>靜態方法：工具類主要提供靜態方法</li>
 *   <li>不可實例化：工具類通常不應該被實例化</li>
 *   <li>單一職責：每個工具類專注於特定功能</li>
 *   <li>充分測試：工具類需要完整的單元測試</li>
 * </ul>
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
package tw.com.tymbackend.core.util; 