package com.hospital.management.medicalhistoryservice.repository;


import com.hospital.management.medicalhistoryservice.entity.MedicalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalHistoryRepository extends JpaRepository<MedicalHistory,Long> {
}
