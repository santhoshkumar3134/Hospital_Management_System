package com.notification.management.system.service;

import com.notification.management.system.client.DoctorServiceClient;
import com.notification.management.system.client.PatientServiceClient;
import com.notification.management.system.dto.NotificationMapper;
import com.notification.management.system.dto.NotificationRequestDTO;
import com.notification.management.system.dto.NotificationResponseDTO;
import com.notification.management.system.exception.ResourceNotFoundException;
import com.notification.management.system.model.Notification;
import com.notification.management.system.model.NotificationStatus;
import com.notification.management.system.model.RecipientType;
import com.notification.management.system.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MessageBuilderService messageBuilderService;
    private final PatientServiceClient patientClient;
    private final DoctorServiceClient doctorClient;

    public List<NotificationResponseDTO> sendNotification(NotificationRequestDTO requestDTO) {
        log.info("Initializing notification process for Patient ID: {} and Doctor ID: {}",
                requestDTO.getPatientId(), requestDTO.getDoctorId());

        String patientMessage = messageBuilderService.buildPatientMessage(requestDTO);
        String doctorMessage = messageBuilderService.buildDoctorMessage(requestDTO);

        Notification patientNotification = NotificationMapper.toPatientEntity(requestDTO, patientMessage);
        Notification doctorNotification = NotificationMapper.toDoctorEntity(requestDTO, doctorMessage);

        patientNotification.setNotificationStatus(NotificationStatus.SENT);
        doctorNotification.setNotificationStatus(NotificationStatus.SENT);

        List<Notification> savedList = notificationRepository.saveAll(
                List.of(patientNotification, doctorNotification)
        );

        log.info("Successfully saved and sent {} notifications.", savedList.size());

        return savedList.stream()
                .map(NotificationMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<NotificationResponseDTO> getNotificationsByPatient(Long patientId) {
        log.debug("Fetching notifications for Patient ID: {}", patientId);

        try {
            patientClient.getPatientById(patientId);
        } catch (Exception e) {
            log.error("Validation failed: Patient with ID {} does not exist. Error: {}", patientId, e.getMessage());
            throw new ResourceNotFoundException("Patient with ID " + patientId + " not found.");
        }

        List<NotificationResponseDTO> notifications = notificationRepository
                .findByPatientIdAndRecipientType(patientId, RecipientType.PATIENT)
                .stream()
                .map(NotificationMapper::toResponseDTO)
                .collect(Collectors.toList());

        log.info("Retrieved {} notifications for Patient ID: {}", notifications.size(), patientId);
        return notifications;
    }

    public List<NotificationResponseDTO> getNotificationsByDoctor(Long doctorId) {
        log.debug("Fetching notifications for Doctor ID: {}", doctorId);

        try {
            doctorClient.getDoctorById(doctorId);
        } catch (Exception e) {
            log.error("Validation failed: Doctor with ID {} does not exist. Error: {}", doctorId, e.getMessage());
            throw new ResourceNotFoundException("Doctor with ID " + doctorId + " not found.");
        }

        List<NotificationResponseDTO> notifications = notificationRepository
                .findByDoctorIdAndRecipientType(doctorId, RecipientType.DOCTOR)
                .stream()
                .map(NotificationMapper::toResponseDTO)
                .collect(Collectors.toList());

        log.info("Retrieved {} notifications for Doctor ID: {}", notifications.size(), doctorId);
        return notifications;
    }

    public List<NotificationResponseDTO> getNotificationsByRecipientType(RecipientType type) {
        log.info("Fetching all notifications for recipient type: {}", type);

        return notificationRepository.findByRecipientType(type).stream()
                .map(NotificationMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<NotificationResponseDTO> getPendingNotifications() {
        log.info("Fetching all notifications with status: {}", NotificationStatus.PENDING);

        List<NotificationResponseDTO> pending = notificationRepository
                .findByNotificationStatus(NotificationStatus.PENDING).stream()
                .map(NotificationMapper::toResponseDTO)
                .collect(Collectors.toList());

        log.debug("Found {} pending notifications", pending.size());
        return pending;
    }
}