package com.hospital.management.auth_service.service;

import com.hospital.management.auth_service.client.DoctorServiceClient;
import com.hospital.management.auth_service.client.PatientServiceClient;
import com.hospital.management.auth_service.dto.*;
import com.hospital.management.auth_service.exception.EmailException;
import com.hospital.management.auth_service.exception.ProfileCreationException;
import com.hospital.management.auth_service.modal.UserEntity;
import com.hospital.management.auth_service.repository.UserRepository;
import com.hospital.management.auth_service.security.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private PatientServiceClient patientServiceClient;
    @Mock private DoctorServiceClient doctorServiceClient;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    // ─── registerUser ────────────────────────────────────────────────────────

    @Test
    @DisplayName("registerUser: patient registration creates user and downstream profile")
    void registerUser_patient_success() {
        UserDTO dto = patientDto();
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");

        UserEntity saved = userEntity(1L, "PATIENT");
        when(userRepository.save(any(UserEntity.class))).thenReturn(saved);

        PatientResponseDTO patientResp = new PatientResponseDTO();
        patientResp.setPatientId(101L);
        when(patientServiceClient.registerPatient(any())).thenReturn(patientResp);

        assertDoesNotThrow(() -> userService.registerUser(dto));

        verify(userRepository, atLeastOnce()).save(any(UserEntity.class));
        verify(patientServiceClient).registerPatient(any());
        verifyNoInteractions(doctorServiceClient);
    }

    @Test
    @DisplayName("registerUser: doctor registration creates user and downstream profile")
    void registerUser_doctor_success() {
        UserDTO dto = doctorDto();
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");

        UserEntity saved = userEntity(2L, "DOCTOR");
        when(userRepository.save(any(UserEntity.class))).thenReturn(saved);

        DoctorResponseDTO doctorResp = new DoctorResponseDTO();
        doctorResp.setId(201L);
        when(doctorServiceClient.registerDoctor(any())).thenReturn(doctorResp);

        assertDoesNotThrow(() -> userService.registerUser(dto));

        verify(userRepository, atLeastOnce()).save(any(UserEntity.class));
        verify(doctorServiceClient).registerDoctor(any());
        verifyNoInteractions(patientServiceClient);
    }

    @Test
    @DisplayName("registerUser: duplicate email throws EmailException")
    void registerUser_duplicateEmail_throwsEmailException() {
        UserDTO dto = patientDto();
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThrows(EmailException.class, () -> userService.registerUser(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("registerUser: ADMIN role throws IllegalArgumentException")
    void registerUser_adminRole_throwsIllegalArgumentException() {
        UserDTO dto = patientDto();
        dto.setRole("ADMIN");
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any())).thenReturn(userEntity(1L, "ADMIN"));

        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(dto));
    }

    @Test
    @DisplayName("registerUser: unknown role throws IllegalArgumentException")
    void registerUser_invalidRole_throwsIllegalArgumentException() {
        UserDTO dto = patientDto();
        dto.setRole("SUPERUSER");
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(dto));
    }

    @Test
    @DisplayName("registerUser: blank email throws IllegalArgumentException")
    void registerUser_blankEmail_throwsIllegalArgumentException() {
        UserDTO dto = patientDto();
        dto.setEmail("  ");

        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(dto));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("registerUser: patient client failure throws ProfileCreationException and rolls back")
    void registerUser_patientClientFailure_throwsProfileCreationException() {
        UserDTO dto = patientDto();
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any())).thenReturn(userEntity(1L, "PATIENT"));
        when(patientServiceClient.registerPatient(any()))
                .thenThrow(new RuntimeException("patient-service down"));

        assertThrows(ProfileCreationException.class, () -> userService.registerUser(dto));
    }

    // ─── loginUser ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("loginUser: valid credentials return LoginResponseDTO with token")
    void loginUser_validCredentials_returnsToken() {
        LoginDTO dto = new LoginDTO();
        dto.setEmail("patient@test.com");
        dto.setPassword("Password1");

        Authentication auth = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);

        UserEntity user = userEntity(1L, "ROLE_PATIENT");
        user.setServiceId(101L);
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(any(), anyLong(), anyLong())).thenReturn("jwt-token");

        LoginResponseDTO result = userService.loginUser(dto);

        assertEquals("jwt-token", result.getToken());
        assertEquals("ROLE_PATIENT", result.getRole());
        assertEquals(1L, result.getUserId());
    }

    @Test
    @DisplayName("loginUser: bad credentials throw ResponseStatusException 401")
    void loginUser_badCredentials_throwsUnauthorized() {
        LoginDTO dto = new LoginDTO();
        dto.setEmail("wrong@test.com");
        dto.setPassword("wrongpass");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad credentials"));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class, () -> userService.loginUser(dto));
        assertEquals(401, ex.getStatusCode().value());
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private UserDTO patientDto() {
        UserDTO dto = new UserDTO();
        dto.setEmail("patient@test.com");
        dto.setPassword("Password1");
        dto.setRole("PATIENT");
        dto.setName("Test Patient");
        dto.setGender("MALE");
        dto.setContactDetails("9876543210");
        return dto;
    }

    private UserDTO doctorDto() {
        UserDTO dto = new UserDTO();
        dto.setEmail("doctor@test.com");
        dto.setPassword("Password1");
        dto.setRole("DOCTOR");
        dto.setName("Dr. Test");
        dto.setSpecialization("Cardiology");
        dto.setDesignation("Consultant");
        dto.setContactDetails("9876543211");
        return dto;
    }

    private UserEntity userEntity(Long userId, String role) {
        return UserEntity.builder()
                .userId(userId)
                .email("test@test.com")
                .password("encoded")
                .role(role)
                .build();
    }
}
