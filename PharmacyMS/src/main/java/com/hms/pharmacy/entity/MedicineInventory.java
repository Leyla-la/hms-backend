package com.hms.pharmacy.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import com.hms.pharmacy.dto.StockStatus;
import com.hms.pharmacy.dto.MedicineInventoryDTO;

import java.time.LocalDate;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MedicineInventory {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    Medicine medicine;
    String batchNo;
    Integer quantity;
    LocalDate expiryDate;
    LocalDate addedDate;
    Integer initialQuantity;
    @Enumerated(EnumType.STRING)
    StockStatus status;



    public MedicineInventoryDTO toMedicineInventoryDTO() {
        MedicineInventoryDTO dto = new MedicineInventoryDTO();
        dto.setId(this.id);
        dto.setMedicineId(this.medicine.getId());
        dto.setMedicineName(this.medicine.getName());
        dto.setManufacturer(this.medicine.getManufacturer());
        dto.setBatchNo(this.batchNo);
        dto.setQuantity(this.quantity);
        dto.setExpiryDate(this.expiryDate);
        dto.setAddedDate(this.addedDate);
        dto.setInitialQuantity(this.initialQuantity);
        dto.setStatus(this.status);
        return dto;
    }
}