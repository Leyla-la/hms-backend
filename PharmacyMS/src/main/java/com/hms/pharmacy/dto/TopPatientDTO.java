package com.hms.pharmacy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopPatientDTO {
    private String name;
    private String contact;
    private Double totalSpent;
}
