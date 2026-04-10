package com.hospital.management.patientservice.service;


import com.hospital.management.patientservice.dto.PatientRegistrationDTO;
import com.hospital.management.patientservice.dto.PatientResponseDTO;
import com.hospital.management.patientservice.dto.PatientUpdateDTO;

public interface PatientService {

    PatientResponseDTO registerPatient(PatientRegistrationDTO dto);

    PatientResponseDTO getPatientById(long patientId);         // returns DTO not entity

    PatientResponseDTO updatePatientById(long patientId, PatientUpdateDTO dto); // returns DTO not entity
}