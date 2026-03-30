package com.hospital.management.medical_history_service.Entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Data
public class MedicalHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long medicalHistoryId;
    private String disease;
    private String treatment;
    @PastOrPresent(message = "Diagnosed date should be at past or present")
    private LocalDateTime diagnosedAt;
    @NotNull(message = "Patient id should not be blank")
    private Long patientId;
}
