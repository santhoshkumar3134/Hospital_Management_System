package com.hospital.management.doctorservice.dto;

import lombok.Data;

@Data
public class BookingUpdateDTO {
    private Long slotId;
    private boolean bookedStatus; // Usually true when an appointment is made
}