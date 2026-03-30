package com.hospital.management.doctorservice.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class DoctorAvailabilityRequestDTO {
    private Long doctorId;
    private LocalDate date;
    private LocalTime shiftStart;
    private LocalTime shiftEnd;
}