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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MessageBuilderService messageBuilderService;
    private final PatientServiceClient patientClient;
    private final DoctorServiceClient doctorClient;

    public List<NotificationResponseDTO> sendNotification(NotificationRequestDTO requestDTO) {

        String patientMessage = messageBuilderService.buildPatientMessage(requestDTO);
        String doctorMessage = messageBuilderService.buildDoctorMessage(requestDTO);

        Notification patientNotification = NotificationMapper.toPatientEntity(requestDTO, patientMessage);
        Notification doctorNotification = NotificationMapper.toDoctorEntity(requestDTO, doctorMessage);


        patientNotification.setNotificationStatus(NotificationStatus.SENT);
        doctorNotification.setNotificationStatus(NotificationStatus.SENT);

        List<Notification> savedList = notificationRepository.saveAll(
                List.of(patientNotification, doctorNotification)
        );


        return savedList.stream()
                .map(NotificationMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    public List<NotificationResponseDTO> getNotificationsByPatient(Long patientId) {

        try {
            patientClient.getPatientById(patientId);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Patient with ID " + patientId + " not found.");
        }

        return notificationRepository.findByPatientIdAndRecipientType(patientId, RecipientType.PATIENT)
                .stream()
                .map(NotificationMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<NotificationResponseDTO> getNotificationsByDoctor(Long doctorId) {

        try {
            doctorClient.getDoctorById(doctorId);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Doctor with ID " + doctorId + " not found.");
        }

        return notificationRepository.findByDoctorIdAndRecipientType(doctorId, RecipientType.DOCTOR)
                .stream()
                .map(NotificationMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<NotificationResponseDTO> getNotificationsByRecipientType(RecipientType type) {
        return notificationRepository.findByRecipientType(type).stream()
                .map(NotificationMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<NotificationResponseDTO> getPendingNotifications() {
        return notificationRepository.findByNotificationStatus(NotificationStatus.PENDING).stream()
                .map(NotificationMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}