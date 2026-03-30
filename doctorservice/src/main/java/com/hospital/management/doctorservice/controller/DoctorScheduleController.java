package com.hospital.management.doctorservice.controller;

import com.hospital.management.doctorservice.dto.MedicalHistoryDTO;
import com.hospital.management.doctorservice.entity.DoctorAvailability;
import com.hospital.management.doctorservice.entity.DoctorSlot;
import com.hospital.management.doctorservice.service.DoctorScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/doctor-schedule")
public class DoctorScheduleController {

    @Autowired
    private DoctorScheduleService scheduleService;

    /**
     * POST: Doctor sets their availability for a day (locked 3 days in advance).
     * The RequestBody will be a JSON matching the DoctorAvailability entity.
     */
    @PostMapping("/set-availability")
    public ResponseEntity<String> setAvailability(@RequestBody DoctorAvailability availability) {
        String response = scheduleService.createMonthlySchedule(availability);
        return ResponseEntity.ok(response);
    }

    /**
     * GET: Returns the full schedule for a doctor on a specific date.
     * Used by the doctor to see their day's plan.
     */
    @GetMapping("/{doctorId}/{date}")
    public ResponseEntity<List<DoctorSlot>> getDoctorSchedule(
            @PathVariable Long doctorId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(scheduleService.getScheduleForDoctor(doctorId, date));
    }

    /**
     * PATCH: Called when an 'Alert' or 'Notification' triggers a booking.
     * Updates a specific slot with a Patient ID and marks it as booked.
     */
    @PatchMapping("/confirm-booking/{slotId}/{patientId}")
    public ResponseEntity<String> confirmBooking(
            @PathVariable Long slotId,
            @PathVariable Long patientId) {
        scheduleService.updateSlotBookingStatus(slotId, patientId);
        return ResponseEntity.ok("Booking confirmed and slot updated.");
    }

    /**
     * GET: The "Click" action.
     * When the doctor clicks a booked slot, this fetches the filtered medical history.
     */
    @GetMapping("/view-history/{slotId}")
    public ResponseEntity<List<MedicalHistoryDTO>> viewPatientHistory(@PathVariable Long slotId) {
        // Now returns the specific DTO instead of Object
        List<MedicalHistoryDTO> filteredHistory = scheduleService.getPatientHistoryForDoctor(slotId);
        return ResponseEntity.ok(filteredHistory);
    }
}