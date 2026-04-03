package com.hospital.appointmentservice.service;

import com.hospital.appointmentservice.client.DoctorServiceClient;
import com.hospital.appointmentservice.client.DoctorSheduleClient;
import com.hospital.appointmentservice.client.NotificationServiceClient;
import com.hospital.appointmentservice.dto.*;
import com.hospital.appointmentservice.model.Appointment;
import com.hospital.appointmentservice.model.AppointmentStatus;
import com.hospital.appointmentservice.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorServiceClient doctorServiceClient;
    private final NotificationServiceClient notificationServiceClient;
    private final DoctorSheduleClient doctorSheduleClient;

    /**
     * Step 1: Patient provides specialization, fetch available doctors with their slots
     */
    public List<DoctorAvailabilityDTO> getAvailableDoctorsBySpecialization(GetAvailableDoctorsRequest request) {
        try {
            return doctorServiceClient.getAvailableDoctorsBySpecialization(request.specialization());
        } catch (Exception e) {
            throw new RuntimeException("Unable to fetch available doctors: " + e.getMessage());
        }
    }

    /**
     * Step 2: Patient selects a doctor, fetch available time slots for that doctor
     */
    public List<TimeSlotDTO> getTimeSlotsForDoctor(GetTimeSlotsRequest request) {
        try {
            return doctorSheduleClient.getTimeSlotsByDoctorId(request.doctorId(),request.date());
        } catch (Exception e) {
            throw new RuntimeException("Unable to fetch time slots for doctor: " + e.getMessage());
        }
    }

    /**
     * Step 3: Patient selects a time slot, book the appointment
     */
    @Transactional
    public Appointment bookAppointment(BookAppointmentRequest request) {
        // 1. Check if appointment already exists
        if (appointmentRepository.existsByPatientIdAndDoctorId(request.patientId(), request.doctorId())) {
            throw new RuntimeException("Patient already has an appointment with this doctor.");
        }

        // 2. REMOTE CALL - Try to claim the time slot in Doctor Schedule Service
        try {
            doctorSheduleClient.claimTimeSlot(request.doctorId(), request.patientId(), request.startTime());
        } catch (Exception e) {
            throw new RuntimeException("Unable to book time slot: " + e.getMessage());
        }

        // 3. PERSIST - Finalize the booking locally
        Appointment appointment = new Appointment();
        appointment.setPatientId(request.patientId());
        appointment.setDoctorId(request.doctorId());
        appointment.setAppointmentDate(request.startTime());
        appointment.setStatus(AppointmentStatus.CONFIRMED);

        try {
            Appointment savedAppointment = appointmentRepository.save(appointment);

            // 4. SEND NOTIFICATION - Notify about new appointment booking
            try {
                NotificationRequest notification = new NotificationRequest();
                notification.setPatientId(savedAppointment.getPatientId());
                notification.setDoctorId(savedAppointment.getDoctorId());
                notification.setStatus(savedAppointment.getStatus().toString());
                notification.setAppointmentId(savedAppointment.getAppointmentId());
                notification.setTime(savedAppointment.getAppointmentDate().toLocalTime());
                notification.setDate(savedAppointment.getAppointmentDate().toLocalDate());

                notificationServiceClient.sendNotification(notification);
            } catch (Exception e) {
                // Log notification failure but don't fail the appointment booking
                System.err.println("Failed to send booking notification: " + e.getMessage());
            }

            return savedAppointment;
        } catch (ObjectOptimisticLockingFailureException e) {
            // This is where your @Version comes into play!
            throw new RuntimeException("Another user just booked this slot. Please try again.");
        }
    }

    /**
     * Cancel an appointment by appointmentId
     */
    @Transactional
    public Appointment cancelAppointment(Long appointmentId) {
        // 1. Find the appointment
        Appointment appointment = appointmentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found with ID: " + appointmentId));

        // 2. Check if already cancelled
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new RuntimeException("Appointment is already cancelled.");
        }

        // 3. REMOTE CALL - Cancel the booking in Doctor Schedule Service using slotId
        try {
            doctorSheduleClient.cancelBooking(appointment.getDoctorId(), appointment.getPatientId(), appointment.getAppointmentDate());
        } catch (Exception e) {
            throw new RuntimeException("Unable to cancel booking in doctor service: " + e.getMessage());
        }

        // 4. Update appointment status locally
        appointment.setStatus(AppointmentStatus.CANCELLED);

        try {
            Appointment cancelledAppointment = appointmentRepository.save(appointment);

            // 5. SEND NOTIFICATION - Notify about appointment cancellation
            try {
                NotificationRequest notification = new NotificationRequest();
                notification.setPatientId(cancelledAppointment.getPatientId());
                notification.setDoctorId(cancelledAppointment.getDoctorId());
                notification.setStatus(cancelledAppointment.getStatus().toString());
                notification.setTime(cancelledAppointment.getAppointmentDate().toLocalTime());
                notification.setDate(cancelledAppointment.getAppointmentDate().toLocalDate());

                notificationServiceClient.sendNotification(notification);
            } catch (Exception e) {
                // Log notification failure but don't fail the appointment cancellation
                System.err.println("Failed to send cancellation notification: " + e.getMessage());
            }

            return cancelledAppointment;
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new RuntimeException("Concurrent cancellation detected. Please try again.");
        }
    }
}