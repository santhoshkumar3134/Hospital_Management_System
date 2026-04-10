package com.hospital.management.medicalhistoryservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Builder
public class MedicalHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recordId;
    private String diagnosis;
    private LocalDateTime diagnosedAt;
    private Long patientId;
    private List<String> prescribedMeds;
}
