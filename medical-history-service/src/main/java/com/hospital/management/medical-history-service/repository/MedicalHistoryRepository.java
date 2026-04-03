package com.hospital.management.medical_history_service.Repository;

import com.hospital.management.medical_history_service.Entity.MedicalHistory;
import com.hospital.management.medical_history_service.MedicalHistoryServiceApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalHistoryRepository extends JpaRepository<MedicalHistory,Long> {
    Optional<List<MedicalHistory>> findByPatientId(Long id);
}
