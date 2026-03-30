package com.hospital.management.doctorservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SlotResponseDTO {
    private Long slotId;
    private LocalTime startTime;
    private boolean isBooked;
    // The Doctor's UI will filter for isBooked = true
    // The Patient's UI will filter for isBooked = false
}