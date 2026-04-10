package com.hms.profile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@EntityScan(basePackages = { "com.hms.profile.entity" })
public class ProfileMsApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProfileMsApplication.class, args);
	}

}
