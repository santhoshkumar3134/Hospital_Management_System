package com.hospital.management.doctorservice.service;

import com.hospital.management.doctorservice.repository.DoctorAvailabilityRepository;
import com.hospital.management.doctorservice.repository.DoctorSlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleCleanupService {

    private final DoctorAvailabilityRepository availabilityRepository;
    private final DoctorSlotRepository slotRepository;

    /**
     * Runs at exactly midnight every day (00:00:00).
     * Deletes all DoctorAvailability and DoctorSlot records whose date
     * is before today, keeping the rolling 3-day window clean.
     * Cron format: second minute hour day month weekday
     * "0 0 0 * * *" = at 00:00:00, every day, every month, every weekday
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void wipeOldSchedules() {
        LocalDate today = LocalDate.now();
        log.info("Midnight cleanup started. Removing records before {}", today);

        availabilityRepository.deleteByDateBefore(today);
        slotRepository.deleteBySlotDateBefore(today);

        log.info("Midnight cleanup completed successfully for date boundary: {}", today);
    }
}