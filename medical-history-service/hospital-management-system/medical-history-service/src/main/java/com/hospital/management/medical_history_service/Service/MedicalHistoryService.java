package com.hospital.management.medical_history_service.Service;

import com.hospital.management.medical_history_service.Entity.MedicalHistory;
import com.hospital.management.medical_history_service.Repository.MedicalHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MedicalHistoryService {
    @Autowired
    private MedicalHistoryRepository medicalHistoryRepository;

    public MedicalHistory saveMedicalHistory(MedicalHistory medicalHistory){
        return medicalHistoryRepository.save(medicalHistory);
    }

    public Optional<List<MedicalHistory>> getMedicalHistory(Long patientId){
        return medicalHistoryRepository.findByPatientId(patientId);
    }
}
