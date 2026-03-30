package com.hospital.management.doctorservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
public class DoctorSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long doctorId;
    private LocalDate slotDate;
    private LocalTime startTime;
    private boolean isBooked = false; // Initially false
    private Long patientId; // Add this - links to Patient Module
}
