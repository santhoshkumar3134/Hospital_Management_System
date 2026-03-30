package com.hospital.management.doctorservice.service;

import com.hospital.management.doctorservice.client.MedicalHistoryClient;
import com.hospital.management.doctorservice.dto.MedicalHistoryDTO;
import com.hospital.management.doctorservice.entity.DoctorAvailability;
import com.hospital.management.doctorservice.entity.DoctorSlot;
import com.hospital.management.doctorservice.repository.DoctorAvailabilityRepository;
import com.hospital.management.doctorservice.repository.DoctorSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorScheduleService {

    @Autowired
    private DoctorAvailabilityRepository availabilityRepository;

    @Autowired
    private DoctorSlotRepository slotRepository;

    @Autowired
    private MedicalHistoryClient historyClient;

    /**
     * Logic for the "Next 3rd Day" and locking mechanism.
     */
    @Transactional
    public String createMonthlySchedule(DoctorAvailability availability) {
        // 1. Rule Check: Must be exactly 3 days in advance (or more)
        long daysBetween = ChronoUnit.DAYS.between(LocalDate.now(), availability.getDate());
        if (daysBetween < 3) {
            return "Error: Availability must be set at least 3 days in advance.";
        }

        // 2. Rule Check: Cannot update if already exists (Locked)
        var existing = availabilityRepository.findByDoctorIdAndDate(
                availability.getDoctorId(), availability.getDate());
        if (existing.isPresent()) {
            return "Error: Schedule for this date is already locked and cannot be updated.";
        }

        // 3. Save Availability
        availabilityRepository.save(availability);

        // 4. Generate the 30-min Slots
        generateTimeSlots(availability);

        return "Schedule created and locked successfully.";
    }

    /**
     * Generates slots with the 3-slot rule and fixed lunch break.
     */
    private void generateTimeSlots(DoctorAvailability availability) {
        List<DoctorSlot> slots = new ArrayList<>();
        LocalTime currentTime = availability.getShiftStart();
        LocalTime endTime = availability.getShiftEnd();
        LocalTime lunchStart = LocalTime.of(13, 0); // 1:00 PM
        LocalTime lunchEnd = LocalTime.of(14, 0);   // 2:00 PM

        int slotCount = 0;

        while (currentTime.isBefore(endTime)) {
            // A. Lunch Break Logic (Fixed 1pm-2pm)
            if (!currentTime.isBefore(lunchStart) && currentTime.isBefore(lunchEnd)) {
                currentTime = lunchEnd;
                slotCount = 0; // Reset counter after lunch break
                continue;
            }

            // B. Create Slot
            DoctorSlot slot = new DoctorSlot();
            slot.setDoctorId(availability.getDoctorId());
            slot.setSlotDate(availability.getDate());
            slot.setStartTime(currentTime);
            slot.setBooked(false); // Initially unbooked
            slots.add(slot);

            // C. Increment Time and Counter
            currentTime = currentTime.plusMinutes(30);
            slotCount++;

            // D. "3 Slots then 30m Break" Rule
            if (slotCount == 3) {
                currentTime = currentTime.plusMinutes(30);
                slotCount = 0; // Reset counter after short break
            }
        }
        slotRepository.saveAll(slots);
    }

    /**
     * Called when the doctor clicks a slot. Fetches history and filters to last 3 entries.
     */
    public List<MedicalHistoryDTO> getPatientHistoryForDoctor(Long slotId) {
        // 1. Fetch the slot
        DoctorSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        // 2. Check if booked
        if (!slot.isBooked() || slot.getPatientId() == null) {
            throw new RuntimeException("No patient assigned to this slot.");
        }

        // 3. Fetch and Filter (Last 3 visits)
        List<MedicalHistoryDTO> fullHistory = historyClient.getPatientHistory(slot.getPatientId());

        return fullHistory.stream()
                .sorted((h1, h2) -> h2.getVisitDate().compareTo(h1.getVisitDate())) // Sort by newest date first
                .limit(3)
                .toList();
    }

    /**
     * Updates slot when the Notification/Alert service sends a booking confirmation.
     */
    @Transactional
    public void updateSlotBookingStatus(Long slotId, Long patientId) {
        DoctorSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        slot.setBooked(true);
        slot.setPatientId(patientId);
        slotRepository.save(slot);
    }

    /**
     * Used by the Frontend to show the doctor's current schedule.
     */
    public List<DoctorSlot> getScheduleForDoctor(Long doctorId, LocalDate date) {
        return slotRepository.findByDoctorIdAndSlotDate(doctorId, date);
    }
}