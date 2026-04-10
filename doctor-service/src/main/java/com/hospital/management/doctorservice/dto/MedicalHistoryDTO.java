package com.hospital.management.doctorservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MedicalHistoryDTO {
    private String diagnosis;
    private List<String> prescribedMeds;
    private LocalDate visitDate;
    private Long patientId;
}