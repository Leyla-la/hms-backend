package com.hms.pharmacy.entity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import com.hms.pharmacy.dto.SaleItemDTO;

@Entity
@Table(name = "sale_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SaleItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "sale_id")
    Sale sale;

    @ManyToOne
    @JoinColumn(name = "medicine_id")
    Medicine medicine;

    String batchNo;
    Integer quantity;
    Double unitPrice;

    public SaleItemDTO toSaleItemDTO() {
        return new SaleItemDTO(this.id, this.sale != null ? this.sale.getId() : null, this.medicine != null ? this.medicine.getId() : null, this.batchNo, this.quantity, this.unitPrice);
    }
}
