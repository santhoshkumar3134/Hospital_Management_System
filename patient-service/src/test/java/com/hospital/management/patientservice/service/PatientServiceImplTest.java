package com.hospital.management.patientservice.service;

import com.hospital.management.patientservice.client.MedicalHistoryClient;
import com.hospital.management.patientservice.dto.MedicalHistoryRequestDTO;
import com.hospital.management.patientservice.dto.PatientRegistrationDTO;
import com.hospital.management.patientservice.dto.PatientResponseDTO;
import com.hospital.management.patientservice.dto.PatientUpdateDTO;
import com.hospital.management.patientservice.model.Patient;
import com.hospital.management.patientservice.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private MedicalHistoryClient medicalHistoryClient;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private PatientServiceImpl patientService;

    private PatientRegistrationDTO registrationDTO;
    private PatientUpdateDTO updateDTO;
    private Patient savedPatient;

    @BeforeEach
    void setUp() {
        registrationDTO = PatientRegistrationDTO.builder()
                .name("Santhoshkumar")
                .dateOfBirth(LocalDate.of(2000, 3, 31))
                .contactDetails("9876543210")
                .initialMedicalHistory("fever")
                .build();

        updateDTO = new PatientUpdateDTO();
        updateDTO.setName("Kumar Updated");
        updateDTO.setContactDetails("9898989892");
        updateDTO.setUpdatedMedicalHistory("Recovered");

        savedPatient = Patient.builder()
                .patientId(1L)
                .name("Santhoshkumar")
                .dateOfBirth(LocalDate.of(2000, 3, 31))
                .contactDetails("9876543210")
                .build();
    }

    // ─────────────────────────────────────────────
    // REGISTER PATIENT
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("registerPatient — Should save patient and call MedicalHistoryClient")
    void registerPatient_Success() {
        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);

        PatientResponseDTO result = patientService.registerPatient(registrationDTO);

        assertNotNull(result);
        assertEquals(1L, result.getPatientId());
        assertEquals("Santhoshkumar", result.getName());

        // Verify Repository Call
        verify(patientRepository, times(1)).save(any(Patient.class));

        // Verify Client Call
        verify(medicalHistoryClient, times(1))
                .createInitialHistory(any(MedicalHistoryRequestDTO.class));
    }

    @Test
    @DisplayName("registerPatient — Should still return DTO even if MedicalHistoryClient fails")
    void registerPatient_ClientFailure_ShouldNotThrowException() {
        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);

        // Simulate external service down
        doThrow(new RuntimeException("Service Down"))
                .when(medicalHistoryClient).createInitialHistory(any());

        PatientResponseDTO result = patientService.registerPatient(registrationDTO);

        assertNotNull(result);
        verify(patientRepository, times(1)).save(any(Patient.class));
        // Ensure the catch block worked
        verify(medicalHistoryClient, times(1)).createInitialHistory(any());
    }

    // ─────────────────────────────────────────────
    // GET PATIENT
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("getPatientById — Should return DTO when found")
    void getPatientById_Found() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(savedPatient));

        PatientResponseDTO result = patientService.getPatientById(1L);

        assertEquals(1L, result.getPatientId());
        verify(patientRepository, times(1)).findById(1L);
    }

    // ─────────────────────────────────────────────
    // UPDATE PATIENT
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("updatePatientById — Should update fields and send history update")
    void updatePatientById_Success() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(savedPatient));
        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);

        PatientResponseDTO result = patientService.updatePatientById(1L, updateDTO);

        assertNotNull(result);
        verify(patientRepository).save(any(Patient.class));
        // Verify the history update was triggered
        verify(medicalHistoryClient).updateHistory(any(MedicalHistoryRequestDTO.class));
    }

    @Test
    @DisplayName("updatePatientById — Should NOT call history client if history field is null")
    void updatePatientById_NoHistoryUpdate() {
        updateDTO.setUpdatedMedicalHistory(null);
        when(patientRepository.findById(1L)).thenReturn(Optional.of(savedPatient));
        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);

        patientService.updatePatientById(1L, updateDTO);

        // Verify history client was NEVER called
        verify(medicalHistoryClient, never()).updateHistory(any());
    }
}