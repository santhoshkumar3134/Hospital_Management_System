package com.hospital.management.doctorprofile.controller;


import com.hospital.management.doctorprofile.dto.DoctorAppointmentResponseDTO;
import com.hospital.management.doctorprofile.dto.DoctorRegistrationDTO;
import com.hospital.management.doctorprofile.dto.DoctorProfileResponseDTO;
import com.hospital.management.doctorprofile.dto.DoctorUpdateDTO;
import com.hospital.management.doctorprofile.service.DoctorProfileServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
public class DoctorProfileController {

    private final DoctorProfileServiceImpl doctorProfileServiceImpl;

    // POST /api/v1/doctors
    // Called by hospital admin to register a new doctor
    @PostMapping
    public ResponseEntity<DoctorProfileResponseDTO> registerDoctor(
            @Valid @RequestBody DoctorRegistrationDTO requestDTO) {
        log.info("Register doctor request received");
        DoctorProfileResponseDTO response = doctorProfileServiceImpl.registerDoctor(requestDTO);
        // 201 Created — resource was successfully created
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/v1/doctors/{id}
    // Called by Appointment Service to get doctor details by ID
    @GetMapping("/{id}")
    public ResponseEntity<DoctorProfileResponseDTO> getDoctorById(@PathVariable Long id) {
        log.info("Get doctor by id={}", id);
        return ResponseEntity.ok(doctorProfileServiceImpl.getDoctorById(id));
    }

    // GET /api/v1/doctors
    // Returns all registered doctors
    @GetMapping
    public ResponseEntity<List<DoctorProfileResponseDTO>> getAllDoctors() {
        log.info("Get all doctors request received");
        return ResponseEntity.ok(doctorProfileServiceImpl.getAllDoctors());
    }

    // GET /api/v1/doctors/search?specialization=Cardiologist
    // Called by Appointment Service when patient searches by specialization
    // doctor id , doctor name, specialization
    @GetMapping("/specialization/{specialization}")
    public ResponseEntity<List<DoctorAppointmentResponseDTO>> getDoctorsBySpecialization(
            @PathVariable String specialization) {
        log.info("Search doctors by specialization={}", specialization);
        return ResponseEntity.ok(doctorProfileServiceImpl.getDoctorsBySpecialization(specialization));
    }

    // PUT /api/v1/doctors/{id}
    // Updates an existing doctor's profile — email cannot be changed
    @PutMapping("/{id}")
    public ResponseEntity<DoctorProfileResponseDTO> updateDoctor(
            @PathVariable Long id,
            @Valid @RequestBody DoctorUpdateDTO updateDTO) {
        log.info("Update doctor request for id={}", id);
        return ResponseEntity.ok(doctorProfileServiceImpl.updateDoctor(id, updateDTO));
    }


}
