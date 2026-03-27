package com.hospital.management.patientservice.service;


import com.hospital.management.patientservice.dto.PatientRegistrationDTO;
import com.hospital.management.patientservice.dto.PatientUpdateDTO;
import com.hospital.management.patientservice.exception.PatientNotFoundException;
import com.hospital.management.patientservice.model.PatientProfile;
import com.hospital.management.patientservice.repository.PatientProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientProfileServiceImpl implements PatientProfileService {

    private final PatientProfileRepository patientRepository;

    @Override
    @Transactional
    public PatientProfile registerPatient(PatientRegistrationDTO dto) {
        log.info("Creating profile for: {}", dto.getName());

        // 1. Map DTO to Entity and Save to YOUR database [cite: 35]
        PatientProfile patient = PatientProfile.builder()
                .name(dto.getName())
                .dateOfBirth(dto.getDateOfBirth())
                .contactDetails(dto.getContactDetails())
                .build();

        PatientProfile savedPatient = patientRepository.save(patient);

        // 2. TODO: Send dto.getInitialMedicalHistory() to the Medical History Service
        // Example: medicalHistoryClient.saveInitialRecord(savedPatient.getPatientId(), dto.getInitialMedicalHistory());

        return savedPatient;
    }

    @Override
    @Transactional(readOnly = true)
    public PatientProfile getPatientProfileById(long patientId) {
        log.debug("Fetching profile for patient ID: {}", patientId);
        return patientRepository.findById(patientId)
                .orElseThrow(() -> {
                    log.error("Patient search failed: ID {} not found", patientId);
                    return new PatientNotFoundException("Patient not found with ID: " + patientId);
                });
    }

    @Override
    @Transactional
    public PatientProfile updatePatientProfileById(long patientId, PatientUpdateDTO patientDetails) {
        log.info("Updating profile for patient ID: {}", patientId);

        PatientProfile existingPatient = getPatientProfileById(patientId);

        existingPatient.setName(patientDetails.getName());
        existingPatient.setContactDetails(patientDetails.getContactDetails());
        existingPatient.setDateOfBirth(patientDetails.getDateOfBirth());

        PatientProfile updated = patientRepository.save(existingPatient);
        log.info("Successfully updated profile for patient: {}", updated.getName());
        return updated;
    }
}