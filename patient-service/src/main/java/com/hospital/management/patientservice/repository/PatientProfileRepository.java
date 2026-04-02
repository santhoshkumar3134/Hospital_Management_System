package com.hospital.management.patientservice.repository;

import com.hospital.management.patientservice.model.PatientProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientProfileRepository extends JpaRepository<PatientProfile,Long> {

}
