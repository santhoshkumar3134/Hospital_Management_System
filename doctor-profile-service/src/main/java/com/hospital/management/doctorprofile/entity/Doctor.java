package com.hospital.management.doctorprofile.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a doctor's profile in the hospital system.
 * This service owns doctor identity and profile data only.
 * Scheduling is handled by the Doctor Schedule Service (Module 2.3).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "doctors")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Full name of the doctor
    @Column(nullable = false)
    private String name;

    // Unique email — used for identification across services
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String phone;

    // Medical specialization — e.g. Cardiologist, Neurologist
    // Appointment Service searches doctors by this field
    @Column(nullable = false)
    private String specialization;

    // Professional title — e.g. Consultant, Senior Consultant, HOD
    @Column(nullable = false)
    private String designation;
}
