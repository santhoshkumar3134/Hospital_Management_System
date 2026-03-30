package com.hospital.management.doctorservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MedicalHistoryDTO {

    private Long recordId;
    private LocalDate visitDate;
    private String diagnosis;      // e.g., "Common Cold"
    private String treatment;      // e.g., "Rest and Fluids"
    private String prescribedMeds; // e.g., "Paracetamol 500mg"
    private String doctorNotes;    // Any special notes from the previous doctor
}