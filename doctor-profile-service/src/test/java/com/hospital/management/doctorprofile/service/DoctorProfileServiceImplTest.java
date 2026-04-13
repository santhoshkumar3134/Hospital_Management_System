package com.hospital.management.doctorprofile.service;

import static org.junit.jupiter.api.Assertions.*;
import com.hospital.management.doctorprofile.dto.DoctorProfileResponseDTO;
import com.hospital.management.doctorprofile.dto.DoctorRegistrationDTO ;
import com.hospital.management.doctorprofile.dto.DoctorUpdateDTO;
import com.hospital.management.doctorprofile.entity.Doctor;
import com.hospital.management.doctorprofile.exception.DoctorException;
import com.hospital.management.doctorprofile.exception.ResourceNotFoundException;
import com.hospital.management.doctorprofile.repository.DoctorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class DoctorProfileServiceImplTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private DoctorProfileServiceImpl doctorProfileService;

    private Doctor doctor;
    private DoctorRegistrationDTO registrationDTO;
    private DoctorUpdateDTO updateDTO;

    @BeforeEach
    void setUp() {
        doctor = Doctor.builder()
                .id(1L)
                .name("Dr. Arun")
                .email("arun@hospital.com")
                .phone("9876543210")
                .specialization("Cardiologist")
                .designation("Consultant")
                .build();

        registrationDTO = new DoctorRegistrationDTO();
        registrationDTO.setName("Dr. Arun");
        registrationDTO.setEmail("arun@hospital.com");
        registrationDTO.setPhone("9876543210");
        registrationDTO.setSpecialization("Cardiologist");
        registrationDTO.setDesignation("Consultant");

        updateDTO = new DoctorUpdateDTO();
        updateDTO.setName("Dr. Arun Updated");
        updateDTO.setPhone("9898989892");
    }

    @Test
    void registerDoctor_success() {
        when(doctorRepository.existsByEmail("arun@hospital.com")).thenReturn(false);
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);

        DoctorProfileResponseDTO result = doctorProfileService.registerDoctor(registrationDTO);

        assertNotNull(result);
        assertEquals("Dr. Arun", result.getName());
    }

    // Most critical edge case — duplicate email must be blocked
    @Test
    void registerDoctor_duplicateEmail_throwsException() {
        when(doctorRepository.existsByEmail("arun@hospital.com")).thenReturn(true);

        assertThrows(DoctorException.class,
                () -> doctorProfileService.registerDoctor(registrationDTO));

        verify(doctorRepository, never()).save(any());
    }

    @Test
    void getDoctorById_notFound_throwsException() {
        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> doctorProfileService.getDoctorById(99L));
    }

    @Test
    void updateDoctor_success() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);

        DoctorProfileResponseDTO result = doctorProfileService.updateDoctor(1L, updateDTO);

        assertNotNull(result);
        verify(doctorRepository, times(1)).save(any(Doctor.class));
    }

    @Test
    void getDoctorsBySpecialization_notFound_throwsException() {
        when(doctorRepository.findBySpecializationIgnoreCase("Unknown"))
                .thenReturn(List.of());

        assertThrows(DoctorException.class,
                () -> doctorProfileService.getDoctorsBySpecialization("Unknown"));
    }
}