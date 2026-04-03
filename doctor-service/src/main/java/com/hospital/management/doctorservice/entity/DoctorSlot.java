package com.hospital.management.doctorservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Represents a single 30-minute bookable time slot
 * generated from a DoctorAvailability record.
 * patientId is null until the slot is booked by the Appointment service.
 */
@Entity
@Data
@Table(name = "doctor_slot")
public class DoctorSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long slotId;

    // References the doctor in the Doctor microservice — no JPA relationship
    @Column(nullable = false)
    private Long doctorId;

    @Column(nullable = false)
    private LocalDate slotDate;

    @Column(nullable = false)
    private LocalTime startTime;

    // Default false — becomes true when Appointment service confirms a booking
    @Column(nullable = false)
    private boolean isBooked = false;

    // Null until booked. References the patient in the Patient microservice.
    // No JPA relationship — only the ID is stored (decoupled microservice design).
    private Long patientId;

    // NEW — Appointment Service expects createdAt in the TimeSlotDTO response.
    // @CreationTimestamp sets this automatically when the slot is first saved.
    // updatable=false ensures it never changes after creation.
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}