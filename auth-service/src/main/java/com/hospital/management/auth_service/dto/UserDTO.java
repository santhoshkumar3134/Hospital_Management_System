package com.hospital.management.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Integer userId;
    private String name;
    private String gender;
    private String phoneNumber;
    private String email;
    private String password;
    private LocalDate dob;
    private String role;
}
