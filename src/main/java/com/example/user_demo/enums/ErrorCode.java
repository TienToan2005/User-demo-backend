package com.example.user_demo.enums;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    VALIDATION_ERROR(1000,HttpStatus.BAD_REQUEST,"Validation failed"), //400
    USER_NOT_FOUND(1001,HttpStatus.NOT_FOUND,"User not found"), //404
    EMAIL_EXISTED(1002,HttpStatus.CONFLICT,"Email already exists"), //409
    BAD_REQUEST(1004,HttpStatus.BAD_REQUEST,"Bad request"),//400
    INTERNAL_ERROR(1005,HttpStatus.INTERNAL_SERVER_ERROR,"Internal server error"), //500
    STATUS_FORBIDDEN(1006,HttpStatus.FORBIDDEN,"User is inactive"), //403
    INVALID_CREDENTIALS(1007,HttpStatus.BAD_REQUEST,"Invalid credentials"),
    UNAUTHTHENTICATED(1008,HttpStatus.UNAUTHORIZED,"")
    ;

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(int code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getMessage() {
        return message;
    }
}
