package com.hospital.management.doctorservice.service;

import com.hospital.management.doctorservice.repository.DoctorAvailabilityRepository;
import com.hospital.management.doctorservice.repository.DoctorSlotRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@Slf4j
@ExtendWith(MockitoExtension.class)
class ScheduleCleanupServiceTest {

    @Mock
    private DoctorAvailabilityRepository availabilityRepository;

    @Mock
    private DoctorSlotRepository slotRepository;

    @InjectMocks
    private ScheduleCleanupService cleanupService;

    @Test
    @DisplayName("Should call delete methods with today's date when wipeOldSchedules is triggered")
    void testWipeOldSchedules() {
        // 1. Arrange
        LocalDate today = LocalDate.now();

        // 2. Act
        cleanupService.wipeOldSchedules();

        // 3. Assert: Verify the repositories were called exactly once
        // and that they were passed today's date as the boundary
        verify(availabilityRepository, times(1)).deleteByDateBefore(today);
        verify(slotRepository, times(1)).deleteBySlotDateBefore(today);

        log.info("Cleanup test passed: Repositories triggered correctly.");
    }
}