package com.hospital.management.doctorservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Centralized exception handler for all controllers.
 * Ensures every error returns a structured JSON body with
 * a proper HTTP status code instead of a plain 200 OK string.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles all custom ScheduleException subclasses
     * (ResourceNotFoundException, ScheduleAlreadyLockedException, etc.)
     */
    @ExceptionHandler(ScheduleException.class)
    public ResponseEntity<Map<String, Object>> handleScheduleException(ScheduleException ex) {
        log.warn("Business rule violation: {}", ex.getMessage());
        return buildResponse(ex.getStatus(), ex.getMessage());
    }

    /**
     * Handles @Valid annotation failures on request DTOs.
     * Returns all field validation errors at once so the client
     * knows exactly what was wrong with their request.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex) {

        // Collect all field errors into a list of readable messages
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();

        log.warn("Validation failed: {}", errors);
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed: " + errors);
    }

    /**
     * Catch-all for any unexpected exceptions.
     * Logs the full stack trace for debugging but returns
     * a generic message to the client (never expose internals).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred: ", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please contact support.");
    }

    /**
     * Builds a consistent error response body structure.
     * Every error from this service will look the same to callers.
     */
    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}