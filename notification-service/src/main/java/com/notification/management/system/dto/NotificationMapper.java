package com.notification.management.system.dto;

import com.notification.management.system.model.Notification;
import com.notification.management.system.model.NotificationStatus;
import com.notification.management.system.model.RecipientType;

public class NotificationMapper {

    public static Notification toPatientEntity(NotificationRequestDTO dto, String message) {
        return Notification.builder()
                .patientId(dto.getPatientId())
                .doctorId(dto.getDoctorId())
                .appointmentId(dto.getAppointmentId())
                .appointmentStatus(dto.getAppointmentStatus())
                .message(message)
                .recipientType(RecipientType.PATIENT)
                .notificationStatus(NotificationStatus.PENDING)
                .build();
    }

    public static Notification toDoctorEntity(NotificationRequestDTO dto, String message) {
        return Notification.builder()
                .patientId(dto.getPatientId())
                .doctorId(dto.getDoctorId())
                .appointmentId(dto.getAppointmentId())
                .appointmentStatus(dto.getAppointmentStatus())
                .message(message)
                .recipientType(RecipientType.DOCTOR)
                .notificationStatus(NotificationStatus.PENDING)
                .build();
    }

    public static NotificationResponseDTO toResponseDTO(Notification entity) {

        return new NotificationResponseDTO(
                entity.getNotificationId(),
                entity.getPatientId(),
                entity.getDoctorId(),
                entity.getMessage(),
                entity.getNotificationStatus()
        );
    }
}