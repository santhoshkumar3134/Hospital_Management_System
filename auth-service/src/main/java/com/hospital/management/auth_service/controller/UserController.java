package com.hospital.management.auth_service.controller;

import com.hospital.management.auth_service.dto.LoginDTO;
import com.hospital.management.auth_service.dto.UserDTO;
import com.hospital.management.auth_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("register")
    public ResponseEntity<String> registerUser(@RequestBody UserDTO userDTO){
        return userService.registerUser(userDTO);
    }

    @PostMapping("login")
    public ResponseEntity<String> loginUser(@RequestBody LoginDTO loginDTO){
        return userService.loginUser(loginDTO);
    }
}
