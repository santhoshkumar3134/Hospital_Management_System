package com.notification.management.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;


@SpringBootApplication
@EnableFeignClients
public class HospitalManagementSystemNotificationApplication {

	public static void main(String[] args) {
		SpringApplication.run(HospitalManagementSystemNotificationApplication.class, args);
	}

}
