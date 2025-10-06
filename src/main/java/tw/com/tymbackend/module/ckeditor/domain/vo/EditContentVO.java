package tw.com.tymbackend.module.ckeditor.domain.vo;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * CKEditor 內容實體類別，代表系統中的編輯器內容資訊
 * 
 * <p>此類別包含編輯器的基本資訊和內容資料，
 * 用於儲存和管理 CKEditor 的編輯內容。</p>
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "ckeditor")
public class EditContentVO {
    
    /**
     * 編輯器ID（主鍵）
     * <p>作為實體的唯一識別符，長度限制為20個字元</p>
     */
    @Id
    @Column(name = "editor", length = 20)
    private String editor;
    
    /**
     * 編輯器內容
     * <p>儲存 CKEditor 的編輯內容，支援長文字格式</p>
     */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /**
     * 預設建構函數
     */
    public EditContentVO() {
    }

    /**
     * 編輯器內容建構函數
     * 
     * @param editor 編輯器ID
     * @param content 編輯器內容
     */
    public EditContentVO(String editor, String content) {
        this.editor = editor;
        this.content = content;
    }

    /**
     * 取得編輯器ID（等同於編輯器名稱）
     * 
     * @return 編輯器ID
     */
    public String getId() {
        return this.editor;
    }

    /**
     * 設定編輯器ID（等同於編輯器名稱）
     * 
     * @param id 編輯器ID
     */
    public void setId(String id) {
        this.editor = id;
    }
    
    /**
     * 取得編輯器ID
     * 
     * @return 編輯器ID
     */
    public String getEditor() {
        return this.editor;
    }

    /**
     * 設定編輯器ID
     * 
     * @param editor 編輯器ID
     */
    public void setEditor(String editor) {
        this.editor = editor;
    }

    /**
     * 取得編輯器內容
     * 
     * @return 編輯器內容
     */
    public String getContent() {
        return this.content;
    }

    /**
     * 設定編輯器內容
     * 
     * @param content 編輯器內容
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 設定編輯器ID並返回當前物件（方法鏈式調用）
     * 
     * @param editor 編輯器ID
     * @return 當前物件
     */
    public EditContentVO editor(String editor) {
        setEditor(editor);
        return this;
    }

    /**
     * 設定編輯器內容並返回當前物件（方法鏈式調用）
     * 
     * @param content 編輯器內容
     * @return 當前物件
     */
    public EditContentVO content(String content) {
        setContent(content);
        return this;
    }

    /**
     * 比較此編輯器內容與指定物件是否相等
     * 
     * @param o 要比較的物件
     * @return 如果相等則返回true，否則返回false
     */
    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof EditContentVO)) {
            return false;
        }
        EditContentVO editContentVO = (EditContentVO) o;
        return Objects.equals(editor, editContentVO.editor) && Objects.equals(content, editContentVO.content);
    }

    /**
     * 計算此編輯器內容物件的雜湊碼
     * 
     * @return 雜湊碼值
     */
    @Override
    public int hashCode() {
        return Objects.hash(editor, content);
    }

    /**
     * 返回此編輯器內容物件的字串表示
     * 
     * @return 包含主要屬性的字串表示
     */
    @Override
    public String toString() {
        return "{" +
                " editor='" + getEditor() + "'" +
                ", content='" + getContent() + "'" +
                "}";
    }

}
