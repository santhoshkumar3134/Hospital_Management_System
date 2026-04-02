package com.hospital.management.patientservice.controller;

import com.hospital.management.patientservice.dto.PatientRegistrationDTO;
import com.hospital.management.patientservice.dto.PatientUpdateDTO;
import com.hospital.management.patientservice.model.PatientProfile;
import com.hospital.management.patientservice.service.PatientProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientProfileController {

    private final PatientProfileService patientProfileService;

    @PostMapping("/create")
    public ResponseEntity<PatientProfile> registerPatient(@Valid @RequestBody PatientRegistrationDTO dto) {
        // Feature: Registers patients with personal information [cite: 7, 31]
        return new ResponseEntity<>(patientProfileService.registerPatient(dto), HttpStatus.CREATED);
    }

    @GetMapping("/get/{patientId}")
    public ResponseEntity<PatientProfile> getPatientProfileById(@PathVariable long patientId) {
        return ResponseEntity.ok(patientProfileService.getPatientProfileById(patientId));
    }

    @PutMapping("/update/{patientId}")
    public ResponseEntity<PatientProfile> updatePatientProfile(
            @PathVariable Long patientId,
            @Valid @RequestBody PatientUpdateDTO dto) {

        return ResponseEntity.ok(patientProfileService.updatePatientProfileById(patientId, dto));
    }
}