package com.hospital.management.doctorservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
public class DoctorAvailability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long doctorId;      // The link to the Doctor in the Doctor Service
    private LocalDate date;     // The specific day (one of the 3 days)
    private LocalTime shiftStart; // e.g., 10:00 AM
    private LocalTime shiftEnd;   // e.g., 05:00 PM
    private boolean isAvailable;  // Doctor's confirmation toggle
}