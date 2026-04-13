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

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<MedicalHistory> getMedicalRecord(@PathVariable("patientId") Long patientId) {
        return ResponseEntity.ok(medicalHistoryService.getMedicalHistory(patientId));
    }

    @PostMapping("/save")
    public ResponseEntity<String> saveMedicalRecord(@RequestBody MedicalHistoryDTO medicalHistoryDTO) {
        MedicalHistory medicalHistory = MedicalHistory.builder()
                .recordId(medicalHistoryDTO.getRecordId())
                .patientId(medicalHistoryDTO.getPatientId())
                .diagnosis(medicalHistoryDTO.getDiagnosis())
                .prescribedMeds(medicalHistoryDTO.getPrescribedMeds())
                .build();
        return ResponseEntity.ok().body("Successfully created the medical record");
    }
}
