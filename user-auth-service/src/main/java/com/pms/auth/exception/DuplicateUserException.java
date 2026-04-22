package com.pms.auth.exception;
import org.springframework.http.HttpStatus;

public class DuplicateUserException extends PmsBaseException {
    public DuplicateUserException(String field) {
        // field = "username" or "email"
        super(field + " already exists", "DUPLICATE_USER", HttpStatus.CONFLICT); // 409
    }
}
