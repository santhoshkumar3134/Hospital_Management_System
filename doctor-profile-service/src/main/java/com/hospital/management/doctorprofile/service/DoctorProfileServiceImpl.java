package com.hospital.management.doctorprofile.service;


import com.hospital.management.doctorprofile.dto.DoctorRegistrationDTO;
import com.hospital.management.doctorprofile.dto.DoctorProfileResponseDTO;
import com.hospital.management.doctorprofile.dto.DoctorUpdateDTO;
import com.hospital.management.doctorprofile.entity.Doctor;
import com.hospital.management.doctorprofile.exception.DoctorException;
import com.hospital.management.doctorprofile.exception.ResourceNotFoundException;
import com.hospital.management.doctorprofile.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorProfileServiceImpl implements  DoctorProfileService{

    private final DoctorRepository doctorRepository;
    private final ModelMapper modelMapper;

    /**
     * Registers a new doctor.
     * Checks for duplicate email before saving —
     * email is a unique identifier across the system.
     */
    @Transactional
    public DoctorProfileResponseDTO registerDoctor(DoctorRegistrationDTO requestDTO) {
        log.info("Registering new doctor with email={}", requestDTO.getEmail());

        // Duplicate email guard — each doctor must have a unique email
        if (doctorRepository.existsByEmail(requestDTO.getEmail())) {
            throw new DoctorException(
                    "Doctor with email " + requestDTO.getEmail() + " already exists.",
                    HttpStatus.CONFLICT);
        }

        // Map DTO → Entity and save
        Doctor doctor = modelMapper.map(requestDTO, Doctor.class);

        Doctor saved = doctorRepository.save(doctor);
        log.info("Doctor registered successfully with id={}", saved.getId());

        return modelMapper.map(saved, DoctorProfileResponseDTO.class);
    }

    /**
     * Fetches a single doctor by their ID.
     * Used by Appointment Service and Doctor Schedule Service
     * to get doctor details using the doctorId they store.
     */
    public DoctorProfileResponseDTO getDoctorById(Long id) {
        log.info("Fetching doctor with id={}", id);

        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", id));

        return modelMapper.map(doctor, DoctorProfileResponseDTO.class);
    }

    /**
     * Returns all registered doctors.
     * Used by admin panel or listing pages.
     */
    public List<DoctorProfileResponseDTO> getAllDoctors() {
        log.info("Fetching all doctors");

        return doctorRepository.findAll()
                .stream()
                .map(doctor -> modelMapper.map(doctor, DoctorProfileResponseDTO.class))
                .toList();
    }

    /**
     * Searches doctors by specialization.
     * Used by Appointment Service when patient searches
     * "Show me all Cardiologists".
     * Case-insensitive — "cardiologist" and "Cardiologist" both work.
     */
    public List<DoctorProfileResponseDTO> getDoctorsBySpecialization(String specialization) {
        log.info("Searching doctors by specialization={}", specialization);

        List<Doctor> doctors = doctorRepository
                .findBySpecializationIgnoreCase(specialization);

        if (doctors.isEmpty()) {
            throw new DoctorException(
                    "No doctors found with specialization: " + specialization,
                    HttpStatus.NOT_FOUND);
        }

        return doctors.stream()
                .map(doctor -> modelMapper.map(doctor, DoctorProfileResponseDTO.class))
                .toList();
    }

    /**
     * Updates an existing doctor's profile.
     * Only updates fields that are provided — null fields are ignored.
     * Email cannot be updated — it is an identifier.
     */
    @Transactional
    public DoctorProfileResponseDTO updateDoctor(Long id, DoctorUpdateDTO updateDTO) {
        log.info("Updating doctor with id={}", id);

        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", id));

        // Only update fields that are explicitly provided
        // Null check prevents overwriting existing data with null
        if (updateDTO.getName() != null) {
            doctor.setName(updateDTO.getName());
        }
        if (updateDTO.getPhone() != null) {
            doctor.setPhone(updateDTO.getPhone());
        }
        if (updateDTO.getSpecialization() != null) {
            doctor.setSpecialization(updateDTO.getSpecialization());
        }
        if (updateDTO.getDesignation() != null) {
            doctor.setDesignation(updateDTO.getDesignation());
        }

        Doctor updated = doctorRepository.save(doctor);
        log.info("Doctor id={} updated successfully", id);

        return modelMapper.map(updated, DoctorProfileResponseDTO.class);
    }
}
