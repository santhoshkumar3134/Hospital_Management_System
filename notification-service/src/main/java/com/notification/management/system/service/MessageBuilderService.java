package com.notification.management.system.service;

import com.notification.management.system.client.DoctorServiceClient;
import com.notification.management.system.client.PatientServiceClient;
import com.notification.management.system.dto.NotificationRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageBuilderService {
    private final PatientServiceClient patientClient;
    private final DoctorServiceClient doctorClient;

    public String buildPatientMessage(NotificationRequestDTO dto) {
        String name = fetchPatientName(dto.getPatientId());
        return switch (dto.getAppointmentStatus()) {
            case CONFIRMED -> "Dear " + name + ", your appointment is confirmed and the appointment ID is:  " + dto.getConfirmationCode() + ".";
            case CANCELLED -> "Dear " + name + ", appointment " + dto.getConfirmationCode() + " was cancelled.";
            case COMPLETED -> "Dear " + name + ", thank you for your visit (ID: " + dto.getConfirmationCode() + ").";
        };
    }

    public String buildDoctorMessage(NotificationRequestDTO dto) {
        String dName = fetchDoctorName(dto.getDoctorId());
        return "Hello Dr. " + dName + ", there is a status update (" + dto.getAppointmentStatus() + ") for Appointment " + dto.getConfirmationCode();
    }

    private String fetchPatientName(Long id) {
        try { return patientClient.getPatientById(id).getName(); } catch (Exception e) { return "Patient"; }
    }
    private String fetchDoctorName(Long id) {
        try { return doctorClient.getDoctorById(id).getName(); } catch (Exception e) { return "Doctor"; }
    }
}