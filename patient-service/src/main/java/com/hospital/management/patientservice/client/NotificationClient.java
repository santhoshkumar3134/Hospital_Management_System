package com.hospital.management.patientservice.client;

import com.hospital.management.patientservice.dto.MedicalHistoryRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", url = "http://localhost:8083/api/v1/notifications")
public interface NotificationClient {

    @GetMapping("/api/v1/histories")
    void createInitialHistory(@RequestBody MedicalHistoryRequestDTO requestDTO);
}
