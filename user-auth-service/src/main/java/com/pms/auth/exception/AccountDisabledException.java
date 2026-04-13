package com.pms.auth.exception;

import org.springframework.http.HttpStatus;

public class AccountDisabledException extends PmsBaseException {
    public AccountDisabledException() {
        // 403 Forbidden — account exists but is deactivated
        super("Account is deactivated", "ACCOUNT_DISABLED", HttpStatus.FORBIDDEN);
    }
}