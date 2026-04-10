package com.hms.pharmacy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopMedicineDTO {
    private Long medicineId;
    private String name;
    private Long totalSold;
}
