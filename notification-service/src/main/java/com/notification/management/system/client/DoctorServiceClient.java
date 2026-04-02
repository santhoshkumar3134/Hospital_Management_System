package com.notification.management.system.client;


import com.notification.management.system.dto.DoctorDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "doctor-service")
public interface DoctorServiceClient {

    @GetMapping("/api/v1/doctors/{doctorId}")
    DoctorDTO getDoctorById(@PathVariable Long doctorId);
}