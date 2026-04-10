package com.hospital.management.auth_service.service;

import com.hospital.management.auth_service.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("patient-service")
public interface PatientService {
    @PostMapping("register")
    public ResponseEntity<String> registerPatient(@RequestBody UserDTO userDTO);
}
