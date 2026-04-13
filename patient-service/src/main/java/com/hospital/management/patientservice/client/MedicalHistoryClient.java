package com.hospital.management.patientservice.client;


import com.hospital.management.patientservice.dto.MedicalHistoryRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "medical-history-service", url = "http://localhost:8081/api/medical-history")
public interface MedicalHistoryClient {

    // called on patient registration
    @PostMapping("/api/v1/histories")
    void createInitialHistory(@RequestBody MedicalHistoryRequestDTO requestDTO);

    // called on patient update
    @PutMapping("/api/v1/histories")
    void updateHistory(@RequestBody MedicalHistoryRequestDTO requestDTO);
}