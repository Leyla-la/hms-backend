package com.hms.appointment.service;

import com.hms.appointment.dto.MedicineDTO;
import com.hms.appointment.exception.HmsException;

import java.util.List;

public interface MedicineService {
    public Long saveMedicine(MedicineDTO medicineDTO) throws HmsException;
    public List<MedicineDTO> saveAllMedicines(List<MedicineDTO> medicineDTOList) throws HmsException;
    public List<MedicineDTO> getAllMedicinesByPrescriptionId(Long prescriptionId) throws HmsException;
}
