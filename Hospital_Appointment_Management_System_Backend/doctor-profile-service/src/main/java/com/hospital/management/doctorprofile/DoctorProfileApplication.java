package com.hospital.management.doctorprofile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class DoctorProfileApplication {

    public static void main(String[] args) {
        SpringApplication.run(DoctorProfileApplication.class, args);
    }
}