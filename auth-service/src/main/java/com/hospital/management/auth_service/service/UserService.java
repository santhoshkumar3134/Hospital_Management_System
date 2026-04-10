package com.hospital.management.auth_service.service;

import com.hospital.management.auth_service.dto.LoginDTO;
import com.hospital.management.auth_service.dto.UserDTO;
import com.hospital.management.auth_service.modal.UserEntity;
import com.hospital.management.auth_service.repository.UserRepository;
import com.hospital.management.auth_service.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private PatientService patientService;
    @Autowired
    public AuthenticationManager authenticationManager;
    @Autowired
    public JwtUtil jwtUtil;


    public ResponseEntity<String> registerUser(UserDTO userDTO){
        UserEntity user = new UserEntity();
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setRole(userDTO.getRole());

        user = userRepository.save(user);

        userDTO.setUserId(user.getUserId());
        patientService.registerPatient(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body("success");
    }

    public ResponseEntity<String> loginUser(LoginDTO loginDTO){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getEmail(),loginDTO.getPassword())
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return new ResponseEntity<>(jwtUtil.generateToken(userDetails),HttpStatus.OK);
    }
}
