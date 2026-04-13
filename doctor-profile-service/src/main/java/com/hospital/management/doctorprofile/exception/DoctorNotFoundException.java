package com.hospital.management.doctorprofile.exception;

import org.springframework.http.HttpStatus;

/**
 * Base exception for doctor-related errors
 */
public class DoctorNotFoundException extends RuntimeException {
    private HttpStatus status;

    public DoctorNotFoundException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
