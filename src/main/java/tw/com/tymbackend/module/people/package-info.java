/**
 * 人員管理模組
 * 
 * <p>此模組負責人員相關的業務功能，包括人員信息管理、圖片處理和武器傷害計算。
 * 提供完整的人員管理解決方案。</p>
 * 
 * <h2>主要功能</h2>
 * <ul>
 *   <li><strong>人員信息管理</strong>：人員基本信息的增刪改查</li>
 *   <li><strong>圖片處理</strong>：人員圖片的上傳、存儲和管理</li>
 *   <li><strong>武器傷害計算</strong>：基於人員屬性的傷害計算</li>
 *   <li><strong>數據統計</strong>：人員相關的統計分析</li>
 * </ul>
 * 
 * <h2>技術特點</h2>
 * <ul>
 *   <li>多數據源支持：使用獨立的 People 數據庫</li>
 *   <li>圖片處理：支持多種圖片格式</li>
 *   <li>緩存機制：提高查詢性能</li>
 *   <li>事務管理：確保數據一致性</li>
 * </ul>
 * 
 * <h2>API 端點</h2>
 * <ul>
 *   <li><code>/people</code>：人員基本信息管理</li>
 *   <li><code>/people/image</code>：人員圖片管理</li>
 *   <li><code>/weapon/damage</code>：武器傷害計算</li>
 * </ul>
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
package tw.com.tymbackend.module.people; 