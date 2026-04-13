package com.hospital.management.medicalhistoryservice.service;

import com.hospital.management.medicalhistoryservice.entity.MedicalHistory;
import com.hospital.management.medicalhistoryservice.exception.PatientNotFoundException;
import com.hospital.management.medicalhistoryservice.exception.RecordCreationException;
import com.hospital.management.medicalhistoryservice.repository.MedicalHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MedicalHistoryService {
    @Autowired
    private MedicalHistoryRepository medicalHistoryRepository;

    public MedicalHistory saveMedicalHistory(MedicalHistory medicalHistory){
        try{
            return medicalHistoryRepository.save(medicalHistory);
        }catch (Exception e){
            throw new RecordCreationException("Unable to create medical record");
        }
    }

    public MedicalHistory getMedicalHistory(Long patientId){
        return medicalHistoryRepository.findById(patientId).orElseThrow(()-> new PatientNotFoundException("Patient not found for ID:"+patientId));

    }

    public List<MedicalHistory> getAllMedicalHistory(){
        return medicalHistoryRepository.findAll();
    }
}
