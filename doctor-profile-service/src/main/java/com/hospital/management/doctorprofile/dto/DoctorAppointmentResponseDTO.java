package com.hospital.management.doctorprofile.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorAppointmentResponseDTO {
        private Long doctorId;
        private String doctorName;
        private String specialization;
}
