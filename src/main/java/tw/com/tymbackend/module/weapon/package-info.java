/**
 * 武器管理模組
 * 
 * <p>此模組負責武器相關的業務功能，包括武器信息管理、屬性計算和傷害分析。
 * 提供完整的武器管理系統。</p>
 * 
 * <h2>主要功能</h2>
 * <ul>
 *   <li><strong>武器信息管理</strong>：武器基本信息的增刪改查</li>
 *   <li><strong>屬性計算</strong>：武器屬性的計算和分析</li>
 *   <li><strong>傷害分析</strong>：武器傷害的計算和評估</li>
 *   <li><strong>武器分類</strong>：武器類型的分類和管理</li>
 * </ul>
 * 
 * <h2>技術特點</h2>
 * <ul>
 *   <li>向量搜索：支持武器屬性的相似性搜索</li>
 *   <li>緩存優化：提高查詢性能</li>
 *   <li>數據驗證：確保武器數據的完整性</li>
 *   <li>統計分析：提供武器使用統計</li>
 * </ul>
 * 
 * <h2>API 端點</h2>
 * <ul>
 *   <li><code>/weapon</code>：武器基本信息管理</li>
 *   <li><code>/weapon/search</code>：武器搜索功能</li>
 *   <li><code>/weapon/analysis</code>：武器分析功能</li>
 * </ul>
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
package tw.com.tymbackend.module.weapon; 