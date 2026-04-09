package com.hospital.appointmentservice.client;

import com.hospital.appointmentservice.dto.TimeSlotDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = "DOCTOR-SERVICE")
public interface DoctorSheduleClient {
    @GetMapping("/api/v1/doctor-schedule/slots/doctor/{doctorId}/{date}")
    List<TimeSlotDTO> getTimeSlotsByDoctorId(
            @PathVariable("doctorId") Long doctorId,
            // ADD @DateTimeFormat HERE
            @PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    );

    // Claim a specific time slot
    @PutMapping("/api/v1/doctor-schedule/slots/claim")
    void claimTimeSlot(
            @RequestParam Long doctorId,
            @RequestParam Long patientId,
            @RequestParam("startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime
    );

    // Cancel a booking and release the time slot
    @PutMapping("/api/v1/doctor-schedule/cancel-booking")
    void cancelBooking(
            @RequestParam Long doctorId,
            @RequestParam Long patientId,
            @RequestParam("startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime
    );
}
