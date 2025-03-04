package com.mli.dashboard.modules.ckeditor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mli.dashboard.core.constants.ActionTypeEnum;
import com.mli.dashboard.core.constants.ModuleEnum;
import com.mli.dashboard.core.keycloak.controller.KeycloakController;
import com.mli.dashboard.modules.ckeditor.bean.dto.GetContentDTO;
import com.mli.dashboard.modules.ckeditor.bean.vo.EditContentVO;
import com.mli.dashboard.modules.ckeditor.service.EditorContentService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/ckeditor")
public class FileUploadController {
    private String module = ModuleEnum.ckeditor.name();

    @Autowired
    private EditorContentService editorContentService;
    @Autowired
    private KeycloakController keycloakController;

    /**
     * 檢查使用者是否已登入
     * 
     * @param session HTTP 會話
     * @return 如果使用者已登入則返回 true，否則返回 false
     */
    private boolean isUserLoggedIn(HttpSession session, String action, String module){
        
        ResponseEntity<String> response = keycloakController.checkSession(session, action, module);
        return response.getStatusCode() == HttpStatus.OK;
	}

    // 儲存編輯器內容
    @PostMapping("/save-content")
    public ResponseEntity<?> saveContent(@RequestBody EditContentVO editorContent, HttpSession session) {
        String existingContent = editorContentService.getContent(editorContent.getEditor());
        if (existingContent.equals(editorContent.getContent())) {
            return ResponseEntity.ok("No changes detected. Content not saved.");
        }
        String action = ActionTypeEnum.UPDATE.name();

        if(!isUserLoggedIn(session, action, module)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
        }
        try {
            String message = editorContentService.saveContent(editorContent.getEditor(), editorContent.getContent());
            
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 讀取編輯器內容
    @PostMapping("/get-content")
    public ResponseEntity<?> getContent(@RequestBody GetContentDTO editor) {

        try {
            String content = editorContentService.getContent(editor.getEditor());

            return ResponseEntity.ok(new EditContentVO(editor.getEditor(), content));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
