package com.hospital.management.doctorprofile.service;

import com.hospital.management.doctorprofile.dto.DoctorAppointmentResponseDTO;
import com.hospital.management.doctorprofile.dto.DoctorProfileResponseDTO;
import com.hospital.management.doctorprofile.dto.DoctorRegistrationDTO;

import java.util.List;

public interface DoctorProfileService {
    public DoctorProfileResponseDTO registerDoctor(DoctorRegistrationDTO requestDTO);
    public DoctorProfileResponseDTO getDoctorById(Long id);
    public List<DoctorProfileResponseDTO> getAllDoctors();
    public List<DoctorAppointmentResponseDTO> getDoctorsBySpecialization(String specialization);
}
