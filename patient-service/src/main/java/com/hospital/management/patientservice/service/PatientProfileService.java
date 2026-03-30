package com.hospital.management.patientservice.service;


import com.hospital.management.patientservice.dto.PatientRegistrationDTO;
import com.hospital.management.patientservice.dto.PatientUpdateDTO;
import com.hospital.management.patientservice.model.PatientProfile;

/**
 * Interface for Patient Profile Management.
 * Defines the contract for registering patients and managing their medical records.
 */
public interface PatientProfileService {

    /**
     * Registers a new patient with personal and medical information. [cite: 7]
     * @param dto The profile data to persist.
     * @return The saved PatientProfile.
     */
    PatientProfile registerPatient(PatientRegistrationDTO dto);

    /**
     * Retrieves a patient's profile and medical history by ID. [cite: 17]
     * @param patientId The unique identifier of the patient. [cite: 39, 91]
     * @return The found PatientProfile.
     */
    PatientProfile getPatientProfileById(long patientId);

    /**
     * Updates an existing patient's personal details and medical history.
     * @param patientId The ID of the patient to update.
     * @param patientDetails The updated information.
     * @return The updated PatientProfile.
     */
    PatientProfile updatePatientProfileById(long patientId, PatientUpdateDTO patientDetails);
}