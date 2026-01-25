package com.example.user_demo.config;

import com.example.user_demo.dto.response.ApiResponse;
import com.example.user_demo.enums.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
public class RestAccessDeniedHandle implements AccessDeniedHandler {
    //403
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, org.springframework.security.access.AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setStatus(ErrorCode.FORBIDDEN.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiResponse<Void> body = ApiResponse.<Void>builder()
                .code(ErrorCode.FORBIDDEN.getCode())
                .message(ErrorCode.FORBIDDEN.getMessage())
                .build();

        objectMapper.writeValue(response.getWriter(),body);
    }
}
