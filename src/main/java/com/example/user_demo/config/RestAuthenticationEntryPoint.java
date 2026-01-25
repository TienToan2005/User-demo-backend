package com.example.user_demo.config;

import com.example.user_demo.dto.response.ApiResponse;
import com.example.user_demo.enums.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    //401
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        response.setStatus(ErrorCode.UNAUTHENTICATED.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiResponse<Void> body = ApiResponse.<Void>builder()
                .code(ErrorCode.UNAUTHENTICATED.getCode())
                .message(ErrorCode.UNAUTHENTICATED.getMessage())
                .build();

        objectMapper.writeValue(response.getWriter(), body);
    }
}
