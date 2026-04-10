package com.hms.pharmacy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;
import com.hms.pharmacy.entity.Sale;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    @org.springframework.data.jpa.repository.Query("SELECT count(s) > 0 FROM Sale s WHERE s.prescriptionId = :prescriptionId")
    boolean existsByPrescriptionId(@org.springframework.data.repository.query.Param("prescriptionId") Long prescriptionId);

    @org.springframework.data.jpa.repository.Query("SELECT s FROM Sale s WHERE s.prescriptionId = :prescriptionId")
    Optional<Sale> findByPrescriptionId(@org.springframework.data.repository.query.Param("prescriptionId") Long prescriptionId);

    List<Sale> findTop5ByOrderBySaleDateDesc();

    @org.springframework.data.jpa.repository.Query("SELECT SUM(COALESCE(s.totalAmount, 0)) FROM Sale s")
    Double getTotalRevenue();
    
    @org.springframework.data.jpa.repository.Query("SELECT s.buyerName, s.buyerContact, SUM(s.totalAmount) FROM Sale s WHERE s.buyerName IS NOT NULL OR s.buyerContact IS NOT NULL GROUP BY s.buyerName, s.buyerContact ORDER BY SUM(s.totalAmount) DESC")
    List<Object[]> findTopPatientsByRevenue(org.springframework.data.domain.Pageable pageable);

}