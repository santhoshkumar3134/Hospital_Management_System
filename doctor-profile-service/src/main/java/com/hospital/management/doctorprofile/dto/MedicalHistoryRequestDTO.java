package com.hospital.management.doctorprofile.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MedicalHistoryRequestDTO {

    private Long patientId;
    private String initialMedicalHistory;
}