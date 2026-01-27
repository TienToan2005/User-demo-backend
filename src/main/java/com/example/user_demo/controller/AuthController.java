package com.example.user_demo.controller;

import com.example.user_demo.dto.request.LoginRequest;
import com.example.user_demo.dto.request.RegisterRequest;
import com.example.user_demo.dto.request.TokenRequest;
import com.example.user_demo.dto.response.ApiResponse;
import com.example.user_demo.dto.response.TokenResponse;
import com.example.user_demo.dto.response.UserResponse;
import com.example.user_demo.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request){
        TokenResponse token = authService.login(request);
        return ApiResponse.<TokenResponse>builder()
                .code(0)
                .message("success")
                .result(token)
                .build();
    }
    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@Valid @RequestBody RegisterRequest request){
        UserResponse user = authService.register(request);
        return ApiResponse.<UserResponse>builder()
                .code(0)
                .message("success")
                .result(user)
                .build();
    }
    @Transactional
    @PostMapping("/refresh")
    public  ApiResponse<TokenResponse> refresh(@RequestBody TokenRequest request){
        TokenResponse token = authService.refresh(request);
        return  ApiResponse.<TokenResponse>builder()
                .code(0)
                .message("success")
                .result(token)
                .build();
    }
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        authService.logout(email);
        return ResponseEntity.noContent().build();
    }

}
