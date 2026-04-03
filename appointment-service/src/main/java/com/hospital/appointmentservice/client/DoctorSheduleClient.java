package com.hospital.appointmentservice.client;

import com.hospital.appointmentservice.dto.TimeSlotDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = "doctor-sheduling-service", url = "${doctor.service.url}")
public interface DoctorSheduleClient {
    @GetMapping("/api/v1/doctor-schedule/slots/doctor/{doctorId}/{date}")
    List<TimeSlotDTO> getTimeSlotsByDoctorId(
            @PathVariable("doctorId") Long doctorId,
            @PathVariable("date") LocalDate date
    );

    // Claim a specific time slot
    @PutMapping("/api/v1/doctor-schedule/slots/claim")
    void claimTimeSlot(
            @RequestParam Long doctorId,
            @RequestParam Long patientId,
            @RequestParam LocalDateTime startTime
    );

    // Cancel a booking and release the time slot
    @PatchMapping("/api/v1/doctor-schedule/cancel-booking")
    void cancelBooking(
            @RequestParam Long doctorId,
            @RequestParam Long patientId,
            @RequestParam LocalDateTime startTime
    );
}
