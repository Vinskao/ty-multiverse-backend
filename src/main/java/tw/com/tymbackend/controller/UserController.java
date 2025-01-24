package tw.com.tymbackend.controller;

import tw.com.tymbackend.domain.vo.User;
import tw.com.tymbackend.dto.SignUpUserRequest;
import tw.com.tymbackend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<User> signUp(@RequestBody SignUpUserRequest request) {
        User user = userService.signUp(request);
        return ResponseEntity.ok(user);
    }
} 