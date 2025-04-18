package tw.com.tymbackend.module.ckeditor.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tw.com.tymbackend.module.ckeditor.domain.vo.EditContentVO;

/**
 * Repository for EditContentVO entity
 */
@Repository
public interface EditContentVORepository extends JpaRepository<EditContentVO, String> {
    
    /**
     * Find EditContentVO by editor name
     * 
     * @param editor the editor name
     * @return the EditContentVO with the given editor name
     */
    EditContentVO findByEditor(String editor);
} 