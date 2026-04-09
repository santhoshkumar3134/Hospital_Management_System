package com.hospital.management.doctorservice.client;

import com.hospital.management.doctorservice.dto.MedicalHistoryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

/**
 * Feign client to communicate with the Medical History Service.
 * Used to fetch a patient's visit history when a doctor
 * clicks a booked slot to view patient details.
 * Resolves via Eureka — no hardcoded URL.
 */
@FeignClient(name = "medical-history-service")
public interface MedicalHistoryClient {

    // Fetches full history for a patient — service filters to top 3 most recent
    @GetMapping("/medical-history-service/get/{patientId}")
    List<MedicalHistoryDTO> getPatientHistory(@PathVariable("patientId") Long patientId);
}