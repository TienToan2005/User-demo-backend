package com.example.user_demo.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
public record ApiResponse <T> (
     int code,
     String message,
     T result
){};
