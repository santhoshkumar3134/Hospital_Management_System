package com.hospital.management.medicalhistoryservice.controller;

import com.hospital.management.medicalhistoryservice.DTO.MedicalHistoryDTO;
import com.hospital.management.medicalhistoryservice.entity.MedicalHistory;
import com.hospital.management.medicalhistoryservice.service.MedicalHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/medical-history-service")
public class MedicalHistoryController {
    @Autowired
    private MedicalHistoryService medicalHistoryService;

    @GetMapping("getAll")
    public ResponseEntity<List<MedicalHistory>> getAllMedicalRecords() {
        return ResponseEntity.ok().body(
                medicalHistoryService.getAllMedicalHistory());
    }

    @GetMapping("get/{patientId}")
    public ResponseEntity<List<MedicalHistoryDTO>> getMedicalRecord(@PathVariable("patientId") Long id) {
        //to be coded soon.
    }

    @PostMapping("/save")
    public ResponseEntity<String> saveMedicalRecord(@RequestBody MedicalHistoryDTO medicalHistoryDTO) {
        MedicalHistory medicalHistory = MedicalHistory.builder()
                .recordId(medicalHistoryDTO.getRecordId())
                .patientId(medicalHistoryDTO.getPatientId())
                .diagnosis(medicalHistoryDTO.getDiagnosis())
                .diagnosedAt(medicalHistoryDTO.getDiagnosedAt())
                .prescribedMeds(medicalHistoryDTO.getPrescribedMeds())
                .build();
        return ResponseEntity.ok().body("Successfully created the medical record");
    }
}
