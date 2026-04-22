package com.pms.auth.exception;
import org.springframework.http.HttpStatus;

public class AccessDeniedException extends PmsBaseException {
    public AccessDeniedException() {
        super("You are not allowed to access this resource", "ACCESS_DENIED", HttpStatus.FORBIDDEN);
    }
}