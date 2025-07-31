package tw.com.tymbackend.core.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/guardian")
public class Guardian {
    
    // 需要 manage-users 角色的端點
    @PreAuthorize("hasRole('manage-users')")
    @GetMapping("/admin")
    public ResponseEntity<String> adminOnly() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok("Hello " + auth.getName() + "! You have manage-users role.");
    }

    // 登入用戶（包括 GUEST）都可以訪問的端點
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/user")
    public ResponseEntity<String> userAccess() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok("Hello " + auth.getName() + "! You are authenticated.");
    }

    // 公開端點 - 不需要認證
    @GetMapping("/public/info")
    public ResponseEntity<String> publicInfo() {
        return ResponseEntity.ok("This is public information.");
    }
}
