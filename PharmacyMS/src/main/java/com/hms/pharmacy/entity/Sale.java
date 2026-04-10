package com.hms.pharmacy.entity;

import com.hms.pharmacy.dto.SaleDTO;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Sale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    Long prescriptionId;
    LocalDateTime saleDate;
    Double totalAmount;
    String buyerName;
    String buyerContact;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL)
    List<SaleItem> saleItems;

    public Sale(Long id) {
        this.id = id;
    }

    public SaleDTO toSaleDTO() {
        return SaleDTO.builder()
                .id(this.id)
                .prescriptionId(this.prescriptionId)
                .saleDate(this.saleDate)
                .totalAmount(this.totalAmount)
                .buyerName(this.buyerName)
                .buyerContact(this.buyerContact)
                .saleItems(this.saleItems != null ? this.saleItems.stream().map(SaleItem::toSaleItemDTO).collect(java.util.stream.Collectors.toList()) : null)
                .build();
    }
}
