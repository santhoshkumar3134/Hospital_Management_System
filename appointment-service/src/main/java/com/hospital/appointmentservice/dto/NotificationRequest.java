package com.hospital.appointmentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private Long patientId;
    private Long doctorId;
    private Long appointmentId;
    private String status;           // CONFIRMED, CANCELLED, COMPLETED
    private LocalTime time;      // Appointment time
    private LocalDate date;
    // Appointment date
}
