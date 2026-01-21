package com.example.user_demo.dto.response;

import lombok.Builder;

@Builder
public record ApiResponse <T> (
     int code,
     String message,
     T result
){};
