package com.hospital.appointmentservice.service;

import com.hospital.appointmentservice.client.DoctorServiceClient;
import com.hospital.appointmentservice.client.DoctorSheduleClient;
import com.hospital.appointmentservice.client.NotificationServiceClient;
import com.hospital.appointmentservice.dto.*;
import com.hospital.appointmentservice.model.Appointment;
import com.hospital.appointmentservice.model.AppointmentStatus;
import com.hospital.appointmentservice.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentService.class);

    private final AppointmentRepository appointmentRepository;
    private final DoctorServiceClient doctorServiceClient;
    private final NotificationServiceClient notificationServiceClient;
    private final DoctorSheduleClient doctorSheduleClient;

    /**
     * Step 1: Patient provides specialization, fetch available doctors with their slots
     */
    public List<DoctorAvailabilityDTO> getAvailableDoctorsBySpecialization(GetAvailableDoctorsRequest request) {
        logger.info("Fetching available doctors for specialization: {}", request.specialization());
        try {
            List<DoctorAvailabilityDTO> doctors = doctorServiceClient.getAvailableDoctorsBySpecialization(request.specialization());
            logger.info("Successfully fetched {} doctors for specialization: {}", doctors.size(), request.specialization());
            return doctors;
        } catch (Exception e) {
            logger.error("Error fetching available doctors for specialization: {}", request.specialization(), e);
            throw new RuntimeException("Unable to fetch available doctors: " + e.getMessage());
        }
    }

    /**
     * Step 2: Patient selects a doctor, fetch available time slots for that doctor
     */
    public List<TimeSlotDTO> getTimeSlotsForDoctor(GetTimeSlotsRequest request) {
        logger.info("Fetching available time slots for doctorId: {}, date: {}", request.doctorId(), request.date());
        try {
            List<TimeSlotDTO> slots = doctorSheduleClient.getTimeSlotsByDoctorId(request.doctorId(), request.date());
            logger.info("Successfully fetched {} time slots for doctorId: {}", slots.size(), request.doctorId());
            return slots;
        } catch (Exception e) {
            logger.error("Error fetching time slots for doctorId: {}", request.doctorId(), e);
            throw new RuntimeException("Unable to fetch time slots for doctor: " + e.getMessage());
        }
    }

    /**
     * Step 3: Patient selects a time slot, book the appointment
     */
    @Transactional
    public Appointment bookAppointment(BookAppointmentRequest request) {
        logger.info("Booking appointment for patientId: {}, doctorId: {}, startTime: {}", 
                request.patientId(), request.doctorId(), request.startTime());
        
        // 1. Check if appointment already exists
        if (appointmentRepository.existsByPatientIdAndDoctorId(request.patientId(), request.doctorId())) {
            logger.warn("Patient {} already has an appointment with doctor {}", request.patientId(), request.doctorId());
            throw new RuntimeException("You already have an appointment with this doctor.");
        }

        // 2. REMOTE CALL - Try to claim the time slot in Doctor Schedule Service
        try {
            logger.debug("Claiming time slot from DoctorSchedule service");
            doctorSheduleClient.claimTimeSlot(request.doctorId(), request.patientId(), request.startTime());
            logger.debug("Time slot successfully claimed");
        } catch (Exception e) {
            logger.error("Failed to claim time slot in DoctorSchedule service for patientId: {}, doctorId: {}", 
                    request.patientId(), request.doctorId(), e);
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
            logger.info("Appointment successfully created with confirmationCode: {} for patientId: {}, doctorId: {}", 
                    savedAppointment.getConfirmationCode(), savedAppointment.getPatientId(), savedAppointment.getDoctorId());

            // 4. SEND NOTIFICATION - Notify about new appointment booking
            try {
                logger.debug("Sending booking notification for confirmationCode: {}", savedAppointment.getConfirmationCode());
                NotificationRequest notification = new NotificationRequest();
                notification.setPatientId(savedAppointment.getPatientId());
                notification.setDoctorId(savedAppointment.getDoctorId());
                notification.setStatus(savedAppointment.getStatus().toString());
                notification.setConfirmationCode(savedAppointment.getConfirmationCode());
                notification.setTime(savedAppointment.getAppointmentDate().toLocalTime());
                notification.setDate(savedAppointment.getAppointmentDate().toLocalDate());

                notificationServiceClient.sendNotification(notification);
                logger.info("Booking notification successfully sent for confirmationCode: {}", savedAppointment.getConfirmationCode());
            } catch (Exception e) {
                logger.warn("Failed to send booking notification: {}", e.getMessage());
            }

            return savedAppointment;
        } catch (ObjectOptimisticLockingFailureException e) {
            logger.error("Optimistic locking conflict: Another user booked this slot for patientId: {}, doctorId: {}", 
                    request.patientId(), request.doctorId(), e);
            throw new RuntimeException("Another user just booked this slot. Please try again.");
        }
    }

    /**
     * Cancel an appointment by confirmation code (not by internal appointmentId)
     * This is a security best practice - we don't expose internal database IDs
     */
    @Transactional
    public Appointment cancelAppointment(String confirmationCode) {
        logger.info("Cancelling appointment with confirmationCode: {}", confirmationCode);
        
        // 1. Find the appointment by confirmation code (external identifier)
        Appointment appointment = appointmentRepository.findByConfirmationCode(confirmationCode)
                .orElseThrow(() -> {
                    logger.error("Appointment not found with confirmationCode: {}", confirmationCode);
                    return new RuntimeException("Appointment not found with confirmation code: " + confirmationCode);
                });

        // 2. Check if already cancelled
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            logger.warn("Appointment {} is already cancelled", confirmationCode);
            throw new RuntimeException("Appointment is already cancelled.");
        }

        // 3. REMOTE CALL - Cancel the booking in Doctor Schedule Service
        try {
            logger.debug("Cancelling booking in DoctorSchedule service");
            doctorSheduleClient.cancelBooking(appointment.getDoctorId(), appointment.getPatientId(), appointment.getAppointmentDate());
            logger.debug("Booking successfully cancelled in DoctorSchedule service");
        } catch (Exception e) {
            logger.error("Failed to cancel booking in DoctorSchedule service for confirmationCode: {}", confirmationCode, e);
            throw new RuntimeException("Unable to cancel booking in doctor service: " + e.getMessage());
        }

        // 4. Update appointment status locally
        appointment.setStatus(AppointmentStatus.CANCELLED);

        try {
            Appointment cancelledAppointment = appointmentRepository.save(appointment);
            logger.info("Appointment successfully cancelled with confirmationCode: {}", confirmationCode);

            // 5. SEND NOTIFICATION - Notify about appointment cancellation
            try {
                logger.debug("Sending cancellation notification for confirmationCode: {}", confirmationCode);
                NotificationRequest notification = new NotificationRequest();
                notification.setPatientId(cancelledAppointment.getPatientId());
                notification.setDoctorId(cancelledAppointment.getDoctorId());
                notification.setStatus(cancelledAppointment.getStatus().toString());
                notification.setConfirmationCode(cancelledAppointment.getConfirmationCode());
                notification.setTime(cancelledAppointment.getAppointmentDate().toLocalTime());
                notification.setDate(cancelledAppointment.getAppointmentDate().toLocalDate());

                notificationServiceClient.sendNotification(notification);
                logger.info("Cancellation notification successfully sent for confirmationCode: {}", confirmationCode);
            } catch (Exception e) {
                logger.warn("Failed to send cancellation notification for confirmationCode: {}: {}", confirmationCode, e.getMessage());
            }

            return cancelledAppointment;
        } catch (ObjectOptimisticLockingFailureException e) {
            logger.error("Optimistic locking conflict during cancellation for confirmationCode: {}", confirmationCode, e);
            throw new RuntimeException("Concurrent cancellation detected. Please try again.");
        }
    }

    /**
     * Reschedule an existing appointment to a new time slot
     */
    @Transactional
    public Appointment rescheduleAppointment(RescheduleAppointmentRequest request) {
        logger.info("Rescheduling appointment with confirmationCode: {} to newTime: {}", 
                request.confirmationCode(), request.newAppointmentTime());
        
        // 1. Find the existing appointment by confirmation code
        Appointment existingAppointment = appointmentRepository.findByConfirmationCode(request.confirmationCode())
                .orElseThrow(() -> {
                    logger.error("Appointment not found for rescheduling with confirmationCode: {}", request.confirmationCode());
                    return new RuntimeException("Appointment not found with confirmation code: " + request.confirmationCode());
                });

        // 2. Check if appointment is still valid (not cancelled or completed)
        if (existingAppointment.getStatus() == AppointmentStatus.CANCELLED) {
            logger.warn("Cannot reschedule cancelled appointment: {}", request.confirmationCode());
            throw new RuntimeException("Cannot reschedule a cancelled appointment.");
        }

        if (existingAppointment.getStatus() == AppointmentStatus.COMPLETED) {
            logger.warn("Cannot reschedule completed appointment: {}", request.confirmationCode());
            throw new RuntimeException("Cannot reschedule a completed appointment.");
        }

        // 3. Check if new time slot is not already booked by this doctor
        if (appointmentRepository.existsByDoctorIdAndAppointmentDate(
                existingAppointment.getDoctorId(), request.newAppointmentTime())) {
            logger.warn("Doctor {} is not available at requested time: {}", 
                    existingAppointment.getDoctorId(), request.newAppointmentTime());
            throw new RuntimeException("Doctor is not available at the requested time.");
        }

        // 4. REMOTE CALL - Release the old slot in Doctor Schedule Service
        try {
            logger.debug("Releasing old time slot for confirmationCode: {}", request.confirmationCode());
            doctorSheduleClient.cancelBooking(existingAppointment.getDoctorId(), 
                    existingAppointment.getPatientId(), 
                    existingAppointment.getAppointmentDate());
            logger.debug("Old time slot successfully released");
        } catch (Exception e) {
            logger.error("Failed to release old time slot for confirmationCode: {}", request.confirmationCode(), e);
            throw new RuntimeException("Unable to release old time slot: " + e.getMessage());
        }

        // 5. REMOTE CALL - Claim the new time slot in Doctor Schedule Service
        try {
            logger.debug("Claiming new time slot for confirmationCode: {}", request.confirmationCode());
            doctorSheduleClient.claimTimeSlot(existingAppointment.getDoctorId(), 
                    existingAppointment.getPatientId(), 
                    request.newAppointmentTime());
            logger.debug("New time slot successfully claimed");
        } catch (Exception e) {
            logger.error("Failed to claim new time slot for confirmationCode: {}", request.confirmationCode(), e);
            throw new RuntimeException("Unable to claim new time slot: " + e.getMessage());
        }

        // 6. Update the appointment with new time
        existingAppointment.setAppointmentDate(request.newAppointmentTime());
        existingAppointment.setStatus(AppointmentStatus.CONFIRMED); // Reconfirm after rescheduling

        try {
            Appointment rescheduledAppointment = appointmentRepository.save(existingAppointment);
            logger.info("Appointment successfully rescheduled with confirmationCode: {} to newTime: {}", 
                    request.confirmationCode(), request.newAppointmentTime());

            // 7. SEND NOTIFICATION - Notify about appointment rescheduling
            try {
                logger.debug("Sending rescheduling notification for confirmationCode: {}", request.confirmationCode());
                NotificationRequest notification = new NotificationRequest();
                notification.setPatientId(rescheduledAppointment.getPatientId());
                notification.setDoctorId(rescheduledAppointment.getDoctorId());
                notification.setStatus("RESCHEDULED");
                notification.setConfirmationCode(rescheduledAppointment.getConfirmationCode());
                notification.setTime(rescheduledAppointment.getAppointmentDate().toLocalTime());
                notification.setDate(rescheduledAppointment.getAppointmentDate().toLocalDate());

                notificationServiceClient.sendNotification(notification);
                logger.info("Rescheduling notification successfully sent for confirmationCode: {}", request.confirmationCode());
            } catch (Exception e) {
                logger.warn("Failed to send rescheduling notification for confirmationCode: {}: {}", 
                        request.confirmationCode(), e.getMessage());
            }

            return rescheduledAppointment;
        } catch (ObjectOptimisticLockingFailureException e) {
            logger.error("Optimistic locking conflict during rescheduling for confirmationCode: {}", request.confirmationCode(), e);
            throw new RuntimeException("Concurrent rescheduling detected. Please try again.");
        }
    }
}