package com.example.user_demo.controller;

import com.example.user_demo.dto.request.UserRequest;
import com.example.user_demo.dto.response.ApiResponse;
import com.example.user_demo.dto.response.PageResponse;
import com.example.user_demo.dto.response.UserResponse;
import com.example.user_demo.enums.UserStatus;
import com.example.user_demo.repository.UserRepository;
import com.example.user_demo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> getMe(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserResponse user = userService.getByEmail(email);
        return ApiResponse.<UserResponse>builder()
                .code(0)
                .result(user)
                .message("success")
                .build();
    }
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getById(@PathVariable Long id){
        UserResponse user = userService.getById(id);
        return ApiResponse.<UserResponse>builder()
                .code(0)
                .message("success")
                .result(user)
                .build();
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ApiResponse<PageResponse<UserResponse>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UserStatus status,
            Pageable pageable
    ) {
        PageResponse<UserResponse> response = userService.getUsers(keyword, status, pageable);
        return ApiResponse.<PageResponse<UserResponse>>builder()
                .code(0)
                .message("success")
                .result(response)
                .build();
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ApiResponse<UserResponse> update(@PathVariable Long id ,@RequestBody @Valid UserRequest request){
        UserResponse userResponse =  userService.update(id, request);
        return ApiResponse.<UserResponse>builder()
                .code(0)
                .message("success")
                .result(userResponse)
                .build();
    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
