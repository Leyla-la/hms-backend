package com.hms.pharmacy.dto;

import lombok.Data;

@Data
public class PatientDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String citizenId;
}