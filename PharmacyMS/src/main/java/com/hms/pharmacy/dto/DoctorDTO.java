package com.hms.pharmacy.dto;

import lombok.Data;

@Data
public class DoctorDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String specialization;
    private String licenseNo;
}