package tw.com.tymbackend.module.ckeditor.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import tw.com.tymbackend.core.repository.DataAccessor;
import tw.com.tymbackend.module.ckeditor.dao.EditContentRepository;
import tw.com.tymbackend.module.ckeditor.dao.EditContentVORepository;
import tw.com.tymbackend.module.ckeditor.domain.vo.EditContentVO;

@Service
public class EditContentService {

    private final DataAccessor<EditContentVO, String> editContentDataAccessor;
    private final EditContentRepository editContentRepository;
    private final EditContentVORepository editContentVORepository;

    public EditContentService(DataAccessor<EditContentVO, String> editContentDataAccessor, 
                             EditContentRepository editContentRepository,
                             EditContentVORepository editContentVORepository) {
        this.editContentDataAccessor = editContentDataAccessor;
        this.editContentRepository = editContentRepository;
        this.editContentVORepository = editContentVORepository;
    }

    /**
     * 儲存編輯器內容
     * 
     * @param editContentVO 要儲存的內容物件
     * @return 儲存後的內容物件
     */
    public EditContentVO saveContent(EditContentVO editContentVO) {
        return editContentDataAccessor.save(editContentVO);
    }

    /**
     * 讀取編輯器內容
     * 
     * @param editor 編輯器名稱
     * @return 儲存的內容，如果找不到則返回空的Optional
     */
    public Optional<EditContentVO> getContent(String editor) {
        return editContentDataAccessor.findById(editor);
    }
}
