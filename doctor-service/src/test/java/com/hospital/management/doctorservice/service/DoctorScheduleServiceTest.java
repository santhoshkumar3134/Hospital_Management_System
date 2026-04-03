package com.hospital.management.doctorservice.service;

import com.hospital.management.doctorservice.client.DoctorClient;
import com.hospital.management.doctorservice.client.MedicalHistoryClient;
import com.hospital.management.doctorservice.dto.DoctorAvailabilityRequestDTO;
import com.hospital.management.doctorservice.dto.DoctorResponseDTO;
import com.hospital.management.doctorservice.entity.DoctorAvailability;
import com.hospital.management.doctorservice.exception.ScheduleException;
import com.hospital.management.doctorservice.repository.DoctorAvailabilityRepository;
import com.hospital.management.doctorservice.repository.DoctorSlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorScheduleServiceTest {

    @Mock
    private DoctorAvailabilityRepository availabilityRepository;

    @Mock
    private DoctorSlotRepository slotRepository;

    @Mock
    private MedicalHistoryClient historyClient;

    @Mock
    private DoctorClient doctorClient;

    // Real ModelMapper — not a mock.
    // Maps DTO fields to Entity fields by name automatically.
    // Avoids mock matching issues with overloaded map() methods.
    private ModelMapper modelMapper = new ModelMapper();

    // Real service instance — created manually in @BeforeEach
    // so we can inject real ModelMapper alongside the mocks.
    private DoctorScheduleService scheduleService;

    // Runs before every single test method in this class.
    // Manually constructs the service passing real ModelMapper + all mocks.
    // This replaces @InjectMocks since ModelMapper is not a @Mock.
    @BeforeEach
    void setUp() {
        scheduleService = new DoctorScheduleService(
                availabilityRepository,
                slotRepository,
                historyClient,
                modelMapper,
                doctorClient
        );
    }

    // ── Test 1 ────────────────────────────────────────────────────────────────
    // Negative test — date only 1 day ahead must fail the 3-day advance rule.
    // Validates that the service throws ScheduleException with HTTP 400
    // and never reaches the repository save call.
    @Test
    void createMonthlySchedule_shouldThrowException_whenDateIsLessThan3DaysInAdvance() {

        // ── Arrange ───────────────────────────────────────────────────────
        DoctorAvailabilityRequestDTO requestDTO = new DoctorAvailabilityRequestDTO();
        requestDTO.setDoctorId(1L);
        requestDTO.setDate(LocalDate.now().plusDays(1)); // only 1 day ahead — must fail
        requestDTO.setShiftStart(LocalTime.of(9, 0));
        requestDTO.setShiftEnd(LocalTime.of(17, 0));

        // Validation 0 must pass so execution reaches validation 3 (3-day rule).
        // Without this mock, the service throws SERVICE_UNAVAILABLE instead.
        DoctorResponseDTO fakeDoctor = new DoctorResponseDTO();
        fakeDoctor.setId(1L);
        fakeDoctor.setName("Dr. Arjun");
        when(doctorClient.getDoctorById(1L)).thenReturn(fakeDoctor);

        // ── Act & Assert ──────────────────────────────────────────────────
        ScheduleException exception = assertThrows(
                ScheduleException.class,
                () -> scheduleService.createMonthlySchedule(requestDTO)
        );

        assertTrue(exception.getMessage().contains("at least 3 days in advance"));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());

        // Repository save must never be called — exception stops execution before that
        verify(availabilityRepository, never()).save(any());
    }

    // ── Test 2 ────────────────────────────────────────────────────────────────
    // Positive test — valid date, valid shift times, no break.
    // Service should complete successfully, save availability,
    // and generate slots via saveAll.
    @Test
    void createMonthlySchedule_shouldSucceed_whenDateIsValidAndShiftIsCorrect() {

        // ── Arrange ───────────────────────────────────────────────────────
        DoctorAvailabilityRequestDTO requestDTO = new DoctorAvailabilityRequestDTO();
        requestDTO.setDoctorId(1L);
        requestDTO.setDate(LocalDate.now().plusDays(5)); // 5 days ahead — passes 3-day rule
        requestDTO.setShiftStart(LocalTime.of(9, 0));
        requestDTO.setShiftEnd(LocalTime.of(18, 0));     // 9-hour shift, no break
        // breakStart is null — optional field, no long break for this shift

        // Validation 0 — doctor exists
        DoctorResponseDTO fakeDoctor = new DoctorResponseDTO();
        fakeDoctor.setId(1L);
        fakeDoctor.setName("Dr. Arjun");
        when(doctorClient.getDoctorById(1L)).thenReturn(fakeDoctor);

        // Validation 4 — no existing schedule for this doctor on this date
        when(availabilityRepository.findByDoctorIdAndDate(1L, LocalDate.now().plusDays(5)))
                .thenReturn(Optional.empty());

        // Slot save — return empty list, we only care that saveAll was called
        when(slotRepository.saveAll(any())).thenReturn(List.of());

        // ── Act ───────────────────────────────────────────────────────────
        String result = scheduleService.createMonthlySchedule(requestDTO);

        // ── Assert ────────────────────────────────────────────────────────
        assertTrue(result.contains("Schedule created and locked successfully"));

        // Availability must be saved exactly once
        verify(availabilityRepository, times(1)).save(any());

        // Slots must be generated and saved exactly once
        verify(slotRepository, times(1)).saveAll(any());
    }

    // ── Test 3 ────────────────────────────────────────────────────────────────
    // Negative test — shiftEnd before shiftStart must fail validation 1.
    // Validates that the service throws ScheduleException with HTTP 400
    // immediately after the shift time check.
    @Test
    void createMonthlySchedule_shouldThrowException_whenShiftEndIsBeforeShiftStart() {

        // ── Arrange ───────────────────────────────────────────────────────
        DoctorAvailabilityRequestDTO requestDTO = new DoctorAvailabilityRequestDTO();
        requestDTO.setDoctorId(1L);
        requestDTO.setDate(LocalDate.now().plusDays(5)); // valid date — passes validations 0 and 3
        requestDTO.setShiftStart(LocalTime.of(17, 0));   // 5 PM start
        requestDTO.setShiftEnd(LocalTime.of(9, 0));      // 9 AM end — before start, must fail

        // Validation 0 must pass so execution reaches validation 1 (shift time check)
        DoctorResponseDTO fakeDoctor = new DoctorResponseDTO();
        fakeDoctor.setId(1L);
        fakeDoctor.setName("Dr. Arjun");
        when(doctorClient.getDoctorById(1L)).thenReturn(fakeDoctor);

        // ── Act & Assert ──────────────────────────────────────────────────
        ScheduleException exception = assertThrows(
                ScheduleException.class,
                () -> scheduleService.createMonthlySchedule(requestDTO)
        );

        assertTrue(exception.getMessage().contains("Shift end time must be after shift start time"));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());

        // Repository save must never be called — invalid shift times stop execution
        verify(availabilityRepository, never()).save(any());
    }

    // ── Test 4 ────────────────────────────────────────────────────────────────
    // Negative test — schedule already exists for this doctor on this date.
    // Validates that the service throws ScheduleException with HTTP 409 Conflict
    // and never overwrites the existing locked schedule.
    @Test
    void createMonthlySchedule_shouldThrowException_whenScheduleAlreadyExists() {

        // ── Arrange ───────────────────────────────────────────────────────
        LocalDate futureDate = LocalDate.now().plusDays(5);

        DoctorAvailabilityRequestDTO requestDTO = new DoctorAvailabilityRequestDTO();
        requestDTO.setDoctorId(1L);
        requestDTO.setDate(futureDate);
        requestDTO.setShiftStart(LocalTime.of(9, 0));
        requestDTO.setShiftEnd(LocalTime.of(17, 0));

        // Validation 0 — doctor exists
        DoctorResponseDTO fakeDoctor = new DoctorResponseDTO();
        fakeDoctor.setId(1L);
        fakeDoctor.setName("Dr. Arjun");
        when(doctorClient.getDoctorById(1L)).thenReturn(fakeDoctor);

        // Validation 4 — simulate an existing locked schedule for this date.
        // Returning a present Optional triggers the CONFLICT exception.
        DoctorAvailability existingAvailability = new DoctorAvailability();
        existingAvailability.setDoctorId(1L);
        existingAvailability.setDate(futureDate);
        existingAvailability.setLocked(true);
        when(availabilityRepository.findByDoctorIdAndDate(1L, futureDate))
                .thenReturn(Optional.of(existingAvailability));

        // ── Act & Assert ──────────────────────────────────────────────────
        ScheduleException exception = assertThrows(
                ScheduleException.class,
                () -> scheduleService.createMonthlySchedule(requestDTO)
        );

        assertTrue(exception.getMessage().contains("already locked"));
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());

        // Repository save must never be called — existing schedule blocks saving
        verify(availabilityRepository, never()).save(any());
    }
}