package com.example.user_demo.controller;

import com.example.user_demo.dto.request.UpdateUserRequest;
import com.example.user_demo.dto.request.UserRequest;
import com.example.user_demo.dto.response.ApiResponse;
import com.example.user_demo.dto.response.PageResponse;
import com.example.user_demo.dto.response.UserResponse;
import com.example.user_demo.enums.UserStatus;
import com.example.user_demo.service.UserService;
import jakarta.validation.Valid;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> create(@RequestBody @Valid UserRequest request){
        UserResponse user =  userService.create(request);
        return ResponseEntity.status(201).body(ApiResponse.<UserResponse>builder()
                .code(0)
                .result(user)
                .message("success")
                .build());
    }
    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getById(@PathVariable Long id){
        UserResponse user = userService.getById(id);
        return ApiResponse.<UserResponse>builder()
                .result(user)
                .build();
    }
    @GetMapping
    public ApiResponse<PageResponse<UserResponse>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UserStatus status,
            Pageable pageable
    ) {
        PageResponse<UserResponse> response = userService.getUsers(keyword, status, pageable);
        return ApiResponse.<PageResponse<UserResponse>>builder()
                .message("success")
                .result(response)
                .build();
    }
    @PutMapping("/{id}")
    public ApiResponse<UserResponse> update(@PathVariable Long id ,@RequestBody @Valid UserRequest request){
        UserResponse userResponse =  userService.update(id, request);
        return ApiResponse.<UserResponse>builder()
                .result(userResponse)
                .build();
    }
    @PatchMapping("/me")
    public ApiResponse<UserResponse> updateMe(@RequestBody UpdateUserRequest request, Authentication authentication){
        String email = authentication.name();
        UserResponse user = userService.updateMe(email,request);

        return ApiResponse.<UserResponse>builder()
                .code(0)
                .message("success")
                .result(user)
                .build();
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
