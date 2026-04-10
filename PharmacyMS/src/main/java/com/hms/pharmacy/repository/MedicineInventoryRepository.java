package com.hms.pharmacy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hms.pharmacy.dto.StockStatus;
import com.hms.pharmacy.entity.Medicine;
import com.hms.pharmacy.entity.MedicineInventory;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MedicineInventoryRepository extends JpaRepository<MedicineInventory, Long> {
    List<MedicineInventory> findByExpiryDateBefore(LocalDate date);
    List<MedicineInventory> findByQuantityLessThan(Integer threshold);
    long countByStatus(StockStatus status);
}

