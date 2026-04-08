package com.hospital.management.doctorservice.client;

import com.hospital.management.doctorservice.dto.DoctorResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client to communicate with the Doctor Service.
 * Used to verify that a doctorId actually exists before
 * creating availability or slots for that doctor.
 * Resolves via Eureka — no hardcoded URL.
 */
//url=url of the below service "http://localhost/:8082"  -> Optional
@FeignClient(name = "doctor-service")
public interface DoctorClient {

    // Calls GET /api/v1/doctors/{id} on the Doctor Service
    // Returns the doctor's profile if found, throws 404 if not
    @GetMapping("/api/v1/doctors/{id}")
    DoctorResponseDTO getDoctorById(@PathVariable("id") Long id);
}
