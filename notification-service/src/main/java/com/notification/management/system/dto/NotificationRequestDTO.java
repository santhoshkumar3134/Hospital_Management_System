package com.notification.management.system.dto;

import com.notification.management.system.model.AppointmentStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class NotificationRequestDTO {
    private Long patientId;
    private Long doctorId;
    private Long appointmentId;
    private AppointmentStatus appointmentStatus; // Match the Enum!
    private LocalDate date;
    private LocalTime time;
}