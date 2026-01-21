package com.example.user_demo.exception;

import com.example.user_demo.dto.response.ApiResponse;
import com.example.user_demo.enums.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException exception){
       ErrorCode errorCode = exception.getErrorCode();

       return ResponseEntity.status(errorCode.getHttpStatus())
               .body(ApiResponse.<Void>builder()
                       .code(errorCode.getCode())
                       .message(exception.getMessage())
                       .build()
       );
    }
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception){
        return ResponseEntity.status(ErrorCode.VALIDATION_ERROR.getHttpStatus())
                .body(ApiResponse.<Void>builder()
                    .code(ErrorCode.VALIDATION_ERROR.getCode())
                    .message(exception.getBindingResult().getFieldError().getDefaultMessage())
                    .build()
        );
    }
    @ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException exception){
        return ResponseEntity.status(ErrorCode.BAD_REQUEST.getHttpStatus())
                .body(ApiResponse.<Void>builder()
                    .code(ErrorCode.BAD_REQUEST.getCode())
                    .message("Invalid '" + exception.getValue() + "for parameter " + exception.getName() + "'")
                    .build()
        );
    }
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception){
        log.error("Unhandled exception", exception);
        return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.getHttpStatus())
                .body(ApiResponse.<Void>builder()
                    .code(ErrorCode.INTERNAL_ERROR.getCode())
                    .message("Internal server error")
                    .build()
        );
    }
}
