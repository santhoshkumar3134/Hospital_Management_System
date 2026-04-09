package com.notification.management.system.service;

import com.notification.management.system.client.DoctorServiceClient;
import com.notification.management.system.client.PatientServiceClient;
import com.notification.management.system.dto.NotificationRequestDTO;
import com.notification.management.system.dto.NotificationResponseDTO;
import com.notification.management.system.exception.ResourceNotFoundException;
import com.notification.management.system.model.Notification;
import com.notification.management.system.model.NotificationStatus;
import com.notification.management.system.model.RecipientType;
import com.notification.management.system.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private MessageBuilderService messageBuilderService;
    @Mock
    private PatientServiceClient patientClient;
    @Mock
    private DoctorServiceClient doctorClient;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void sendNotification_ShouldSaveTwoNotifications() {
        // Arrange
        NotificationRequestDTO request = new NotificationRequestDTO();
        when(messageBuilderService.buildPatientMessage(any())).thenReturn("Patient Msg");
        when(messageBuilderService.buildDoctorMessage(any())).thenReturn("Doctor Msg");
        when(notificationRepository.saveAll(anyList())).thenReturn(List.of(new Notification(), new Notification()));

        // Act
        List<NotificationResponseDTO> responses = notificationService.sendNotification(request);

        // Assert
        assertEquals(2, responses.size());
        verify(notificationRepository, times(1)).saveAll(anyList());
    }

    @Test
    void getNotificationsByPatient_ThrowsExceptionWhenPatientNotFound() {
        // Arrange
        Long patientId = 1L;
        when(patientClient.getPatientById(patientId)).thenThrow(new RuntimeException("API Down"));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            notificationService.getNotificationsByPatient(patientId);
        });
    }

    @Test
    void getNotificationsByRecipientType_ReturnsList() {
        // Arrange
        when(notificationRepository.findByRecipientType(RecipientType.PATIENT))
                .thenReturn(List.of(new Notification()));

        // Act
        List<NotificationResponseDTO> results = notificationService.getNotificationsByRecipientType(RecipientType.PATIENT);

        // Assert
        assertFalse(results.isEmpty());
        verify(notificationRepository).findByRecipientType(RecipientType.PATIENT);
    }

    @Test
    void getPendingNotifications_ReturnsOnlyPending() {
        // Arrange
        when(notificationRepository.findByNotificationStatus(NotificationStatus.PENDING))
                .thenReturn(List.of(new Notification()));

        // Act
        List<NotificationResponseDTO> results = notificationService.getPendingNotifications();

        // Assert
        assertNotNull(results);
        verify(notificationRepository).findByNotificationStatus(NotificationStatus.PENDING);
    }
}