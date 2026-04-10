package com.hospital.management.doctorprofile.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * DTO for updating an existing doctor's profile.
 * All fields are optional — only provided fields will be updated.
 * Email is intentionally excluded — email is an identifier
 * and should not be changed after registration.
 */
@Data
public class DoctorUpdateDTO {

    // Optional — only update if provided
    private String name;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be a 10-digit number")
    private String phone;

    private String specialization;

    private String designation;
}
