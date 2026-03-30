package com.hospital.management.doctorservice.service;

import com.hospital.management.doctorservice.repository.DoctorAvailabilityRepository;
import com.hospital.management.doctorservice.repository.DoctorSlotRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;

public class ScheduleCleanupService {

    @Autowired
    private DoctorAvailabilityRepository availabilityRepository;

    @Autowired
    private DoctorSlotRepository slotRepository;

    /**
     * This task runs at midnight (00:00:00) every day.
     * It wipes all availability and slots for dates before today.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void wipeOldSchedules() {
        LocalDate today = LocalDate.now();

        // Custom delete methods in your repositories
        availabilityRepository.deleteByDateBefore(today);
        slotRepository.deleteBySlotDateBefore(today);

        System.out.println("Midnight Wipe Successful: Cleared records before " + today);
    }
}
