package com.hospital.management.doctorservice.client;

import com.hospital.management.doctorservice.dto.MedicalHistoryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@FeignClient(name = "medical-history-service")
public interface MedicalHistoryClient {

    // Using List<Object> is better here so our Service can filter it
    @GetMapping("/api/v1/history/patient/{patientId}")
    List<MedicalHistoryDTO> getPatientHistory(@PathVariable("patientId") Long patientId);
}