package com.hospital.management.doctorservice.controller;

import com.hospital.management.doctorservice.dto.DoctorAvailabilityRequestDTO;
import com.hospital.management.doctorservice.dto.MedicalHistoryDTO;
import com.hospital.management.doctorservice.dto.TimeSlotDTO;
import com.hospital.management.doctorservice.service.DoctorScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Tag(name = "Doctor Schedule API", description = "Manages doctor availability and slot booking")
@RestController
@RequestMapping("/api/v1/doctor-schedule")
@RequiredArgsConstructor
public class DoctorScheduleController {

    private final DoctorScheduleService scheduleService;

    /**
     * @Valid triggers validation on DoctorAvailabilityRequestDTO.
     * Internally verifies doctorId with Doctor Service before saving.
     * Returns 400 if doctorId does not exist in Doctor Service.
     * Returns 503 if Doctor Service is currently unavailable.
     */
    @Operation(summary = "Set doctor availability", description = "Creates shift and generates 30-min slots. Locks after save.")
    @PostMapping("/set-availability")
    public ResponseEntity<String> setAvailability(
            @Valid @RequestBody DoctorAvailabilityRequestDTO availabilityDTO) {

        log.info("Received set-availability request for doctorId={}",
                availabilityDTO.getDoctorId());
        String response = scheduleService.createMonthlySchedule(availabilityDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * GET: Returns slots for a doctor on a specific date as List<TimeSlotDTO>.
     *
     * CHANGED — Two things updated to match Appointment Service's Feign client:
     *   1. URL changed from /{doctorId}/{date}
     *              to /slots/doctor/{doctorId}/{date}
     *   2. Return type List<TimeSlotDTO>
     *      because Appointment Service expects fields:
     *      doctorId, patientId, startTime (LocalDateTime), createdAt
     */
    @Operation(summary = "Get Doctor Timeslots", description = "Returns all the time slots available for a particular doctor based on availability")
    @GetMapping("/slots/doctor/{doctorId}/{date}")
    public ResponseEntity<List<TimeSlotDTO>> getTimeSlotsByDoctorId(
            @PathVariable Long doctorId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("Fetching slots for doctorId={} on date={}", doctorId, date);
        return ResponseEntity.ok(scheduleService.getTimeSlotsByDoctorId(doctorId, date));
    }

    /**
     * PUT: Claim a slot — called by Appointment Service when patient books.
     * Why PUT and not PATCH?
     *   Appointment Service's Feign client uses @PutMapping — must match exactly.
     *
     * Why @RequestParam and not @PathVariable?
     *   Appointment Service sends doctorId, patientId, startTime as query params.
     *   Must match their Feign client signature exactly.
     *
     * Why startTime (LocalDateTime) and not slotId?
     *   Appointment Service does not store slotId — it stores the startTime.
     *   We find the slot by doctorId + startTime in the service layer.
     */
    @Operation(summary = "Update the Slot", description = "Update the slots if the slots are booked by patient, and marked true")
    @PutMapping("/slots/claim")
    public ResponseEntity<Void> claimTimeSlot(
            @RequestParam Long doctorId,
            @RequestParam Long patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startTime) {

        log.info("Claim slot request — doctorId={}, patientId={}, startTime={}",
                doctorId, patientId, startTime);
        scheduleService.claimTimeSlot(doctorId, patientId, startTime);
        // Returns 200 with no body — matches void in Appointment Service's Feign client
        return ResponseEntity.ok().build();
    }

    /**
     * PATCH: Releases a booked slot back to available.
     * Called by Appointment Service when patient cancels.
     * Uses doctorId + patientId + startTime — no slotId needed.
     */
    @Operation(summary = "Delete the booked Timeslot", description = "Patient can able to delete/cancel their booked timeslot")
    @PatchMapping("/cancel-booking")
    public ResponseEntity<String> cancelBooking(
            @RequestParam Long doctorId,
            @RequestParam Long patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startTime) {

        log.info("Cancel booking — doctorId={}, patientId={}, startTime={}",
                doctorId, patientId, startTime);
        scheduleService.cancelSlotBooking(doctorId, patientId, startTime);
        return ResponseEntity.ok().build();
    }

    /**
     * GET: Doctor clicks a booked slot to view patient's recent medical history.
     * Returns 3 most recent visits via Medical History microservice.
     * Returns empty list (not 500) if Medical History Service is unavailable.
     */
    @Operation(summary = "View the patient medical history", description = "Doctor can view the patient history based on their previous visit")
    @GetMapping("/view-history/{slotId}")
    public ResponseEntity<List<MedicalHistoryDTO>> viewPatientHistory(
            @PathVariable Long slotId) {

        log.info("View history request for slotId={}", slotId);
        List<MedicalHistoryDTO> filteredHistory =
                scheduleService.getPatientHistoryForDoctor(slotId);
        return ResponseEntity.ok(filteredHistory);
    }

    /**
     * GET: Returns all dates where this doctor has availability set.
     * Used by Appointment Service to populate the calendar date picker.
     */
    @Operation(summary = "Get doctor availability dates", description = "Patient can view the doctor availability dates")
    @GetMapping("/available-dates/{doctorId}")
    public ResponseEntity<List<LocalDate>> getAvailableDates(
            @PathVariable Long doctorId) {

        log.info("Available dates request for doctorId={}", doctorId);
        return ResponseEntity.ok(scheduleService.getAvailableDatesForDoctor(doctorId));
    }


}