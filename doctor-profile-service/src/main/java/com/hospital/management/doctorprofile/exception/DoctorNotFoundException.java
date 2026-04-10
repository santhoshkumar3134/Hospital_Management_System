package com.hospital.management.doctorprofile.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends DoctorException {

    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " not found with id: " + id, HttpStatus.NOT_FOUND);
    }
}