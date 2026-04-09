package com.notification.management.system.controller;

import com.notification.management.system.dto.NotificationRequestDTO;
import com.notification.management.system.dto.NotificationResponseDTO;
import com.notification.management.system.model.RecipientType;
import com.notification.management.system.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<List<NotificationResponseDTO>> sendNotification(
            @Valid @RequestBody NotificationRequestDTO requestDTO) {
        List<NotificationResponseDTO> responses = notificationService.sendNotification(requestDTO);
        return new ResponseEntity<>(responses, HttpStatus.CREATED);
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<NotificationResponseDTO>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(notificationService.getNotificationsByPatient(patientId));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<NotificationResponseDTO>> getByDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(notificationService.getNotificationsByDoctor(doctorId));
    }

    @GetMapping("/recipient/{type}")
    public ResponseEntity<List<NotificationResponseDTO>> getByRecipientType(@PathVariable RecipientType type) {
       return ResponseEntity.ok(notificationService.getNotificationsByRecipientType(type));
    }
}