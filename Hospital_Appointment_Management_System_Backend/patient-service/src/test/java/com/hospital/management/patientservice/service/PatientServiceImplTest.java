package com.hospital.management.patientservice.service;

import com.hospital.management.patientservice.dto.PatientRegistrationDTO;
import com.hospital.management.patientservice.dto.PatientResponseDTO;
import com.hospital.management.patientservice.dto.PatientUpdateDTO;
import com.hospital.management.patientservice.exception.PatientNotFoundException;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock
    private PatientRepository patientRepository;

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
                .gender("Male")
                .build();

        updateDTO = new PatientUpdateDTO();
        updateDTO.setName("Kumar Updated");
        updateDTO.setContactDetails("9898989892");

        savedPatient = Patient.builder()
                .patientId(1L)
                .name("Santhoshkumar")
                .dateOfBirth(LocalDate.of(2000, 3, 31))
                .contactDetails("9876543210")
                .gender("Male")
                .build();
    }

    //  REGISTER

    @Test
    @DisplayName("registerPatient — Should save patient and return DTO")
    void registerPatient_Success() {
        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);

        PatientResponseDTO result = patientService.registerPatient(registrationDTO);

        assertNotNull(result);
        assertEquals(1L, result.getPatientId());
        assertEquals("Santhoshkumar", result.getName());
        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    //  GET BY ID

    @Test
    @DisplayName("getPatientById — Should return DTO when found")
    void getPatientById_Found() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(savedPatient));

        PatientResponseDTO result = patientService.getPatientById(1L);

        assertEquals(1L, result.getPatientId());
        assertEquals("Santhoshkumar", result.getName());
        verify(patientRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("getPatientById — Should throw PatientNotFoundException when not found")
    void getPatientById_NotFound() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(PatientNotFoundException.class,
                () -> patientService.getPatientById(99L));

        verify(patientRepository, times(1)).findById(99L);
    }

    //  GET ALL

    @Test
    @DisplayName("getAllPatient — Should return list of PatientResponseDTO")
    void getAllPatient_Success() {
        when(patientRepository.findAll()).thenReturn(List.of(savedPatient));

        List<PatientResponseDTO> result = patientService.getAllPatient();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Santhoshkumar", result.get(0).getName());
        verify(patientRepository, times(1)).findAll();
    }

    //  UPDATE

    @Test
    @DisplayName("updatePatientById — Should update fields and return DTO")
    void updatePatientById_Success() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(savedPatient));
        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);

        PatientResponseDTO result = patientService.updatePatientById(1L, updateDTO);

        assertNotNull(result);
        verify(patientRepository, times(1)).findById(1L);
        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    @Test
    @DisplayName("updatePatientById — Should throw PatientNotFoundException when not found")
    void updatePatientById_NotFound() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(PatientNotFoundException.class,
                () -> patientService.updatePatientById(99L, updateDTO));

        verify(patientRepository, never()).save(any(Patient.class));
    }

    //  DELETE

    @Test
    @DisplayName("deletePatientById — Should delete and return success message")
    void deletePatientById_Success() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(savedPatient));
        doNothing().when(patientRepository).deleteById(1L);

        String result = patientService.deletePatientById(1L);

        assertEquals("Successfully Deleted Id 1", result);
        verify(patientRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deletePatientById — Should throw PatientNotFoundException when not found")
    void deletePatientById_NotFound() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(PatientNotFoundException.class,
                () -> patientService.deletePatientById(99L));

        verify(patientRepository, never()).deleteById(any());
    }
}