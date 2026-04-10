package com.hms.appointment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.hms.appointment.clients")
public class AppointmentMsApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppointmentMsApplication.class, args);
	}

}
