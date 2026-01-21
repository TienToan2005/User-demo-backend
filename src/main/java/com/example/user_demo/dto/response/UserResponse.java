package com.example.user_demo.dto.response;

import com.example.user_demo.enums.UserStatus;
import lombok.Builder;

@Builder
public record UserResponse(
        Long id,
        String email,
        String fullName,
        UserStatus status
) { };
