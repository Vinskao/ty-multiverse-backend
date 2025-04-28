package tw.com.tymbackend.module.ckeditor.service;

import org.springframework.stereotype.Service;
import tw.com.tymbackend.module.ckeditor.dao.EditContentRepository;
import tw.com.tymbackend.module.ckeditor.domain.vo.EditContentVO;

import java.util.Optional;

@Service
public class EditContentService {
    private final EditContentRepository editContentRepository;

    public EditContentService(EditContentRepository editContentRepository) {
        this.editContentRepository = editContentRepository;
    }

    /**
     * 儲存編輯器內容
     * 
     * @param editContentVO 要儲存的內容物件
     * @return 儲存後的內容物件
     */
    public EditContentVO save(EditContentVO editContentVO) {
        return editContentRepository.save(editContentVO);
    }

    /**
     * 讀取編輯器內容
     * 
     * @param editor 編輯器名稱
     * @return 儲存的內容，如果找不到則返回空的Optional
     */
    public Optional<EditContentVO> findById(String editor) {
        return editContentRepository.findById(editor);
    }
    
    /**
     * 儲存編輯器內容
     * 
     * @param editContentVO 要儲存的內容物件
     * @return 儲存後的內容物件
     */
    public EditContentVO saveContent(EditContentVO editContentVO) {
        return save(editContentVO);
    }
    
    /**
     * 讀取編輯器內容
     * 
     * @param editor 編輯器名稱
     * @return 儲存的內容，如果找不到則返回空的Optional
     */
    public Optional<EditContentVO> getContent(String editor) {
        return findById(editor);
    }
}
