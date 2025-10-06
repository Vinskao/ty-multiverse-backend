package tw.com.tymbackend.module.ckeditor.domain.dto;

import java.util.Objects;

public class GetContentDTO {
    private String editor;

    public GetContentDTO() {
    }

    public GetContentDTO(String editor) {
        this.editor = editor;
    }

    public String getEditor() {
        return this.editor;
    }

    public void setEditor(String editor) {
        this.editor = editor;
    }

    public GetContentDTO editor(String editor) {
        setEditor(editor);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof GetContentDTO)) {
            return false;
        }
        GetContentDTO saveContentVO = (GetContentDTO) o;
        return Objects.equals(editor, saveContentVO.editor);
    }

    @Override
    public String toString() {
        return "{" +
                " editor='" + getEditor() + "'" +
                "}";
    }

}
