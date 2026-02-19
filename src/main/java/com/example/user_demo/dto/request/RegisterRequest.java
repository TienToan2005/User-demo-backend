package com.example.user_demo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record RegisterRequest(
        @Email @NotBlank String email,
        @NotBlank  String fullName,
        @NotBlank @Size(min = 6) String password
){};