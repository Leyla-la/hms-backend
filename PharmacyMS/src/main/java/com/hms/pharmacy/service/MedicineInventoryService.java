package com.hms.pharmacy.service;

import com.hms.pharmacy.dto.MedicineInventoryDTO;
import com.hms.pharmacy.dto.ResponseDTO;
import com.hms.pharmacy.exception.HmsException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface MedicineInventoryService {
    Page<MedicineInventoryDTO> getAllMedicines(Pageable pageable) throws HmsException;
    MedicineInventoryDTO getMedicineById(Long id) throws HmsException;
    MedicineInventoryDTO addMedicine(MedicineInventoryDTO medicine) throws HmsException;
    MedicineInventoryDTO updateMedicine(MedicineInventoryDTO medicine) throws HmsException;
    void deleteMedicine(Long id) throws HmsException;
    void deleteExpiredMedicines() throws HmsException;
    long count();
    long getLowStockCount(Integer threshold);
    com.hms.pharmacy.dto.PharmacyStatsDTO getPharmacyStats() throws HmsException;
}
