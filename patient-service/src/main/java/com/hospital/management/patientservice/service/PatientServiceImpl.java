package com.hospital.management.patientservice.service;

import com.hospital.management.patientservice.client.MedicalHistoryClient;
import com.hospital.management.patientservice.dto.MedicalHistoryRequestDTO;
import com.hospital.management.patientservice.dto.PatientRegistrationDTO;
import com.hospital.management.patientservice.dto.PatientResponseDTO;
import com.hospital.management.patientservice.dto.PatientUpdateDTO;
import com.hospital.management.patientservice.exception.PatientNotFoundException;
import com.hospital.management.patientservice.model.Patient;
import com.hospital.management.patientservice.repository.PatientRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final ModelMapper modelMapper;
    private final MedicalHistoryClient medicalHistoryClient;

    @Override
    @Transactional
    public PatientResponseDTO registerPatient(PatientRegistrationDTO dto) {
        log.info("Registering patient: {}", dto.getName());

        Patient patient = modelMapper.map(dto, Patient.class);
        Patient saved = patientRepository.save(patient);

        // send initial medical history to medical history service
        try {
            medicalHistoryClient.createInitialHistory(
                    new MedicalHistoryRequestDTO(
                            saved.getPatientId(),
                            dto.getInitialMedicalHistory()
                    )
            );
            log.info("Initial medical history sent for patientId={}",
                    saved.getPatientId());
        } catch (Exception ex) {
            log.warn("Medical history service unavailable for patientId={}: {}",
                    saved.getPatientId(), ex.getMessage());
        }

        return modelMapper.map(saved, PatientResponseDTO.class);
    }

    @Override
    public PatientResponseDTO getPatientById(long patientId) {
        log.debug("Fetching patient ID: {}", patientId);

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> {
                    log.error("Patient not found: ID {}", patientId);
                    return new PatientNotFoundException(
                            "Patient not found with ID: " + patientId);
                });

        return modelMapper.map(patient, PatientResponseDTO.class);
    }

    @Override
    @Transactional
    public PatientResponseDTO updatePatientById(long patientId, PatientUpdateDTO dto) {
        log.info("Updating patient ID: {}", patientId);

        Patient existing = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(
                        "Patient not found with ID: " + patientId));

        modelMapper.map(dto, existing);
        Patient updated = patientRepository.save(existing);

        // send updated medical history to medical history service
        try {
            if (dto.getUpdatedMedicalHistory() != null
                    && !dto.getUpdatedMedicalHistory().isBlank()) {
                medicalHistoryClient.updateHistory(
                        new MedicalHistoryRequestDTO(
                                updated.getPatientId(),
                                dto.getUpdatedMedicalHistory()
                        )
                );
                log.info("Updated medical history sent for patientId={}",
                        updated.getPatientId());
            }
        } catch (Exception ex) {
            log.warn("Medical history service unavailable for patientId={}: {}",
                    updated.getPatientId(), ex.getMessage());
        }

        log.info("Patient updated successfully: {}", updated.getName());
        return modelMapper.map(updated, PatientResponseDTO.class);
    }
}