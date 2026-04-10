package com.hms.pharmacy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class PharmacyApplication {
    public static void main(String[] args) {
        SpringApplication.run(PharmacyApplication.class, args);
    }
}
