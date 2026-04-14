package com.hospital.appointmentservice.service;

import com.hospital.appointmentservice.client.DoctorSheduleClient;
import com.hospital.appointmentservice.client.NotificationServiceClient;
import com.hospital.appointmentservice.dto.BookAppointmentRequest;
import com.hospital.appointmentservice.dto.RescheduleAppointmentRequest;
import com.hospital.appointmentservice.exception.BusinessValidationException; // <-- NEW IMPORT
import com.hospital.appointmentservice.model.Appointment;
import com.hospital.appointmentservice.model.AppointmentStatus;
import com.hospital.appointmentservice.repository.AppointmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AppointmentServiceTest {

    // --- Constants for Testing ---
    private static final Long PATIENT_ID = 101L;
    private static final Long DOCTOR_ID = 201L;
    private static final String CONFIRMATION_CODE = "550e8400-e29b-41d4-a716-446655440000";
    private static final LocalDateTime APPOINTMENT_TIME = LocalDateTime.of(2030, 12, 15, 10, 0);

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private DoctorSheduleClient doctorSheduleClient;

    @Mock
    private NotificationServiceClient notificationServiceClient;

    @InjectMocks
    private AppointmentService appointmentService;

    // --- Helper Methods ---
    private BookAppointmentRequest createBookingRequest() {
        return new BookAppointmentRequest(PATIENT_ID, DOCTOR_ID, APPOINTMENT_TIME);
    }

    private Appointment createValidAppointment() {
        Appointment appointment = new Appointment();
        appointment.setAppointmentId(1000L);
        appointment.setConfirmationCode(CONFIRMATION_CODE);
        appointment.setPatientId(PATIENT_ID);
        appointment.setDoctorId(DOCTOR_ID);
        appointment.setAppointmentDate(APPOINTMENT_TIME);
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        return appointment;
    }

    // --- Tests ---

    @Test
    @DisplayName("bookAppointment - Throws Exception when patient already has an appointment with the doctor at this time")
    void testBookAppointment_AlreadyBooked() {
        BookAppointmentRequest request = createBookingRequest();

        // UPDATED: Now testing the new method signature with Date
        when(appointmentRepository.existsByPatientIdAndDoctorIdAndAppointmentDate(request.patientId(), request.doctorId(), request.startTime()))
                .thenReturn(true);

        // UPDATED: Now expecting your custom BusinessValidationException
        BusinessValidationException exception = assertThrows(BusinessValidationException.class, () ->
                appointmentService.bookAppointment(request)
        );

        // UPDATED: Expected error message to match your updated service code
        assertEquals("You already have an appointment with this doctor at this specific time.", exception.getMessage());

        verify(doctorSheduleClient, never()).claimTimeSlot(anyLong(), anyLong(), any());
        verify(appointmentRepository, never()).save(any());
        verify(notificationServiceClient, never()).sendNotification(any());
    }

    @Test
    @DisplayName("bookAppointment - Handles Optimistic Locking Failure during concurrent booking")
    void testBookAppointment_OptimisticLockingFailure() {
        BookAppointmentRequest request = createBookingRequest();

        // UPDATED: Mocking the new repository method signature
        when(appointmentRepository.existsByPatientIdAndDoctorIdAndAppointmentDate(anyLong(), anyLong(), any(LocalDateTime.class)))
                .thenReturn(false);
        doNothing().when(doctorSheduleClient).claimTimeSlot(anyLong(), anyLong(), any());

        when(appointmentRepository.save(any(Appointment.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException(Appointment.class, "appointmentId"));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                appointmentService.bookAppointment(request)
        );

        assertEquals("Another user just booked this slot. Please try again.", exception.getMessage());

        verify(doctorSheduleClient, times(1)).claimTimeSlot(anyLong(), anyLong(), any());
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
        verify(notificationServiceClient, never()).sendNotification(any());
    }

    @Test
    @DisplayName("bookAppointment - Succeeds even if Notification Service fails")
    void testBookAppointment_NotificationFails() {
        BookAppointmentRequest request = createBookingRequest();
        Appointment savedAppointment = createValidAppointment();

        // UPDATED: Mocking the new repository method signature
        when(appointmentRepository.existsByPatientIdAndDoctorIdAndAppointmentDate(anyLong(), anyLong(), any(LocalDateTime.class)))
                .thenReturn(false);
        doNothing().when(doctorSheduleClient).claimTimeSlot(anyLong(), anyLong(), any());
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(savedAppointment);

        doThrow(new RuntimeException("Notification server is down!"))
                .when(notificationServiceClient).sendNotification(any());

        Appointment result = appointmentService.bookAppointment(request);

        assertEquals(CONFIRMATION_CODE, result.getConfirmationCode());

        verify(notificationServiceClient, times(1)).sendNotification(any());
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    @Test
    @DisplayName("cancelAppointment - Successfully cancels an appointment and sends notification")
    void testCancelAppointment_Success() {
        Appointment existingAppointment = createValidAppointment();

        when(appointmentRepository.findByConfirmationCode(CONFIRMATION_CODE))
                .thenReturn(Optional.of(existingAppointment));
        doNothing().when(doctorSheduleClient).cancelBooking(anyLong(), anyLong(), any(LocalDateTime.class));

        // Using Mockito's returnsFirstArg() for cleaner saving mocks
        when(appointmentRepository.save(any(Appointment.class))).then(returnsFirstArg());
        doNothing().when(notificationServiceClient).sendNotification(any());

        Appointment result = appointmentService.cancelAppointment(CONFIRMATION_CODE);

        assertEquals(AppointmentStatus.CANCELLED, result.getStatus());
        assertEquals(CONFIRMATION_CODE, result.getConfirmationCode());

        verify(appointmentRepository, times(1)).findByConfirmationCode(CONFIRMATION_CODE);
        verify(doctorSheduleClient, times(1)).cancelBooking(DOCTOR_ID, PATIENT_ID, APPOINTMENT_TIME);
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
        verify(notificationServiceClient, times(1)).sendNotification(any());
    }

    @Test
    @DisplayName("rescheduleAppointment - Successfully reschedules and sends notification")
    void testRescheduleAppointment_Success() {
        LocalDateTime newTime = APPOINTMENT_TIME.plusDays(5);
        RescheduleAppointmentRequest request = new RescheduleAppointmentRequest(CONFIRMATION_CODE, newTime);
        Appointment existingAppointment = createValidAppointment();

        when(appointmentRepository.findByConfirmationCode(CONFIRMATION_CODE))
                .thenReturn(Optional.of(existingAppointment));
        when(appointmentRepository.existsByDoctorIdAndAppointmentDate(DOCTOR_ID, newTime))
                .thenReturn(false);

        doNothing().when(doctorSheduleClient).cancelBooking(DOCTOR_ID, PATIENT_ID, APPOINTMENT_TIME);
        doNothing().when(doctorSheduleClient).claimTimeSlot(DOCTOR_ID, PATIENT_ID, newTime);
        when(appointmentRepository.save(any(Appointment.class))).then(returnsFirstArg());
        doNothing().when(notificationServiceClient).sendNotification(any());

        Appointment result = appointmentService.rescheduleAppointment(request);

        assertEquals(newTime, result.getAppointmentDate());
        assertEquals(AppointmentStatus.CONFIRMED, result.getStatus());

        verify(doctorSheduleClient, times(1)).cancelBooking(DOCTOR_ID, PATIENT_ID, APPOINTMENT_TIME);
        verify(doctorSheduleClient, times(1)).claimTimeSlot(DOCTOR_ID, PATIENT_ID, newTime);
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
        verify(notificationServiceClient, times(1)).sendNotification(any());
    }
}