package com.hospital.management.doctorservice.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;

/**
 * Base custom exception for all Doctor Schedule business rule violations.
 * Carries an HttpStatus so the GlobalExceptionHandler can respond
 * with the correct HTTP status code automatically.
 */
@Getter
public class ScheduleException extends RuntimeException {

    private final HttpStatus status;

    public ScheduleException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}