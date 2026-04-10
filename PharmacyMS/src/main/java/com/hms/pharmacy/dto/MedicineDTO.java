package com.hms.pharmacy.dto;

import com.hms.pharmacy.entity.Medicine;
import com.hms.pharmacy.entity.MedicineCategory;
import com.hms.pharmacy.entity.MedicineType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MedicineDTO {
    Long id;
    String name;
    String dosage;
    MedicineCategory category;
    MedicineType type;
    String manufacturer;
    Integer unitPrice;
    Integer stock;
    LocalDateTime createdAt;

    public Medicine toMedicine() {
        return new Medicine(this.id, this.name, this.dosage, this.category, this.type, this.manufacturer, this.unitPrice, this.stock, this.createdAt);
    }
}
