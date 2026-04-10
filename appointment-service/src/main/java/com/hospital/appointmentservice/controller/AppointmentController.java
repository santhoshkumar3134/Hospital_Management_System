package com.hospital.appointmentservice.controller;

import com.hospital.appointmentservice.model.Appointment;
import com.hospital.appointmentservice.dto.*;
import com.hospital.appointmentservice.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentController.class);
    
    private final AppointmentService appointmentService;


    @PostMapping("/available-doctors")
    public ResponseEntity<List<DoctorAvailabilityDTO>> getAvailableDoctors(
            @Valid @RequestBody GetAvailableDoctorsRequest request) {
        logger.info("GET /available-doctors request: patientId={}, specialization={}", 
                request.patientId(), request.specialization());
        List<DoctorAvailabilityDTO> doctors = appointmentService.getAvailableDoctorsBySpecialization(request);
        logger.info("GET /available-doctors response: returned {} doctors", doctors.size());
        return ResponseEntity.ok(doctors);
    }


    @PostMapping("/available-slots")
    public ResponseEntity<List<TimeSlotDTO>> getAvailableSlots(
            @Valid @RequestBody GetTimeSlotsRequest request) {
        logger.info("POST /available-slots request: patientId={}, doctorId={}, date={}", 
                request.patientId(), request.doctorId(), request.date());
        List<TimeSlotDTO> slots = appointmentService.getTimeSlotsForDoctor(request);
        logger.info("POST /available-slots response: returned {} slots for doctorId={}", 
                slots.size(), request.doctorId());
        return ResponseEntity.ok(slots);
    }


    @PostMapping("/booking")
    public ResponseEntity<Appointment> bookAppointment(
            @Valid @RequestBody BookAppointmentRequest request) {
        logger.info("POST /booking request: patientId={}, doctorId={}, startTime={}", 
                request.patientId(), request.doctorId(), request.startTime());
        try {
            Appointment appointment = appointmentService.bookAppointment(request);
            logger.info("POST /booking response: Appointment booked successfully with confirmationCode={}", 
                    appointment.getConfirmationCode());
            return ResponseEntity.ok(appointment);
        } catch (Exception e) {
            logger.error("POST /booking error: Failed to book appointment for patientId={}, doctorId={}", 
                    request.patientId(), request.doctorId(), e);
            throw e;
        }
    }


    @PutMapping("/cancel/{confirmationCode}")
    public ResponseEntity<Appointment> cancelAppointment(
            @PathVariable String confirmationCode) {
        logger.info("PUT /cancel/{} request: confirmationCode={}", confirmationCode, confirmationCode);
        try {
            Appointment appointment = appointmentService.cancelAppointment(confirmationCode);
            logger.info("PUT /cancel response: Appointment cancelled successfully with confirmationCode={}", 
                    confirmationCode);
            return ResponseEntity.ok(appointment);
        } catch (Exception e) {
            logger.error("PUT /cancel error: Failed to cancel appointment with confirmationCode={}", 
                    confirmationCode, e);
            throw e;
        }
    }

    /**
     * Reschedule an existing appointment to a new time slot
     * Request: { "confirmationCode": "550e8400-e29b-41d4-a716-446655440000", "newAppointmentTime": "2026-04-10T14:00:00" }
     * Response: Rescheduled Appointment details
     */
    @PutMapping("/reschedule")
    public ResponseEntity<Appointment> rescheduleAppointment(
            @Valid @RequestBody RescheduleAppointmentRequest request) {
        logger.info("PUT /reschedule request: confirmationCode={}, newAppointmentTime={}", 
                request.confirmationCode(), request.newAppointmentTime());
        try {
            Appointment result = appointmentService.rescheduleAppointment(request);
            logger.info("PUT /reschedule response: Appointment rescheduled successfully, confirmationCode={}", 
                    result.getConfirmationCode());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("PUT /reschedule error: Failed to reschedule appointment with confirmationCode={}", 
                    request.confirmationCode(), e);
            throw e;
        }
    }
}