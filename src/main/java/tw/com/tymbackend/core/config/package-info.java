/**
 * 配置包 - 應用程序配置
 * 
 * <p>此包包含應用程序的所有配置類，負責配置各種框架和組件。
 * 採用模組化配置設計，便於管理和維護。</p>
 * 
 * <h2>配置模組</h2>
 * <ul>
 *   <li><strong>cache</strong>：緩存配置，Redis 緩存設置</li>
 *   <li><strong>database</strong>：數據庫配置，JPA 和數據源設置</li>
 *   <li><strong>http</strong>：HTTP 配置，RestTemplate 和 Web 設置</li>
 *   <li><strong>metrics</strong>：監控配置，Micrometer 度量設置</li>
 *   <li><strong>security</strong>：安全配置，Spring Security 和 Keycloak 設置</li>
 *   <li><strong>thread</strong>：線程配置，異步處理設置</li>
 *   <li><strong>websocket</strong>：WebSocket 配置，實時通信設置</li>
 * </ul>
 * 
 * <h2>配置特點</h2>
 * <ul>
 *   <li>環境分離：支持本地和生產環境的不同配置</li>
 *   <li>條件配置：根據環境變數動態配置</li>
 *   <li>安全配置：敏感信息通過環境變數注入</li>
 *   <li>監控配置：完整的應用程序監控體系</li>
 * </ul>
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
package tw.com.tymbackend.core.config; 