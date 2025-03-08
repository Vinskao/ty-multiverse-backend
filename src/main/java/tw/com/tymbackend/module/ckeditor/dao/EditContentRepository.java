package tw.com.tymbackend.module.ckeditor.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tw.com.tymbackend.module.ckeditor.domain.vo.EditContentVO;

@Repository
public interface EditContentRepository extends JpaRepository<EditContentVO, String> {
    
    /**
     * 根據 editor 查詢儲存的內容
     * 
     * @param editor 編輯器名稱
     * @return 對應的 EditorContent 實體
     */
    EditContentVO findByEditor(String editor);
}