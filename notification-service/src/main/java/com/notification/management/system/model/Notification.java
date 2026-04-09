package com.notification.management.system.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @NotNull(message = "Patient ID is required")
    @Positive(message = "Patient ID must be valid")
    @Column(nullable = false)
    private Long patientId;

    @NotNull(message = "Doctor ID is required")
    @Positive(message = "Doctor ID must be valid")
    @Column(nullable = false)
    private Long doctorId;

    @NotNull(message = "Appointment ID is required")
    @Column(nullable = false)
    private String appointmentId;

    @NotNull(message = "Appointment status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus appointmentStatus;

    @NotNull(message = "Notification status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus notificationStatus;

    @NotNull(message = "Recipient type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecipientType recipientType;

    @NotBlank(message = "Notification message cannot be empty")
    @Column(nullable = false, length = 1000) // Adjusted length for longer messages
    private String message;

    @PastOrPresent(message = "Notification timestamp cannot be in the future")
    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    public void init() {
        this.timestamp = LocalDateTime.now();
        if (this.notificationStatus == null) {
            this.notificationStatus = NotificationStatus.PENDING;
        }
    }
}