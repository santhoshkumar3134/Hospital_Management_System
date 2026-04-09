package com.hospital.management.doctorservice.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO for the set-availability request.
 *
 * breakStart is optional — if the doctor does not provide it,
 * no long break is inserted (only the 3-slot rule applies).
 * If provided, a 60-minute break is automatically blocked from
 * breakStart to breakStart + 60 minutes.
 */
@Data
public class DoctorAvailabilityRequestDTO {

    @NotNull(message = "Doctor ID must not be null")
    private Long doctorId;

    @NotNull(message = "Date must not be null")
    @Future(message = "Availability date must be in the future")
    private LocalDate date;

    @NotNull(message = "Shift start time must not be null")
    private LocalTime shiftStart;

    @NotNull(message = "Shift end time must not be null")
    private LocalTime shiftEnd;

    // Optional — doctor provides the time they want to take a break.
    // If null, no long break is scheduled.
    // System always adds exactly 60 minutes from this time.
    private LocalTime breakStart;
}