package tw.com.tymbackend.module.ckeditor.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import tw.com.tymbackend.core.factory.RepositoryFactory;
import tw.com.tymbackend.core.service.BaseService;
import tw.com.tymbackend.module.ckeditor.dao.EditContentRepository;
import tw.com.tymbackend.module.ckeditor.dao.EditContentVORepository;
import tw.com.tymbackend.module.ckeditor.domain.vo.EditContentVO;

@Service
public class EditContentService extends BaseService {

    private final RepositoryFactory repositoryFactory;
    private final EditContentRepository editContentRepository;
    private final EditContentVORepository editContentVORepository;

    public EditContentService(RepositoryFactory repositoryFactory, 
                             EditContentRepository editContentRepository,
                             EditContentVORepository editContentVORepository) {
        this.repositoryFactory = repositoryFactory;
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
        return repositoryFactory.save(editContentVO);
    }

    /**
     * 讀取編輯器內容
     * 
     * @param editor 編輯器名稱
     * @return 儲存的內容，如果找不到則返回空的Optional
     */
    public Optional<EditContentVO> getContent(String editor) {
        return repositoryFactory.findById(EditContentVO.class, editor);
    }
}
