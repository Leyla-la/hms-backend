package com.hms.pharmacy.service;

import com.hms.pharmacy.dto.SaleDTO;
import com.hms.pharmacy.exception.HmsException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface SaleService {
    Long createSale(SaleDTO saleDTO) throws HmsException;

    void updateSale(SaleDTO saleDTO) throws HmsException;

    SaleDTO getSaleById(Long id) throws HmsException;

    SaleDTO getSaleByPrescriptionId(Long prescriptionId) throws HmsException;

    Page<SaleDTO> getAllSales(Pageable pageable) throws HmsException;
    List<SaleDTO> getRecentSales(int limit) throws HmsException;
    double getTotalRevenue() throws HmsException;
    List<com.hms.pharmacy.dto.TopPatientDTO> getTopPatients() throws HmsException;
    List<com.hms.pharmacy.dto.TopMedicineDTO> getTopMedicines() throws HmsException;
}
