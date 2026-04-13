package com.pms.auth.exception;

import com.pms.auth.dto.ErrorResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice  // Catches exceptions thrown by ANY controller in this service
public class GlobalExceptionHandler {

    // Handle our custom PMS exceptions (Invalid credentials, Account disabled, etc.)
    @ExceptionHandler(PmsBaseException.class)
    public ResponseEntity<ErrorResponseDTO> handlePmsException(PmsBaseException ex) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                LocalDateTime.now(),
                ex.getErrorCode(),   // "INVALID_CREDENTIALS"
                ex.getMessage()      // "Invalid username or password"
        );
        return ResponseEntity.status(ex.getHttpStatus()).body(error);
        // Returns proper HTTP status (401, 403 etc.) with JSON error body
    }

    // Handle @Valid validation failures (blank username, blank password)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(MethodArgumentNotValidException ex) {

        // Collect all field errors into one string: "username: required, password: required"
        String errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorResponseDTO error = new ErrorResponseDTO(
                LocalDateTime.now(),
                "VALIDATION_ERROR",
                errors
        );
        return ResponseEntity.badRequest().body(error);  // 400 Bad Request
    }
}
