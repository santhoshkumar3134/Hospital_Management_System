package com.hospital.appointmentservice.client;

import com.hospital.appointmentservice.dto.DoctorAvailabilityDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;


///api/v1/doctors/specialization/{specialization} -> List of doctors with that specialization and their available slots
@FeignClient(name = "doctor-profile-service")
public interface DoctorServiceClient {

    // Get available doctors by specialization (returns only doctorId, doctorName, specialization)
    @GetMapping("/api/v1/doctor-schedule/doctors/specialization/{specialization}")
    List<DoctorAvailabilityDTO> getAvailableDoctorsBySpecialization(
            @PathVariable String specialization
    );

    // Get available time slots for a doctor on a specific date
}