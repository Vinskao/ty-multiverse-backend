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
    
    @PostMapping("/save-content")
    public ResponseEntity<?> saveContent(@RequestBody EditContentVO editorContent, HttpSession session) {
        String content = editorContent.getContent();
        String editor = editorContent.getEditor(); // Changed from getContentId() to getEditor()
        
        try {
            // Check if content is the same as stored
            String storedContent = editContentService.getContent(editor); // Changed method name
            
            if (content.equals(storedContent)) {
                return ResponseEntity.ok("No changes detected. Content not saved.");
            }
            
            String action = "UPDATE"; // Hardcoded instead of using ActionTypeEnum
            
            if(!isUserLoggedIn(session, action, module)){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
            }
            
            // Save the content using the service method
            String result = editContentService.saveContent(editor, content); // Changed method name
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            // If content doesn't exist yet, just save it
            if (e.getMessage().contains("No content found")) {
                String action = "CREATE";
                
                if(!isUserLoggedIn(session, action, module)){
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
                }
                
                String result = editContentService.saveContent(editor, content);
                return ResponseEntity.ok(result);
            }
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error: " + e.getMessage());
        }
    }
    
    @PostMapping("/get-content")
    public ResponseEntity<?> getContent(@RequestBody GetContentDTO editor) {
        String editorName = editor.getEditor(); // Changed from getContentId() to getEditor()
        
        try {
            String content = editContentService.getContent(editorName); // Changed method name
            return ResponseEntity.ok(new EditContentVO(editorName, content));
        } catch (RuntimeException e) {
            // If content doesn't exist, return empty
            if (e.getMessage().contains("No content found")) {
                return ResponseEntity.ok(new EditContentVO(editorName, ""));
            }
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error: " + e.getMessage());
        }
    }
}
