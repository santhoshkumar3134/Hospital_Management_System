package com.hospital.management.doctorprofile.repository;


import com.hospital.management.doctorprofile.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

//     Used during registration to prevent duplicate email entries
//    Optional<Doctor> findByEmail(String email);

    // Used by Appointment Service to search doctors by specialization
    // e.g. findBySpecialization("Cardiologist")
    List<Doctor> findBySpecializationIgnoreCase(String specialization);

    // Check if email already exists — used before saving a new doctor
    boolean existsByEmail(String email);
}
