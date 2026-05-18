package com.hospital.management.auth_service.service;

import com.hospital.management.auth_service.client.DoctorServiceClient;
import com.hospital.management.auth_service.client.PatientServiceClient;
import com.hospital.management.auth_service.dto.*;
import com.hospital.management.auth_service.enums.Role;
import com.hospital.management.auth_service.exception.EmailException;
import com.hospital.management.auth_service.exception.ProfileCreationException;
import com.hospital.management.auth_service.exception.ResourceNotFoundException;
import com.hospital.management.auth_service.modal.UserEntity;
import com.hospital.management.auth_service.repository.UserRepository;
import com.hospital.management.auth_service.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PatientServiceClient patientServiceClient;
    private final DoctorServiceClient doctorServiceClient;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;


    @Transactional
    public void registerUser(UserDTO userDTO) {
        validateUserDTO(userDTO);

        // Fail fast with a clean message before hitting a DB constraint
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new EmailException(
                    "Email already registered: " + userDTO.getEmail());
        }

        Role role = parseRole(userDTO.getRole());

        UserEntity user = UserEntity.builder()
                .email(userDTO.getEmail())
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .role(String.valueOf(role))
                .build();


        userRepository.save(user);
        log.info("User saved in authDB — userId={}, role={}", user.getUserId(), role);

        createDownstreamProfile(userDTO, role, user.getUserId(), user);
    }

    public LoginResponseDTO loginUser(LoginDTO loginDTO) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDTO.getEmail(),
                            loginDTO.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        UserEntity user = userRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user not found in DB: " + loginDTO.getEmail()));

        String token = jwtUtil.generateToken(userDetails, user.getUserId(), user.getServiceId());

        log.info("Login successful — userId={}, role={}", user.getUserId(), user.getRole());

        return LoginResponseDTO.builder()
                .token(token)
                .userId(user.getUserId())
                .serviceId(user.getServiceId())
                .role(user.getRole())
                .build();
    }

    // Private helpers


    private void validateUserDTO(UserDTO userDTO) {
        if (!StringUtils.hasText(userDTO.getEmail())) {
            throw new IllegalArgumentException("Email must not be blank");
        }
        if (!StringUtils.hasText(userDTO.getPassword())) {
            throw new IllegalArgumentException("Password must not be blank");
        }
        if (!StringUtils.hasText(userDTO.getRole())) {
            throw new IllegalArgumentException("Role must not be blank");
        }
    }

    private Role parseRole(String rawRole) {
        try {
            return Role.valueOf(rawRole.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Unsupported role: '" + rawRole + "'. Valid values: "
                            + java.util.Arrays.toString(Role.values()));
        }
    }

    private void createDownstreamProfile(UserDTO userDTO, Role role, Long userId, UserEntity user) {
        switch (role) {
            case PATIENT -> {
                try {
                    PatientRequestDTO patientRequest = PatientRequestDTO.builder()
                            .name(userDTO.getName())
                            .gender(userDTO.getGender())
                            .dateOfBirth(userDTO.getDateOfBirth())
                            .contactDetails(userDTO.getContactDetails())
                            .build();

                    PatientResponseDTO response = patientServiceClient.registerPatient(patientRequest);
                    Long patientId = response.getPatientId();

                    user.setServiceId(patientId);
                    userRepository.save(user);

                    log.info("Patient profile created — userId={}, patientId={}", userId, patientId);
                } catch (Exception e) {
                    log.error("Patient profile creation failed — userId={}, error={}",
                            userId, e.getMessage());
                    throw new ProfileCreationException(
                            "Could not create patient profile. Registration rolled back.", e);
                }
            }

            case DOCTOR -> {
                try {
                    DoctorRequestDTO doctorRequest = DoctorRequestDTO.builder()
                            .name(userDTO.getName())
                            .email(userDTO.getEmail())
                            .contactDetails(userDTO.getContactDetails())
                            .specialization(userDTO.getSpecialization())
                            .designation(userDTO.getDesignation())
                            .build();

                    DoctorResponseDTO response = doctorServiceClient.registerDoctor(doctorRequest);
                    Long doctorId = response.getId();
                    log.info("Doctor service returned id={}, full response={}", doctorId, response); // ← add this

                    user.setServiceId(doctorId);
                    userRepository.save(user);

                    log.info("Doctor profile created — userId={}, doctorId={}", userId, doctorId);
                } catch (Exception e) {
                    log.error("Doctor profile creation failed — userId={}, error={}",
                            userId, e.getMessage());
                    throw new ProfileCreationException(
                            "Could not create doctor profile. Registration rolled back.", e);
                }
            }

            case ADMIN -> throw new IllegalArgumentException(
                    "Admin accounts cannot be self-registered.");
        }
    }
    public List<UserResponseDTO> getUsersByRole(String role) {
        Role parsedRole = parseRole(role);
        return userRepository.findByRole(parsedRole.name())
                .stream()
                .map(user -> UserResponseDTO.builder()
                        .userId(user.getUserId())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .build())
                .toList();
    }

    public String getEmailByServiceId(Long serviceId, String role) {
        return userRepository.findFirstByServiceIdAndRole(serviceId, role)
                .map(UserEntity::getEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No user found with serviceId: " + serviceId + " and role: " + role));
    }


    public String getEmailByUserId(Long userId) {
        return userRepository.findByUserId(userId)
                .map(UserEntity::getEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No user found with userId: " + userId));
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> UserResponseDTO.builder()
                        .userId(user.getUserId())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .build())
                .toList();
    }
}