package com.hospital.management.doctorservice.service;

import com.hospital.management.doctorservice.client.DoctorClient;
import com.hospital.management.doctorservice.client.MedicalHistoryClient;
import com.hospital.management.doctorservice.dto.DoctorAvailabilityRequestDTO;
import com.hospital.management.doctorservice.dto.DoctorResponseDTO;
import com.hospital.management.doctorservice.dto.MedicalHistoryDTO;
import com.hospital.management.doctorservice.dto.TimeSlotDTO;
import com.hospital.management.doctorservice.entity.DoctorAvailability;
import com.hospital.management.doctorservice.entity.DoctorSlot;
import com.hospital.management.doctorservice.exception.ResourceNotFoundException;
import com.hospital.management.doctorservice.exception.ScheduleException;
import com.hospital.management.doctorservice.repository.DoctorAvailabilityRepository;
import com.hospital.management.doctorservice.repository.DoctorSlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorScheduleService implements DoctorScheduleInterface {

    private final DoctorAvailabilityRepository availabilityRepository;
    private final DoctorSlotRepository slotRepository;
    private final MedicalHistoryClient historyClient;
    private final ModelMapper modelMapper;
    private final DoctorClient doctorClient;

    private static final int SLOT_DURATION_MINUTES  = 30;
    private static final int SLOTS_BEFORE_BREAK     = 3;
    private static final int BREAK_DURATION_MINUTES = 60;
    private static final int MIN_DAYS_IN_ADVANCE    = 3;
    private static final int MAX_HISTORY_RECORDS    = 3;

    /**
     * Creates a locked availability record and generates all slots.
     *
     * Validations (in order):
     *  0. doctorId must exist in Doctor Service
     *  1. shiftEnd must be after shiftStart
     *  2. If breakStart is provided:
     *     - breakStart must be within the shift window
     *     - breakEnd (breakStart + 60 min) must not exceed shiftEnd
     *  3. Date must be at least 3 days in advance
     *  4. No existing record for this doctor on this date (lock check)
     */
    @Override
    @Transactional
    public String createMonthlySchedule(DoctorAvailabilityRequestDTO availabilityDTO) {
        log.info("Creating schedule for doctorId={} on date={}",
                availabilityDTO.getDoctorId(), availabilityDTO.getDate());

        // --- Validation 0: Verify doctorId exists in Doctor Service ---
        // Two failure cases handled separately:
        //   - 404 from Doctor Service → doctorId is invalid → BAD_REQUEST
        //   - Any other exception → Doctor Service is down → SERVICE_UNAVAILABLE
        try {
            DoctorResponseDTO doctor = doctorClient.getDoctorById(availabilityDTO.getDoctorId());
            log.info("Doctor verified — name={}, specialization={}",
                    doctor.getName(), doctor.getSpecialization());
        } catch (feign.FeignException.NotFound e) {
            throw new ScheduleException(
                    "Doctor with id=" + availabilityDTO.getDoctorId()
                            + " does not exist. Cannot create schedule.",
                    HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Doctor Service unavailable while verifying doctorId={}. Error: {}",
                    availabilityDTO.getDoctorId(), e.getMessage());
            throw new ScheduleException(
                    "Doctor Service is currently unavailable. Please try again later.",
                    HttpStatus.SERVICE_UNAVAILABLE);
        }

        // Map DTO → Entity
        //(source,destination)
        DoctorAvailability availability = modelMapper.map(availabilityDTO, DoctorAvailability.class);

        // --- Validation 1: shiftEnd must be after shiftStart ---
        if (!availability.getShiftEnd().isAfter(availability.getShiftStart())) {
            throw new ScheduleException(
                    "Shift end time must be after shift start time.",
                    HttpStatus.BAD_REQUEST);
        }

        // --- Validation 2: breakStart sanity checks (only if doctor provided it) ---
        if (availability.getBreakStart() != null) {
            LocalTime breakEnd = availability.getBreakStart()
                    .plusMinutes(BREAK_DURATION_MINUTES);

            // breakStart must fall within the shift window
            if (!availability.getBreakStart().isAfter(availability.getShiftStart())
                    || !availability.getBreakStart().isBefore(availability.getShiftEnd())) {
                throw new ScheduleException(
                        "Break start time must be within the shift window ("
                                + availability.getShiftStart() + " - "
                                + availability.getShiftEnd() + ").",
                        HttpStatus.BAD_REQUEST);
            }

            // breakEnd must not exceed shiftEnd — otherwise no slots after break
            if (breakEnd.isAfter(availability.getShiftEnd())) {
                throw new ScheduleException(
                        "Break end time (" + breakEnd + ") exceeds shift end time ("
                                + availability.getShiftEnd() + "). "
                                + "Please shorten the shift or adjust the break time.",
                        HttpStatus.BAD_REQUEST);
            }
        }

        // --- Validation 3: Must be at least MIN_DAYS_IN_ADVANCE days from today ---
        long daysBetween = ChronoUnit.DAYS.between(LocalDate.now(), availability.getDate());
        if (daysBetween < MIN_DAYS_IN_ADVANCE) {
            throw new ScheduleException(
                    "Availability must be set at least " + MIN_DAYS_IN_ADVANCE
                            + " days in advance. Days provided: " + daysBetween,
                    HttpStatus.BAD_REQUEST);
        }

        // --- Validation 4: Lock check — cannot overwrite an existing schedule ---
        availabilityRepository
                .findByDoctorIdAndDate(availability.getDoctorId(), availability.getDate())
                .ifPresent(existing -> {
                    throw new ScheduleException(
                            "Schedule for doctorId=" + availability.getDoctorId()
                                    + " on " + availability.getDate()
                                    + " is already locked and cannot be updated.",
                            HttpStatus.CONFLICT);
                });

        // Lock and save the availability record
        availability.setLocked(true);
        availability.setAvailable(true);
        availabilityRepository.save(availability);
        log.info("Availability saved and locked for doctorId={}", availability.getDoctorId());

        // Generate and persist all time slots
        int slotsCreated = generateTimeSlots(availability);
        log.info("Generated {} slots for doctorId={} on {}",
                slotsCreated, availability.getDoctorId(), availability.getDate());

        return "Schedule created and locked successfully. Slots generated: " + slotsCreated;
    }

    /**
     * Releases a booked slot back to available pool.
     * Finds slot by doctorId + startTime — same pattern as claimTimeSlot.
     * Also verifies patientId matches to prevent wrong patient cancelling.
     */
    @Override
    @Transactional
    public void cancelSlotBooking(Long doctorId, Long patientId, LocalDateTime startTime) {
        log.info("Cancel booking — doctorId={}, patientId={}, startTime={}",
                doctorId, patientId, startTime);

        LocalDate slotDate = startTime.toLocalDate();
        LocalTime slotTime = startTime.toLocalTime();

        DoctorSlot slot = slotRepository
                .findByDoctorIdAndSlotDateAndStartTime(doctorId, slotDate, slotTime)
                .orElseThrow(() -> new ScheduleException(
                        "No slot found for doctorId=" + doctorId + " at " + startTime,
                        HttpStatus.NOT_FOUND));

        // Guard — only booked slots can be cancelled
        if (!slot.isBooked()) {
            throw new ScheduleException(
                    "Slot at " + startTime + " is not booked. Cannot cancel.",
                    HttpStatus.BAD_REQUEST);
        }

        // Verify the correct patient is cancelling — prevents wrong patient cancelling
        if (slot.getPatientId() == null || !slot.getPatientId().equals(patientId)) {
            throw new ScheduleException(
                    "PatientId=" + patientId + " did not book this slot. Cannot cancel.",
                    HttpStatus.FORBIDDEN);
        }

        slot.setBooked(false);
        slot.setPatientId(null);
        slotRepository.save(slot);
        log.info("Slot at {} for doctorId={} released back to available pool", startTime, doctorId);
    }

    /**
     * Generates 30-minute bookable slots across the doctor's declared shift.
     *
     * RULES (in the order the loop applies them):
     *
     *  Rule A — Long break window:
     *    If the doctor provided a breakStart, a 60-minute break is blocked.
     *    breakEnd = breakStart + 60 min.
     *    If currentTime falls inside [breakStart, breakEnd), skip to breakEnd
     *    and reset the 3-slot counter.
     *
     *  Rule B — Break straddle guard:
     *    If the next slot's 30-minute window would overlap the breakStart
     *    (e.g. currentTime=12:45, breakStart=13:00 → slot runs 12:45–13:15),
     *    skip directly to breakEnd instead of creating a partial slot.
     *    Counter resets here too.
     *
     *  Rule C — 3-slot rule:
     *    After every 3 consecutive slots, advance the clock by an extra
     *    30 minutes (mandatory short break) and reset the counter.
     *    This short break is separate from the long break in Rule A.
     *
     * Why this order matters:
     *    The long break check must come BEFORE the straddle guard,
     *    because if currentTime is exactly at breakStart we want Rule A
     *    to fire, not Rule B. Rule B handles the approach to breakStart.
     */
    private int generateTimeSlots(DoctorAvailability availability) {
        List<DoctorSlot> slots = new ArrayList<>();
        LocalTime currentTime = availability.getShiftStart();
        LocalTime shiftEnd    = availability.getShiftEnd();

        // Derive breakEnd only if breakStart was provided — otherwise both are null
        // and Rules A & B are skipped entirely for shifts with no long break.
        LocalTime breakStart = availability.getBreakStart();
        LocalTime breakEnd   = (breakStart != null)
                ? breakStart.plusMinutes(BREAK_DURATION_MINUTES)
                : null;

        int slotCount = 0;

        log.debug("Generating slots: shift={}-{}, break={}-{}",
                currentTime, shiftEnd, breakStart, breakEnd);

        while (currentTime.isBefore(shiftEnd)) {

            // ── Rule A: Inside the long break window ──────────────────────────
            if (breakStart != null
                    && !currentTime.isBefore(breakStart)
                    && currentTime.isBefore(breakEnd)) {

                log.debug("Long break window reached at {}. Jumping to {}", currentTime, breakEnd);
                currentTime = breakEnd;
                slotCount = 0;
                continue;
            }

            // ── Rule B: Straddle guard ────────────────────────────────────────
            if (breakStart != null
                    && currentTime.isBefore(breakStart)
                    && currentTime.plusMinutes(SLOT_DURATION_MINUTES).isAfter(breakStart)) {

                log.debug("Slot at {} would straddle break start {}. Jumping to {}",
                        currentTime, breakStart, breakEnd);
                currentTime = breakEnd;
                slotCount = 0;
                continue;
            }

            // ── Create the slot ───────────────────────────────────────────────
            DoctorSlot slot = new DoctorSlot();
            slot.setDoctorId(availability.getDoctorId());
            slot.setSlotDate(availability.getDate());
            slot.setStartTime(currentTime);
            slot.setBooked(false);
            slots.add(slot);

            log.debug("Slot created at {}", currentTime);

            currentTime = currentTime.plusMinutes(SLOT_DURATION_MINUTES);
            slotCount++;

            // ── Rule C: 3-slot mandatory short break ─────────────────────────
            if (slotCount == SLOTS_BEFORE_BREAK) {
                log.debug("3 consecutive slots done. Inserting 30-min short break at {}",
                        currentTime);
                currentTime = currentTime.plusMinutes(SLOT_DURATION_MINUTES);
                slotCount = 0;
            }
        }

        slotRepository.saveAll(slots);
        return slots.size();
    }

    /**
     * Fetches and returns the 3 most recent medical history records
     * for the patient assigned to the given slot.
     * Feign call is wrapped in try-catch — graceful degradation if
     * Medical History Service is down.
     */
    @Override
    public List<MedicalHistoryDTO> getPatientHistoryForDoctor(Long slotId) {
        log.info("Fetching patient history for slotId={}", slotId);

        DoctorSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("DoctorSlot", slotId));

        // Guard: only booked slots have a patient assigned
        if (!slot.isBooked() || slot.getPatientId() == null) {
            throw new ScheduleException(
                    "No patient assigned to slotId=" + slotId + ". Slot is not yet booked.",
                    HttpStatus.BAD_REQUEST);
        }

        List<MedicalHistoryDTO> fullHistory;
        try {
            fullHistory = historyClient.getPatientHistory(slot.getPatientId());
            log.info("Fetched {} history records for patientId={}",
                    fullHistory.size(), slot.getPatientId());
        } catch (Exception e) {
            log.warn("Medical history service unavailable for patientId={}. Error: {}",
                    slot.getPatientId(), e.getMessage());
            return List.of();
        }

        return fullHistory.stream()
                .sorted((h1, h2) -> h2.getVisitDate().compareTo(h1.getVisitDate()))
                .limit(MAX_HISTORY_RECORDS)
                .toList();
    }

    /**
     * Primary booking method for the Appointment Service.
     * Appointment Service sends doctorId + patientId + startTime (LocalDateTime).
     * Splits startTime into LocalDate + LocalTime to query the DB.
     * Guards against double-booking and slot-not-found.
     */
    @Override
    @Transactional
    public void claimTimeSlot(Long doctorId, Long patientId, LocalDateTime startTime) {
        log.info("Claim request — doctorId={}, patientId={}, startTime={}",
                doctorId, patientId, startTime);

        // Split LocalDateTime into LocalDate and LocalTime to query the DB
        LocalDate slotDate = startTime.toLocalDate();
        LocalTime slotTime = startTime.toLocalTime();

        DoctorSlot slot = slotRepository
                .findByDoctorIdAndSlotDateAndStartTime(doctorId, slotDate, slotTime)
                .orElseThrow(() -> new ScheduleException(
                        "No slot found for doctorId=" + doctorId + " at " + startTime,
                        HttpStatus.NOT_FOUND));

        // Double-booking guard
        if (slot.isBooked()) {
            throw new ScheduleException(
                    "Slot at " + startTime + " for doctorId=" + doctorId
                            + " is already booked by patientId=" + slot.getPatientId(),
                    HttpStatus.CONFLICT);
        }

        slot.setBooked(true);
        slot.setPatientId(patientId);
        slotRepository.save(slot);
        log.info("Slot claimed successfully for patientId={} at {}", patientId, startTime);
    }

    /**
     * Returns all slots for a doctor on a specific date as List<TimeSlotDTO>.
     * startTime is combined into LocalDateTime because Appointment Service
     * sends it back as LocalDateTime in the claim request.
     */
    @Override
    public List<TimeSlotDTO> getTimeSlotsByDoctorId(Long doctorId, LocalDate date) {
        log.info("Fetching slots for doctorId={} on date={}", doctorId, date);

        return slotRepository.findByDoctorIdAndSlotDate(doctorId, date)
                .stream()
                .map(slot -> new TimeSlotDTO(
                        LocalDateTime.of(slot.getSlotDate(), slot.getStartTime()),
                        slot.isBooked()
                ))
                .toList();
    }

    /**
     * Returns all dates where this doctor has availability set
     * and isAvailable is true — from today onwards.
     * Used by Appointment Service to populate the date picker calendar.
     */
    @Override
    public List<LocalDate> getAvailableDatesForDoctor(Long doctorId) {
        log.info("Fetching available dates for doctorId={}", doctorId);

        return availabilityRepository
                .findByDoctorIdAndDateGreaterThanEqualAndIsAvailableTrue(
                        doctorId, LocalDate.now())
                .stream()
                .map(DoctorAvailability::getDate)
                .sorted()
                .toList();
    }
}