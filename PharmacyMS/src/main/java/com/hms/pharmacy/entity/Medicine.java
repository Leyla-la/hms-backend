package com.hms.pharmacy.entity;

import com.hms.pharmacy.dto.MedicineDTO;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Medicine {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    Long id;
    String name;
    String dosage;
    MedicineCategory category;
    MedicineType type;
    String manufacturer;
    Integer unitPrice;
    Integer stock;
    LocalDateTime createdAt;

    public Medicine(Long id) {
        this.id = id;
    }

    public MedicineDTO toMedicineDTO() {
        return new MedicineDTO(this.id, this.name, this.dosage, this.category, this.type, this.manufacturer, this.unitPrice, this.stock, this.createdAt);
    }
}
