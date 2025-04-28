package tw.com.tymbackend.module.ckeditor.dao;

import org.springframework.stereotype.Repository;
import tw.com.tymbackend.core.repository.StringPkRepository;
import tw.com.tymbackend.module.ckeditor.domain.vo.EditContentVO;

/**
 * Repository for EditContentVO entity
 */
@Repository
public interface EditContentVORepository extends StringPkRepository<EditContentVO> {
    
    /**
     * Find EditContentVO by editor name
     * 
     * @param editor the editor name
     * @return the EditContentVO with the given editor name
     */
    default EditContentVO findByEditor(String editor) {
        return findById(editor).orElse(null);
    }
} 