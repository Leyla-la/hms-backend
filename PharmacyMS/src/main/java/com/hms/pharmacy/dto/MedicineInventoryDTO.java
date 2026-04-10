package com.hms.pharmacy.dto;

import com.hms.pharmacy.entity.Medicine;
import com.hms.pharmacy.entity.MedicineInventory;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;


@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MedicineInventoryDTO {
    Long id;
    Long medicineId;
    String medicineName;
    String manufacturer;
    String batchNo;
    Integer quantity;
    LocalDate expiryDate;
    LocalDate addedDate;
    Integer initialQuantity;
    StockStatus status;

    public MedicineInventory toMedicineInventory() {
        return new MedicineInventory(this.id, new Medicine(this.medicineId), this.batchNo, this.quantity, this.expiryDate, this.addedDate, this.initialQuantity, this.status);
    }
}
