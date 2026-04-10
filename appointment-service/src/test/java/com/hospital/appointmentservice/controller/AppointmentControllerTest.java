package com.hospital.appointmentservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.appointmentservice.dto.*;
import com.hospital.appointmentservice.model.Appointment;
import com.hospital.appointmentservice.model.AppointmentStatus;
import com.hospital.appointmentservice.service.AppointmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AppointmentController using JUnit 5 and Mockito
 * 
 * Test Strategy:
 * - Use @WebMvcTest to load only the web layer (AppointmentController)
 * - Mock the AppointmentService dependency using @MockBean
 * - Use MockMvc to perform HTTP requests and verify responses
 * - Use ObjectMapper to serialize request DTOs to JSON
 * - Follow Given/When/Then pattern for test clarity
 */
@WebMvcTest(AppointmentController.class)
@ActiveProfiles("test")
@DisplayName("AppointmentController Tests")
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AppointmentService appointmentService;

    private GetAvailableDoctorsRequest getAvailableDoctorsRequest;
    private GetTimeSlotsRequest getTimeSlotsRequest;
    private BookAppointmentRequest bookAppointmentRequest;
    private RescheduleAppointmentRequest rescheduleAppointmentRequest;
    private Appointment mockAppointment;

    @BeforeEach
    void setUp() {
        // Given: Set up test data for all test cases
        getAvailableDoctorsRequest = new GetAvailableDoctorsRequest(
                101L,
                "Cardiology"
        );

        getTimeSlotsRequest = new GetTimeSlotsRequest(
                101L,
                201L,
                LocalDate.of(2030, 12, 15)
        );

        bookAppointmentRequest = new BookAppointmentRequest(
                101L,
                201L,
                LocalDateTime.of(2030, 12, 15, 10, 0)
        );

        rescheduleAppointmentRequest = new RescheduleAppointmentRequest(
                "550e8400-e29b-41d4-a716-446655440000",
                LocalDateTime.of(2030, 12, 20, 14, 0)
        );

        mockAppointment = new Appointment();
        mockAppointment.setAppointmentId(1000L);
        mockAppointment.setPatientId(101L);
        mockAppointment.setDoctorId(201L);
        mockAppointment.setConfirmationCode("550e8400-e29b-41d4-a716-446655440000");
        mockAppointment.setAppointmentDate(LocalDateTime.of(2030, 12, 15, 10, 0));
        mockAppointment.setStatus(AppointmentStatus.CONFIRMED);
    }

    // ==================== GET AVAILABLE DOCTORS TESTS ====================

    @Test
    @DisplayName("POST /available-doctors - Success with valid request")
    void testGetAvailableDoctors_Success() throws Exception {
        // Given: A valid request and mock response
        List<DoctorAvailabilityDTO> mockDoctors = Arrays.asList(
                new DoctorAvailabilityDTO(201L, "Dr. Smith", "Cardiology"),
                new DoctorAvailabilityDTO(202L, "Dr. Johnson", "Cardiology")
        );
        when(appointmentService.getAvailableDoctorsBySpecialization(any(GetAvailableDoctorsRequest.class)))
                .thenReturn(mockDoctors);

        String requestJson = objectMapper.writeValueAsString(getAvailableDoctorsRequest);

        // When: POST request is made to /available-doctors
        mockMvc.perform(post("/api/v1/appointments/available-doctors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                // Then: Verify 200 OK response with correct data
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].doctorId").value(201L))
                .andExpect(jsonPath("$[0].doctorName").value("Dr. Smith"))
                .andExpect(jsonPath("$[0].specialization").value("Cardiology"))
                .andExpect(jsonPath("$[1].doctorId").value(202L));

        // Verify service method was called once with correct parameters
        verify(appointmentService, times(1)).getAvailableDoctorsBySpecialization(any(GetAvailableDoctorsRequest.class));
    }

    @Test
    @DisplayName("POST /available-doctors - Returns empty list when no doctors available")
    void testGetAvailableDoctors_EmptyList() throws Exception {
        // Given: A valid request but no doctors available
        when(appointmentService.getAvailableDoctorsBySpecialization(any(GetAvailableDoctorsRequest.class)))
                .thenReturn(List.of());

        String requestJson = objectMapper.writeValueAsString(getAvailableDoctorsRequest);

        // When: POST request is made to /available-doctors
        mockMvc.perform(post("/api/v1/appointments/available-doctors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                // Then: Verify 200 OK response with empty array
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(appointmentService, times(1)).getAvailableDoctorsBySpecialization(any(GetAvailableDoctorsRequest.class));
    }

    @Test
    @DisplayName("POST /available-doctors - Returns 400 Bad Request when patientId is null")
    void testGetAvailableDoctors_InvalidRequest_NullPatientId() throws Exception {
        // Given: A request with null patientId
        GetAvailableDoctorsRequest invalidRequest = new GetAvailableDoctorsRequest(null, "Cardiology");
        String requestJson = objectMapper.writeValueAsString(invalidRequest);

        // When: POST request is made with invalid data
        mockMvc.perform(post("/api/v1/appointments/available-doctors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                // Then: Verify 400 Bad Request due to validation failure
                .andExpect(status().isBadRequest());

        // Service method should not be called
        verify(appointmentService, never()).getAvailableDoctorsBySpecialization(any());
    }

    @Test
    @DisplayName("POST /available-doctors - Returns 400 Bad Request when specialization is blank")
    void testGetAvailableDoctors_InvalidRequest_BlankSpecialization() throws Exception {
        // Given: A request with blank specialization
        GetAvailableDoctorsRequest invalidRequest = new GetAvailableDoctorsRequest(101L, "");
        String requestJson = objectMapper.writeValueAsString(invalidRequest);

        // When: POST request is made with invalid data
        mockMvc.perform(post("/api/v1/appointments/available-doctors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                // Then: Verify 400 Bad Request due to validation failure
                .andExpect(status().isBadRequest());

        verify(appointmentService, never()).getAvailableDoctorsBySpecialization(any());
    }

    // ==================== GET TIME SLOTS TESTS ====================

    @Test
    @DisplayName("POST /available-slots - Success with valid request")
    void testGetAvailableSlots_Success() throws Exception {
        // Given: A valid request and mock response with time slots
        List<TimeSlotDTO> mockSlots = Arrays.asList(
                new TimeSlotDTO(LocalDateTime.of(2030, 12, 15, 9, 0), false),
                new TimeSlotDTO(LocalDateTime.of(2030, 12, 15, 10, 0), false),
                new TimeSlotDTO(LocalDateTime.of(2030, 12, 15, 11, 0), true)
        );
        when(appointmentService.getTimeSlotsForDoctor(any(GetTimeSlotsRequest.class)))
                .thenReturn(mockSlots);

        String requestJson = objectMapper.writeValueAsString(getTimeSlotsRequest);

        // When: POST request is made to /available-slots
        mockMvc.perform(post("/api/v1/appointments/available-slots")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                // Then: Verify 200 OK response with correct time slot data
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].startTime").exists())
                .andExpect(jsonPath("$[0].booked").value(false))
                .andExpect(jsonPath("$[2].booked").value(true));

        verify(appointmentService, times(1)).getTimeSlotsForDoctor(any(GetTimeSlotsRequest.class));
    }

    @Test
    @DisplayName("POST /available-slots - Returns empty list when no slots available")
    void testGetAvailableSlots_EmptyList() throws Exception {
        // Given: A valid request but no time slots available
        when(appointmentService.getTimeSlotsForDoctor(any(GetTimeSlotsRequest.class)))
                .thenReturn(List.of());

        String requestJson = objectMapper.writeValueAsString(getTimeSlotsRequest);

        // When: POST request is made to /available-slots
        mockMvc.perform(post("/api/v1/appointments/available-slots")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                // Then: Verify 200 OK response with empty array
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(appointmentService, times(1)).getTimeSlotsForDoctor(any(GetTimeSlotsRequest.class));
    }

    @Test
    @DisplayName("POST /available-slots - Returns 400 Bad Request when patientId is null")
    void testGetAvailableSlots_InvalidRequest_NullPatientId() throws Exception {
        // Given: A request with null patientId
        GetTimeSlotsRequest invalidRequest = new GetTimeSlotsRequest(null, 201L, LocalDate.of(2026, 4, 15));
        String requestJson = objectMapper.writeValueAsString(invalidRequest);

        // When: POST request is made with invalid data
        mockMvc.perform(post("/api/v1/appointments/available-slots")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                // Then: Verify 400 Bad Request due to validation failure
                .andExpect(status().isBadRequest());

        verify(appointmentService, never()).getTimeSlotsForDoctor(any());
    }

    @Test
    @DisplayName("POST /available-slots - Returns 400 Bad Request when doctorId is null")
    void testGetAvailableSlots_InvalidRequest_NullDoctorId() throws Exception {
        // Given: A request with null doctorId
        GetTimeSlotsRequest invalidRequest = new GetTimeSlotsRequest(101L, null, LocalDate.of(2026, 4, 15));
        String requestJson = objectMapper.writeValueAsString(invalidRequest);

        // When: POST request is made with invalid data
        mockMvc.perform(post("/api/v1/appointments/available-slots")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                // Then: Verify 400 Bad Request due to validation failure
                .andExpect(status().isBadRequest());

        verify(appointmentService, never()).getTimeSlotsForDoctor(any());
    }

    // ==================== BOOK APPOINTMENT TESTS ====================

    @Test
    @DisplayName("POST /booking - Success with valid request")
    void testBookAppointment_Success() throws Exception {
        // Given: A valid booking request and mock appointment response
        when(appointmentService.bookAppointment(any(BookAppointmentRequest.class)))
                .thenReturn(mockAppointment);

        String requestJson = objectMapper.writeValueAsString(bookAppointmentRequest);

        // When: POST request is made to /booking
        mockMvc.perform(post("/api/v1/appointments/booking")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                // Then: Verify 200 OK response with appointment details
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointmentId").value(1000L))
                .andExpect(jsonPath("$.patientId").value(101L))
                .andExpect(jsonPath("$.doctorId").value(201L))
                .andExpect(jsonPath("$.confirmationCode").value("550e8400-e29b-41d4-a716-446655440000"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(appointmentService, times(1)).bookAppointment(any(BookAppointmentRequest.class));
    }

    @Test
    @DisplayName("POST /booking - Returns 400 Bad Request when patientId is null")
    void testBookAppointment_InvalidRequest_NullPatientId() throws Exception {
        // Given: A booking request with null patientId
        BookAppointmentRequest invalidRequest = new BookAppointmentRequest(
                null,
                201L,
                LocalDateTime.of(2026, 4, 15, 10, 0)
        );
        String requestJson = objectMapper.writeValueAsString(invalidRequest);

        // When: POST request is made with invalid data
        mockMvc.perform(post("/api/v1/appointments/booking")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                // Then: Verify 400 Bad Request due to validation failure
                .andExpect(status().isBadRequest());

        verify(appointmentService, never()).bookAppointment(any());
    }

    @Test
    @DisplayName("POST /booking - Returns 400 Bad Request when doctorId is null")
    void testBookAppointment_InvalidRequest_NullDoctorId() throws Exception {
        // Given: A booking request with null doctorId
        BookAppointmentRequest invalidRequest = new BookAppointmentRequest(
                101L,
                null,
                LocalDateTime.of(2026, 4, 15, 10, 0)
        );
        String requestJson = objectMapper.writeValueAsString(invalidRequest);

        // When: POST request is made with invalid data
        mockMvc.perform(post("/api/v1/appointments/booking")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                // Then: Verify 400 Bad Request due to validation failure
                .andExpect(status().isBadRequest());

        verify(appointmentService, never()).bookAppointment(any());
    }

    @Test
    @DisplayName("POST /booking - Returns 400 Bad Request when startTime is null")
    void testBookAppointment_InvalidRequest_NullStartTime() throws Exception {
        // Given: A booking request with null startTime
        BookAppointmentRequest invalidRequest = new BookAppointmentRequest(101L, 201L, null);
        String requestJson = objectMapper.writeValueAsString(invalidRequest);

        // When: POST request is made with invalid data
        mockMvc.perform(post("/api/v1/appointments/booking")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                // Then: Verify 400 Bad Request due to validation failure
                .andExpect(status().isBadRequest());

        verify(appointmentService, never()).bookAppointment(any());
    }

    @Test
    @DisplayName("POST /booking - Handles service exception gracefully")
    void testBookAppointment_ServiceException() throws Exception {
        // Given: A valid request but service throws exception
        when(appointmentService.bookAppointment(any(BookAppointmentRequest.class)))
                .thenThrow(new RuntimeException("Failed to book appointment"));

        String requestJson = objectMapper.writeValueAsString(bookAppointmentRequest);

        // When: POST request is made and service fails
        mockMvc.perform(post("/api/v1/appointments/booking")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                // Then: Verify 500 Internal Server Error
                .andExpect(status().isInternalServerError());

        verify(appointmentService, times(1)).bookAppointment(any(BookAppointmentRequest.class));
    }

    // ==================== CANCEL APPOINTMENT TESTS ====================

    @Test
    @DisplayName("PUT /cancel/{confirmationCode} - Success with valid confirmation code")
    void testCancelAppointment_Success() throws Exception {
        // Given: A valid confirmation code and mock cancelled appointment
        Appointment cancelledAppointment = new Appointment();
        cancelledAppointment.setAppointmentId(1000L);
        cancelledAppointment.setConfirmationCode("550e8400-e29b-41d4-a716-446655440000");
        cancelledAppointment.setStatus(AppointmentStatus.CANCELLED);
        cancelledAppointment.setPatientId(101L);
        cancelledAppointment.setDoctorId(201L);

        when(appointmentService.cancelAppointment("550e8400-e29b-41d4-a716-446655440000"))
                .thenReturn(cancelledAppointment);

        // When: PUT request is made to /cancel/{confirmationCode}
        mockMvc.perform(put("/api/v1/appointments/cancel/550e8400-e29b-41d4-a716-446655440000"))
                // Then: Verify 200 OK response with cancelled appointment
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmationCode").value("550e8400-e29b-41d4-a716-446655440000"))
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.appointmentId").value(1000L));

        verify(appointmentService, times(1)).cancelAppointment("550e8400-e29b-41d4-a716-446655440000");
    }

    @Test
    @DisplayName("PUT /cancel/{confirmationCode} - Returns 500 when service fails")
    void testCancelAppointment_ServiceException() throws Exception {
        // Given: A valid confirmation code but service throws exception
        when(appointmentService.cancelAppointment(anyString()))
                .thenThrow(new RuntimeException("Appointment not found"));

        // When: PUT request is made and service fails
        mockMvc.perform(put("/api/v1/appointments/cancel/invalid-code"))
                // Then: Verify 500 Internal Server Error
                .andExpect(status().isInternalServerError());

        verify(appointmentService, times(1)).cancelAppointment("invalid-code");
    }

    // ==================== RESCHEDULE APPOINTMENT TESTS ====================

    @Test
    @DisplayName("PUT /reschedule - Success with valid request")
    void testRescheduleAppointment_Success() throws Exception {
        // Given: A valid reschedule request and mock rescheduled appointment
        Appointment rescheduledAppointment = new Appointment();
        rescheduledAppointment.setAppointmentId(1000L);
        rescheduledAppointment.setConfirmationCode("550e8400-e29b-41d4-a716-446655440000");
        rescheduledAppointment.setPatientId(101L);
        rescheduledAppointment.setDoctorId(201L);
        rescheduledAppointment.setAppointmentDate(LocalDateTime.of(2026, 4, 20, 14, 0));
        rescheduledAppointment.setStatus(AppointmentStatus.CONFIRMED);

        when(appointmentService.rescheduleAppointment(any(RescheduleAppointmentRequest.class)))
                .thenReturn(rescheduledAppointment);

        String requestJson = objectMapper.writeValueAsString(rescheduleAppointmentRequest);

        // When: PUT request is made to /reschedule
        mockMvc.perform(put("/api/v1/appointments/reschedule")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                // Then: Verify 200 OK response with rescheduled appointment
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmationCode").value("550e8400-e29b-41d4-a716-446655440000"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.appointmentId").value(1000L));

        verify(appointmentService, times(1)).rescheduleAppointment(any(RescheduleAppointmentRequest.class));
    }

    @Test
    @DisplayName("PUT /reschedule - Returns 400 Bad Request when confirmationCode is null")
    void testRescheduleAppointment_InvalidRequest_NullConfirmationCode() throws Exception {
        // Given: A reschedule request with null confirmationCode
        RescheduleAppointmentRequest invalidRequest = new RescheduleAppointmentRequest(
                null,
                LocalDateTime.of(2026, 4, 20, 14, 0)
        );
        String requestJson = objectMapper.writeValueAsString(invalidRequest);

        // When: PUT request is made with invalid data
        mockMvc.perform(put("/api/v1/appointments/reschedule")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                // Then: Verify 400 Bad Request due to validation failure
                .andExpect(status().isBadRequest());

        verify(appointmentService, never()).rescheduleAppointment(any());
    }

    @Test
    @DisplayName("PUT /reschedule - Returns 400 Bad Request when newAppointmentTime is null")
    void testRescheduleAppointment_InvalidRequest_NullNewAppointmentTime() throws Exception {
        // Given: A reschedule request with null newAppointmentTime
        RescheduleAppointmentRequest invalidRequest = new RescheduleAppointmentRequest(
                "550e8400-e29b-41d4-a716-446655440000",
                null
        );
        String requestJson = objectMapper.writeValueAsString(invalidRequest);

        // When: PUT request is made with invalid data
        mockMvc.perform(put("/api/v1/appointments/reschedule")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                // Then: Verify 400 Bad Request due to validation failure
                .andExpect(status().isBadRequest());

        verify(appointmentService, never()).rescheduleAppointment(any());
    }

    @Test
    @DisplayName("PUT /reschedule - Handles service exception gracefully")
    void testRescheduleAppointment_ServiceException() throws Exception {
        // Given: A valid request but service throws exception
        when(appointmentService.rescheduleAppointment(any(RescheduleAppointmentRequest.class)))
                .thenThrow(new RuntimeException("Appointment cannot be rescheduled"));

        String requestJson = objectMapper.writeValueAsString(rescheduleAppointmentRequest);

        // When: PUT request is made and service fails
        mockMvc.perform(put("/api/v1/appointments/reschedule")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                // Then: Verify 500 Internal Server Error
                .andExpect(status().isInternalServerError());

        verify(appointmentService, times(1)).rescheduleAppointment(any(RescheduleAppointmentRequest.class));
    }
}
