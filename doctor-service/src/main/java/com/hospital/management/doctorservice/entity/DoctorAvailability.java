package com.hospital.management.doctorservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents a doctor's declared availability for a single day.
 *
 * breakStart is nullable — null means the doctor chose not to
 * schedule a long break for this particular shift.
 */
@Entity
@Data
@Table(name = "doctor_availability")
public class DoctorAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long doctorId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime shiftStart;

    @Column(nullable = false)
    private LocalTime shiftEnd;

    // Doctor's declared break start time — system adds 60 minutes automatically.
    // Nullable: not every shift needs a long break.
    @Column(nullable = true)
    private LocalTime breakStart;

    private boolean isAvailable = true;

    // Explicitly marks this record as locked once saved.
    // Prevents future updates to this availability.
    @Column(nullable = false)
    private boolean isLocked = false;
}