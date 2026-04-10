package com.hms.pharmacy.service;

import com.hms.pharmacy.dto.MedicineDTO;
import com.hms.pharmacy.exception.HmsException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface MedicineService {
    public Long addMedicine(MedicineDTO medicineDTO) throws HmsException;
    public MedicineDTO getMedicineById(Long id) throws HmsException;
    public void updateMedicine(MedicineDTO medicineDTO) throws HmsException;
    public Page<MedicineDTO> getAllMedicines(Pageable pageable) throws HmsException;
    public Integer getStockById(Long id) throws HmsException;
    public Integer addStock(Long id, Integer quantity) throws HmsException;
    public Integer removeStock(Long id, Integer quantity) throws HmsException;
    long count();
    long getLowStockCount(Integer threshold);
}

