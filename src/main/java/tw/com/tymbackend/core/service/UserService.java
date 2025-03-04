package tw.com.tymbackend.core.service;

import tw.com.tymbackend.core.domain.dto.SignUpUserRequest;
import tw.com.tymbackend.core.domain.vo.User;

public interface UserService {
    // Define the methods that UserApplicationService should implement
    User signUp(SignUpUserRequest request);
} 