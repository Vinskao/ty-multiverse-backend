package tw.com.tymbackend.module.ckeditor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tw.com.tymbackend.module.ckeditor.domain.dto.GetContentDTO;
import tw.com.tymbackend.module.ckeditor.domain.vo.EditContentVO;
import tw.com.tymbackend.module.ckeditor.service.EditContentService;

import jakarta.servlet.http.HttpSession;

import java.util.Optional;

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
        // Comment out the actual authentication check
        // ResponseEntity<String> response = keycloakController.checkSession(session, action, module);
        // return response.getStatusCode() == HttpStatus.OK;
        
        // Always return true for now (bypass authentication)
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
        String editor = editorContent.getEditor(); // Changed from getContentId() to getEditor()
        
        try {
            // Check if content is the same as stored
            Optional<EditContentVO> storedContentOpt = editContentService.getContent(editor);
            
            // 如果內容存在且內容相同，則不保存
            if (storedContentOpt.isPresent() && content.equals(storedContentOpt.get().getContent())) {
                return ResponseEntity.ok("No changes detected. Content not saved.");
            }
            
            // 檢查使用者是否已登入
            String action = "UPDATE"; // Hardcoded instead of using ActionTypeEnum
            
            // 如果使用者未登入，則返回401 Unauthorized
            if(!isUserLoggedIn(session, action, module)){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
            }
            
            // Save the content using the service method
            editContentService.saveContent(editorContent);
            return ResponseEntity.ok("Content saved successfully!");
        } catch (RuntimeException e) {
            // If content doesn't exist yet, just save it
            if (e.getMessage().contains("No content found")) {
                String action = "CREATE";
                
                if(!isUserLoggedIn(session, action, module)){
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
                }
                
                // Save the content using the service method
                editContentService.saveContent(editorContent);
                return ResponseEntity.ok("Content saved successfully!");
            }
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error: " + e.getMessage());
        }
    }
    
    @PostMapping("/get-content")
    public ResponseEntity<?> getContent(@RequestBody GetContentDTO editor) {
        String editorName = editor.getEditor(); // Changed from getContentId() to getEditor()
        
        try {
            Optional<EditContentVO> contentOpt = editContentService.getContent(editorName);
            
            if (contentOpt.isPresent()) {
                return ResponseEntity.ok(contentOpt.get());
            } else {
                // If content doesn't exist, return empty
                return ResponseEntity.ok(new EditContentVO(editorName, ""));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error: " + e.getMessage());
        }
    }
}
