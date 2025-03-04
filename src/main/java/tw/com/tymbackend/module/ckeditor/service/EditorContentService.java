package com.mli.dashboard.modules.ckeditor.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mli.dashboard.modules.ckeditor.dao.EditorContentMapper;

@Service
public class EditorContentService {

    @Autowired
    private EditorContentMapper editorContentMapper;

    /**
     * 儲存編輯器內容
     * 
     * @param editor  編輯器名稱
     * @param content 儲存的內容
     * @return 成功訊息或異常
     */
    public String saveContent(String editor, String content) {
        try {
            int affectedRows = editorContentMapper.upsertContent(editor, content);
            if (affectedRows > 0) {
                return "Content saved successfully!";
            }
            return "Failed to save content.";
        } catch (Exception e) {
            throw new RuntimeException("Error while saving content: " + e.getMessage(), e);
        }
    }

    /**
     * 讀取編輯器內容
     * 
     * @param editor 編輯器名稱
     * @return 儲存的內容
     */
    public String getContent(String editor) {
        try {
            String content = editorContentMapper.findContentByEditor(editor);
            if (content == null) {
                throw new RuntimeException("No content found for editor: " + editor);
            }
            return content;
        } catch (Exception e) {
            throw new RuntimeException("Error while retrieving content: " + e.getMessage(), e);
        }
    }
}
