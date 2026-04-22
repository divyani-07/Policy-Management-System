package com.pms.auth.exception;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends PmsBaseException {
    public UserNotFoundException(Long userId) {
        super("User not found with id: " + userId, "USER_NOT_FOUND", HttpStatus.NOT_FOUND);
    }
}
