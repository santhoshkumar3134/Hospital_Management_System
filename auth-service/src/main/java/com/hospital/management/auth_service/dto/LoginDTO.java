package com.hospital.management.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginDTO {
    private String email;
    private String password;
}

/**
 * Authentication Manager - credentials
 * Authontication Provider
 * DaoAuthentication Provider  database fetch database  gives the email to userdetails service
 * UserDetails Service -linked with database, fetches the entity it returns to Auth provider
 */