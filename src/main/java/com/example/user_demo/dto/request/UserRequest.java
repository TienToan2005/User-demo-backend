package com.example.user_demo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Builder
public record UserRequest (
        @NotBlank String fullName,
        @NotBlank @Email String email
){ };
