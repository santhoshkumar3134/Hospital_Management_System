package com.hospital.management.doctorprofile.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO returned to callers — safe subset of Doctor entity.
 * Never exposes internal DB fields directly.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorProfileResponseDTO {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String specialization;
    private String designation;
}