package com.example.user_demo.dto.response;


public record TokenResponse(
        String accessToken,
        String refreshToken,
        Long expiresIn
) { };
