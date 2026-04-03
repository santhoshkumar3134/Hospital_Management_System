package com.hospital.appointmentservice.repository;

import com.hospital.appointmentservice.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Checks if a doctor is already busy at a specific time
    boolean existsByDoctorIdAndAppointmentDate(Long doctorId, LocalDateTime appointmentDate);

    // Checks if patient already has an appointment with a specific doctor
    boolean existsByPatientIdAndDoctorId(Long patientId, Long doctorId);

    List<Appointment> findByDoctorId(Long doctorId);

    List<Appointment> findByPatientId(Long patientId);

    Optional<Appointment> findByAppointmentId(Long appointmentId);
}