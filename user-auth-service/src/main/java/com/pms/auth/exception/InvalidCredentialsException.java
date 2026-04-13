package com.pms.auth.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends PmsBaseException {
    public InvalidCredentialsException() {
        // 401 Unauthorized — wrong username or password
        super("Invalid username or password", "INVALID_CREDENTIALS", HttpStatus.UNAUTHORIZED);
    }
}