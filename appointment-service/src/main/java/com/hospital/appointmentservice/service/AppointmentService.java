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
                notification.setConfirmationCode(savedAppointment.getConfirmationCode());
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
     * Cancel an appointment by confirmation code (not by internal appointmentId)
     * This is a security best practice - we don't expose internal database IDs
     */
    @Transactional
    public Appointment cancelAppointment(String confirmationCode) {
        // 1. Find the appointment by confirmation code (external identifier)
        Appointment appointment = appointmentRepository.findByConfirmationCode(confirmationCode)
                .orElseThrow(() -> new RuntimeException("Appointment not found with confirmation code: " + confirmationCode));

        // 2. Check if already cancelled
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new RuntimeException("Appointment is already cancelled.");
        }

        // 3. REMOTE CALL - Cancel the booking in Doctor Schedule Service
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
                notification.setConfirmationCode(cancelledAppointment.getConfirmationCode());
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

    /**
     * Reschedule an existing appointment to a new time slot
     */
    @Transactional
    public Appointment rescheduleAppointment(RescheduleAppointmentRequest request) {
        // 1. Find the existing appointment by confirmation code
        Appointment existingAppointment = appointmentRepository.findByConfirmationCode(request.confirmationCode())
                .orElseThrow(() -> new RuntimeException("Appointment not found with confirmation code: " + request.confirmationCode()));

        // 2. Check if appointment is still valid (not cancelled or completed)
        if (existingAppointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new RuntimeException("Cannot reschedule a cancelled appointment.");
        }

        if (existingAppointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new RuntimeException("Cannot reschedule a completed appointment.");
        }

        // 3. Check if new time slot is not already booked by this doctor
        if (appointmentRepository.existsByDoctorIdAndAppointmentDate(
                existingAppointment.getDoctorId(), request.newAppointmentTime())) {
            throw new RuntimeException("Doctor is not available at the requested time.");
        }

        // 4. REMOTE CALL - Release the old slot in Doctor Schedule Service
        try {
            doctorSheduleClient.cancelBooking(existingAppointment.getDoctorId(), 
                    existingAppointment.getPatientId(), 
                    existingAppointment.getAppointmentDate());
        } catch (Exception e) {
            throw new RuntimeException("Unable to release old time slot: " + e.getMessage());
        }

        // 5. REMOTE CALL - Claim the new time slot in Doctor Schedule Service
        try {
            doctorSheduleClient.claimTimeSlot(existingAppointment.getDoctorId(), 
                    existingAppointment.getPatientId(), 
                    request.newAppointmentTime());
        } catch (Exception e) {
            throw new RuntimeException("Unable to claim new time slot: " + e.getMessage());
        }

        // 6. Update the appointment with new time
        existingAppointment.setAppointmentDate(request.newAppointmentTime());
        existingAppointment.setStatus(AppointmentStatus.CONFIRMED); // Reconfirm after rescheduling

        try {
            Appointment rescheduledAppointment = appointmentRepository.save(existingAppointment);

            // 7. SEND NOTIFICATION - Notify about appointment rescheduling
            try {
                NotificationRequest notification = new NotificationRequest();
                notification.setPatientId(rescheduledAppointment.getPatientId());
                notification.setDoctorId(rescheduledAppointment.getDoctorId());
                notification.setStatus("RESCHEDULED");
                notification.setConfirmationCode(rescheduledAppointment.getConfirmationCode());
                notification.setTime(rescheduledAppointment.getAppointmentDate().toLocalTime());
                notification.setDate(rescheduledAppointment.getAppointmentDate().toLocalDate());

                notificationServiceClient.sendNotification(notification);
            } catch (Exception e) {
                // Log notification failure but don't fail the rescheduling
                System.err.println("Failed to send rescheduling notification: " + e.getMessage());
            }

            return rescheduledAppointment;
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new RuntimeException("Concurrent rescheduling detected. Please try again.");
        }
    }
}