package com.hms.pharmacy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.hms.pharmacy.entity.Medicine;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {
    List<Medicine> findByNameIgnoreCaseAndDosageIgnoreCase(String name, String dosage);

    Optional<Integer> findStockById(Long id);
    long countByStockLessThan(Integer threshold);
}
