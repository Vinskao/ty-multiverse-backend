/**
 * 業務模組包 - 業務功能模組
 * 
 * <p>此包包含應用程序的業務功能模組，每個模組代表一個獨立的業務領域。
 * 採用模組化設計，便於維護和擴展。</p>
 * 
 * <h2>模組架構</h2>
 * <p>每個業務模組都遵循相同的分層架構：</p>
 * <ul>
 *   <li><strong>controller</strong>：控制器層，處理 HTTP 請求</li>
 *   <li><strong>dao</strong>：數據訪問層，處理數據庫操作</li>
 *   <li><strong>domain</strong>：領域層，包含 DTO 和 VO</li>
 *   <li><strong>service</strong>：服務層，處理業務邏輯</li>
 *   <li><strong>exception</strong>：異常處理，模組特定的異常</li>
 * </ul>
 * 
 * <h2>業務模組</h2>
 * <ul>
 *   <li><strong>ckeditor</strong>：富文本編輯器模組</li>
 *   <li><strong>deckofcards</strong>：撲克牌遊戲模組</li>
 *   <li><strong>gallery</strong>：畫廊管理模組</li>
 *   <li><strong>livestock</strong>：畜牧業管理模組</li>
 *   <li><strong>people</strong>：人員管理模組</li>
 *   <li><strong>weapon</strong>：武器管理模組</li>
 * </ul>
 * 
 * <h2>設計特點</h2>
 * <ul>
 *   <li>模組獨立：每個模組可以獨立開發和部署</li>
 *   <li>統一接口：所有模組遵循相同的 API 規範</li>
 *   <li>數據隔離：模組間的數據相對獨立</li>
 *   <li>可擴展性：易於添加新的業務模組</li>
 * </ul>
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
package tw.com.tymbackend.module; 