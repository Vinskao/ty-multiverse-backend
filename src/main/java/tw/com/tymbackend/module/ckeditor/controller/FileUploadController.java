package tw.com.tymbackend.module.ckeditor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tw.com.ty.common.response.ErrorCode;
import tw.com.tymbackend.module.ckeditor.domain.dto.GetContentDTO;
import tw.com.tymbackend.module.ckeditor.service.EditContentService;
import tw.com.tymbackend.module.ckeditor.domain.vo.EditContentVO;

import jakarta.servlet.http.HttpSession;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.Map;
import java.util.HashMap;

/**
 * 檔案上傳控制器
 */ 
@RestController
@RequestMapping("/ckeditor")
public class FileUploadController {
    // private String module = ModuleEnum.ckeditor.name();
    private String module = "ckeditor"; // Hardcoded instead of using ModuleEnum

    @Autowired
    private EditContentService editContentService;
    
    // @Autowired
    // private KeycloakController keycloakController;

    /**
     * 檢查使用者是否已登入
     * 
     * @param session HTTP 會話
     * @return 如果使用者已登入則返回 true，否則返回 false
     */
    private boolean isUserLoggedIn(HttpSession session, String action, String module){
        // 檢查 Session 是否存在
        if (session == null) {
            return false;
        }
        
        // 檢查用戶是否已認證
        Object userAttribute = session.getAttribute("user_authenticated");
        if (userAttribute == null) {
            return false;
        }
        
        // 記錄用戶活動
        session.setAttribute("last_activity", System.currentTimeMillis());
        session.setAttribute("last_action", action);
        session.setAttribute("last_module", module);
        
        return true;
    }
    
    /**
     * 儲存編輯器內容
     * 
     * @param editorContent 編輯器內容物件
     * @param session HTTP 會話
     * @return 儲存後的內容物件
     */
    @PostMapping("/save-content")
    public ResponseEntity<?> saveContent(@RequestBody EditContentVO editorContent, HttpSession session) {
        String content = editorContent.getContent();
        String editor = editorContent.getEditor();
        
        try {
            // 檢查使用者是否已登入
            String action = "UPDATE";
            
            if(!isUserLoggedIn(session, action, module)){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorCode.USER_NOT_LOGGED_IN.getMessage());
            }
            
            // 保存編輯狀態到 Session
            session.setAttribute("editor_draft_" + editor, content);
            session.setAttribute("editor_last_save_" + editor, System.currentTimeMillis());
            
            // 檢查內容是否有變化
            Optional<EditContentVO> storedContentOpt = editContentService.getContent(editor).get();
            
            if (storedContentOpt.isPresent() && content.equals(storedContentOpt.get().getContent())) {
                // 內容相同，只更新 Session 中的草稿
                return ResponseEntity.ok("No changes detected. Draft saved to session.");
            }
            
            // 保存內容
            editContentService.saveContent(editorContent);
            
            // 清除草稿，因為已保存
            session.removeAttribute("editor_draft_" + editor);
            
            return ResponseEntity.ok("Content saved successfully!");
        } catch (RuntimeException e) {
            if (e.getMessage().contains("No content found")) {
                String action = "CREATE";
                
                if(!isUserLoggedIn(session, action, module)){
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorCode.USER_NOT_LOGGED_IN.getMessage());
                }
                
                // 保存新內容
                editContentService.saveContent(editorContent);
                return ResponseEntity.ok("Content created successfully!");
            }
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error: " + e.getMessage());
        }
    }
    
    @PostMapping("/get-content")
    public ResponseEntity<?> getContent(@RequestBody GetContentDTO editor) {
        String editorName = editor.getEditor(); // Changed from getContentId() to getEditor()
        
        try {
            Optional<EditContentVO> contentOpt = editContentService.getContent(editorName).get();
            
            if (contentOpt.isPresent()) {
                return ResponseEntity.ok(contentOpt.get());
            } else {
                // If content doesn't exist, return empty
                return ResponseEntity.ok(new EditContentVO(editorName, ""));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error: " + e.getMessage());
        }
    }
}
