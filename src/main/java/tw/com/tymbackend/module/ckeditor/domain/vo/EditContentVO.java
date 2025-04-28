package tw.com.tymbackend.module.ckeditor.domain.vo;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "ckeditor")
public class EditContentVO {
    
    @Id
    @Column(name = "editor", length = 20)
    private String editor;
    
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    public EditContentVO() {
    }

    public EditContentVO(String editor, String content) {
        this.editor = editor;
        this.content = content;
    }

    public String getId() {
        return this.editor;
    }

    public void setId(String id) {
        this.editor = id;
    }
    
    public String getEditor() {
        return this.editor;
    }

    public void setEditor(String editor) {
        this.editor = editor;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public EditContentVO editor(String editor) {
        setEditor(editor);
        return this;
    }

    public EditContentVO content(String content) {
        setContent(content);
        return this;
    }

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

    @Override
    public int hashCode() {
        return Objects.hash(editor, content);
    }

    @Override
    public String toString() {
        return "{" +
                " editor='" + getEditor() + "'" +
                ", content='" + getContent() + "'" +
                "}";
    }

}
