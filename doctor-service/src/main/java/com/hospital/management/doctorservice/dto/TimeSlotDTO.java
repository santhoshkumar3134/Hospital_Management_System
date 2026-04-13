package com.hospital.management.doctorservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Response DTO returned by GET /slots/doctor/{doctorId}/{date}.
 *
 * Only exposes what the Appointment Service needs:
 *   - startTime  → shown to patient as the appointment time
 *   - isBooked   → patient UI filters false, doctor UI filters true
 *
 * doctorId and patientId are intentionally excluded —
 * Appointment Service already knows the doctorId (they passed it in the request)
 * and patientId is internal — should never be exposed to other services.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeSlotDTO {

    private LocalDateTime startTime;
    private boolean isBooked;
}