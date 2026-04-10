package com.notification.management.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.notification.management.system.model.AppointmentStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Getter
@Setter
@Builder
@AllArgsConstructor@NoArgsConstructor
public class NotificationRequestDTO {

    @NotNull(message = "Patient ID cannot be null")
    @Positive(message = "Patient ID must be a positive number")
    private Long patientId;

    @NotNull(message = "Doctor ID cannot be null")
    @Positive(message = "Doctor ID must be a positive number")
    private Long doctorId;

    @NotNull(message = "Appointment ID cannot be null")
    private String confirmationCode;

    @NotNull(message = "Appointment status is required")
    private AppointmentStatus appointmentStatus;

    @NotNull(message = "Appointment date is required")
    @FutureOrPresent(message = "Appointment date cannot be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @NotNull(message = "Appointment time is required")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime time;

}