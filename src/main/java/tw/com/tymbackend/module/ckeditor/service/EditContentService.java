package tw.com.tymbackend.module.ckeditor.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tw.com.tymbackend.module.ckeditor.dao.EditContentRepository;
import tw.com.tymbackend.module.ckeditor.domain.vo.EditContentVO;

@Service
public class EditContentService {

    @Autowired
    private EditContentRepository editContentRepository;

    /**
     * 儲存編輯器內容
     * 
     * @param editor  編輯器名稱
     * @param content 儲存的內容
     * @return 成功訊息或異常
     */
    @Transactional
    public String saveContent(String editor, String content) {
        try {
            // Create new entity or update existing one
            EditContentVO contentVO = editContentRepository.findByEditor(editor);
            
            if (contentVO == null) {
                // Create new entity
                contentVO = new EditContentVO();
                contentVO.setEditor(editor);
            }
            
            contentVO.setContent(content);
            editContentRepository.save(contentVO);
            
            return "Content saved successfully!";
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
            EditContentVO contentVO = editContentRepository.findByEditor(editor);
            if (contentVO == null) {
                throw new RuntimeException("No content found for editor: " + editor);
            }
            return contentVO.getContent();
        } catch (Exception e) {
            throw new RuntimeException("Error while retrieving content: " + e.getMessage(), e);
        }
    }
}
