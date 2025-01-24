package tw.com.tymbackend.service;

import tw.com.tymbackend.domain.vo.User;
import tw.com.tymbackend.dto.SignUpUserRequest;

public interface UserService {
    // Define the methods that UserApplicationService should implement
    User signUp(SignUpUserRequest request);
} 