package com.hospital.management.patientservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientUpdateDTO {
    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @NotBlank
    @Pattern(regexp = "^\\d{10}$", message = "Contact details must be 10 digits")
    private String contactDetails;

    // Restricted: Patients cannot self-update history. Forwarding data to
    // Medical History Services module for administrative update.

    private String updatedMedicalHistory;
}