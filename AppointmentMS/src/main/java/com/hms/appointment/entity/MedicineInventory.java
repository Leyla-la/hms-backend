package com.hms.appointment.entity;

import lombok.Data;

@Data
public class MedicineInventory {
    private Long id;
    private String medicineName;
    private String batchNumber;
    private int quantity;
}
