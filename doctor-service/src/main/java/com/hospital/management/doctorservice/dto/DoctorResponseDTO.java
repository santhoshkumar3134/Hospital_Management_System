package com.hospital.management.doctorservice.dto;

import lombok.Data;

/**
 * DTO representing the doctor profile response received
 * from the Doctor Service via Feign.
 * Must match the DoctorResponseDTO fields in Doctor Service exactly —
 * any mismatch will cause Feign deserialization to fail silently.
 */
@Data
public class DoctorResponseDTO {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String specialization;
    private String designation;
}