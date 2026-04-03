package com.hospital.appointmentservice.controller;

import com.hospital.appointmentservice.model.Appointment;
import com.hospital.appointmentservice.dto.*;
import com.hospital.appointmentservice.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;


    @PostMapping("/available-doctors")
    public ResponseEntity<List<DoctorAvailabilityDTO>> getAvailableDoctors(
            @Valid @RequestBody GetAvailableDoctorsRequest request) {
        List<DoctorAvailabilityDTO> doctors = appointmentService.getAvailableDoctorsBySpecialization(request);
        return ResponseEntity.ok(doctors);
    }


    @PostMapping("/available-slots")
    public ResponseEntity<List<TimeSlotDTO>> getAvailableSlots(
            @Valid @RequestBody GetTimeSlotsRequest request) {
        List<TimeSlotDTO> slots = appointmentService.getTimeSlotsForDoctor(request);
        return ResponseEntity.ok(slots);
    }


    @PostMapping("/booking")
    public ResponseEntity<Appointment> bookAppointment(
            @Valid @RequestBody BookAppointmentRequest request) {
        Appointment result = appointmentService.bookAppointment(request);
        return ResponseEntity.ok(result);
    }


    @PutMapping("/cancel/{appointmentId}")
    public ResponseEntity<Appointment> cancelAppointment(
            @PathVariable Long appointmentId) {
        Appointment result = appointmentService.cancelAppointment(appointmentId);
        return ResponseEntity.ok(result);
    }
}