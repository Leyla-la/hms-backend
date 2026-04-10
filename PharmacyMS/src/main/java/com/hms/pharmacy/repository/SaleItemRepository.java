package com.hms.pharmacy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.hms.pharmacy.entity.SaleItem;

import java.util.List;

@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {
    List<SaleItem> findBySaleId(Long saleId);

    List<SaleItem> findByMedicineId(Long medicineId);

    @org.springframework.data.jpa.repository.Query("SELECT si.medicine.id, si.medicine.name, SUM(si.quantity) FROM SaleItem si GROUP BY si.medicine.id, si.medicine.name ORDER BY SUM(si.quantity) DESC")
    List<Object[]> findTopMedicinesByQuantity(org.springframework.data.domain.Pageable pageable);
}
