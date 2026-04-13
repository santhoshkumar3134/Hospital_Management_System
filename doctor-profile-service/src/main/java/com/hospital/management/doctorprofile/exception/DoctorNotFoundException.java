package com.hospital.management.doctorprofile.exception;

import org.springframework.http.HttpStatus;

<<<<<<< HEAD
public class ResourceNotFoundException extends DoctorException {

    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " not found with id: " + id, HttpStatus.NOT_FOUND);
    }
}
=======
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
>>>>>>> patient_service
