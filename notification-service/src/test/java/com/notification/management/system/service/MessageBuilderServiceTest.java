package com.notification.management.system.service;

import com.notification.management.system.client.DoctorServiceClient;
import com.notification.management.system.client.PatientServiceClient;
import com.notification.management.system.dto.NotificationRequestDTO;
import com.notification.management.system.model.AppointmentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageBuilderServiceTest {

    @Mock
    private PatientServiceClient patientClient;
    @Mock
    private DoctorServiceClient doctorClient;

    @InjectMocks
    private MessageBuilderService messageBuilderService;

    private NotificationRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        requestDTO = new NotificationRequestDTO();
        requestDTO.setPatientId(1L);
        requestDTO.setDoctorId(10L);
        requestDTO.setConfirmationCode("100");
        // Assuming getConfirmationCode() exists in your DTO based on your code logic
        // requestDTO.setConfirmationCode("CONF-123");
    }

    @Test
    void buildPatientMessage_ConfirmedStatus() {
        requestDTO.setAppointmentStatus(AppointmentStatus.CONFIRMED);
        // Mocking a successful patient fetch
        var mockPatient = new Object() { public String getName() { return "John Doe"; } };
        // Note: Replace Object with your actual Patient Response DTO class
        // when(patientClient.getPatientById(1L)).thenReturn(mockPatient);

        String message = messageBuilderService.buildPatientMessage(requestDTO);

        assertTrue(message.contains("confirmed"));
        assertNotNull(message);
    }

    @Test
    void buildDoctorMessage_Success() {
        requestDTO.setAppointmentStatus(AppointmentStatus.CONFIRMED);
        String message = messageBuilderService.buildDoctorMessage(requestDTO);

        assertTrue(message.contains("Dr."));
        assertTrue(message.contains("status update"));
    }
}