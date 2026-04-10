package com.hms.pharmacy.dto;


import com.hms.pharmacy.entity.Sale;
import com.hms.pharmacy.entity.SaleItem;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SaleDTO {
    Long id;
    Long prescriptionId;
    LocalDateTime saleDate;
    Double totalAmount;
    String buyerName;
    String buyerContact;
    java.util.List<SaleItemDTO> saleItems;

    public Sale toSale() {
        Sale s = new Sale(this.id, this.prescriptionId, this.saleDate, this.totalAmount, this.buyerName, this.buyerContact, null);
        if (this.saleItems != null) {
            s.setSaleItems(this.saleItems.stream().map(item -> {
                SaleItem si = item.toSaleItem();
                si.setSale(s);
                return si;
            }).collect(java.util.stream.Collectors.toList()));
        }
        return s;
    }
}
