package com.example.user_demo.service;

import com.example.user_demo.config.JwtService;
import com.example.user_demo.dto.request.LoginRequest;
import com.example.user_demo.dto.request.RegisterRequest;
import com.example.user_demo.dto.response.ApiResponse;
import com.example.user_demo.dto.response.TokenResponse;
import com.example.user_demo.dto.response.UserResponse;
import com.example.user_demo.entity.User;
import com.example.user_demo.enums.ErrorCode;
import com.example.user_demo.enums.RoleUser;
import com.example.user_demo.enums.UserStatus;
import com.example.user_demo.exception.AppException;
import com.example.user_demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public TokenResponse login(LoginRequest request){
        String email = request.email().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));

        if(user.getStatus() != UserStatus.ACTIVE){
            throw new AppException(ErrorCode.STATUS_FORBIDDEN);
        }
        boolean matched = passwordEncoder.matches(request.password(), user.getPassword());
        if(!matched){
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
        String token = jwtService.generateToken(user);
        return new TokenResponse(token);
    }
    public UserResponse register(RegisterRequest request){
        String email = request.email().trim().toLowerCase();
        if(userRepository.existsByEmail(email)){
                throw  new AppException(ErrorCode.EMAIL_EXISTED);
        }
        User user = new User();
        user.setEmail(email);
        user.setFullName(request.fullName());
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(RoleUser.USER);
        user.setPassword(passwordEncoder.encode(request.password()));
        User saved = userRepository.save(user);

        return new UserResponse(
                saved.getId(), saved.getEmail(), saved.getFullName(), saved.getStatus()
        );
    }
}
