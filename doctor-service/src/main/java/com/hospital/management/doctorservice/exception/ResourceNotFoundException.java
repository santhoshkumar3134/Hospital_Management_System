package com.hospital.management.doctorservice.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a requested resource (e.g. DoctorSlot) does not exist in the DB.
 * Maps to HTTP 404 Not Found.
 */
public class ResourceNotFoundException extends ScheduleException {

    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " not found with id: " + id, HttpStatus.NOT_FOUND);
    }
}