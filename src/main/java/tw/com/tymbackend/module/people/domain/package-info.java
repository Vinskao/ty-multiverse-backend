/**
 * 人員模組領域包 - 數據傳輸對象和值對象
 * 
 * <p>此包包含人員模組的數據傳輸對象（DTO）和值對象（VO），
 * 用於在不同層級之間傳遞數據。</p>
 * 
 * <h2>包結構</h2>
 * <ul>
 *   <li><strong>dto</strong>：數據傳輸對象，用於 API 層的數據傳遞</li>
 *   <li><strong>vo</strong>：值對象，用於業務層的數據表示</li>
 * </ul>
 * 
 * <h2>設計原則</h2>
 * <ul>
 *   <li>數據分離：DTO 和 VO 分離，避免耦合</li>
 *   <li>不可變性：值對象通常是不可變的</li>
 *   <li>驗證機制：包含數據驗證邏輯</li>
 *   <li>序列化支持：支持 JSON 序列化</li>
 * </ul>
 * 
 * <h2>主要組件</h2>
 * <ul>
 *   <li><strong>PeopleResponseDTO</strong>：人員響應數據傳輸對象</li>
 *   <li><strong>PeopleNameRequestDTO</strong>：人員名稱請求數據傳輸對象</li>
 *   <li><strong>People</strong>：人員值對象</li>
 *   <li><strong>PeopleImage</strong>：人員圖片值對象</li>
 * </ul>
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
package tw.com.tymbackend.module.people.domain; 