package com.notification.management.system.model;

import jakarta.persistence.*;
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

    private Long patientId;
    private Long doctorId;
    private Long appointmentId;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus appointmentStatus;

    @Enumerated(EnumType.STRING)
    private NotificationStatus notificationStatus;

    @Enumerated(EnumType.STRING)
    private RecipientType recipientType;

    private String message;
    private LocalDateTime timestamp;

    @PrePersist
    public void init() {
        this.timestamp = LocalDateTime.now();
        if (this.notificationStatus == null) this.notificationStatus = NotificationStatus.PENDING;
    }
}