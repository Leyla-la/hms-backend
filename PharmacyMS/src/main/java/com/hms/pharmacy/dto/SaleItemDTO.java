package com.hms.pharmacy.dto;


import com.hms.pharmacy.entity.Medicine;
import com.hms.pharmacy.entity.Sale;
import com.hms.pharmacy.entity.SaleItem;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SaleItemDTO {
    Long id;
    Long saleId;
    Long medicineId;
    String batchNo;
    Integer quantity;
    Double unitPrice;

    public SaleItem toSaleItem() {
        return new SaleItem(this.id, new Sale(this.saleId), new Medicine(this.medicineId), this.batchNo, this.quantity, this.unitPrice);
    }

}
