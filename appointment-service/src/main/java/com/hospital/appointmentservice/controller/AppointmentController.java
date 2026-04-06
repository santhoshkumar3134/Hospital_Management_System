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


    @PutMapping("/cancel/{confirmationCode}")
    public ResponseEntity<Appointment> cancelAppointment(
            @PathVariable String confirmationCode) {
        Appointment result = appointmentService.cancelAppointment(confirmationCode);
        return ResponseEntity.ok(result);
    }

    /**
     * Reschedule an existing appointment to a new time slot
     * Request: { "confirmationCode": "550e8400-e29b-41d4-a716-446655440000", "newAppointmentTime": "2026-04-10T14:00:00" }
     * Response: Rescheduled Appointment details
     */
    @PutMapping("/reschedule")
    public ResponseEntity<Appointment> rescheduleAppointment(
            @Valid @RequestBody RescheduleAppointmentRequest request) {
        Appointment result = appointmentService.rescheduleAppointment(request);
        return ResponseEntity.ok(result);
    }
}