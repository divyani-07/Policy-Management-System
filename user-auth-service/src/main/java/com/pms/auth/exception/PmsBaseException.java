package com.pms.auth.exception;

import org.springframework.http.HttpStatus;

// All our exceptions extend this — one base class to rule them all
public class PmsBaseException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;

    public PmsBaseException(String message, String errorCode, HttpStatus httpStatus) {
        super(message);              // Pass message to RuntimeException
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() { return errorCode; }
    public HttpStatus getHttpStatus() { return httpStatus; }
}