package com.pms.auth.exception;
import org.springframework.http.HttpStatus;

public class TokenBlacklistedException extends PmsBaseException {
    public TokenBlacklistedException() {
        super("Token has been invalidated. Please login again.", "TOKEN_BLACKLISTED", HttpStatus.UNAUTHORIZED);
    }
}
