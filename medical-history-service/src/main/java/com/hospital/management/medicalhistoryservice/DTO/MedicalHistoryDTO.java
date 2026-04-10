package com.hospital.management.medicalhistoryservice.DTO;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MedicalHistoryDTO {
    private Long recordId;
    @NotNull
    private String diagnosis;
    private List<String> prescribedMeds;
    @PastOrPresent(message = "Diagnosed date should be at past or present")
    private LocalDate visitDate;
    @NotNull(message = "Patient id should not be blank")
    private Long patientId;

}
