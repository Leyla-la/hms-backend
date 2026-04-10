package com.hms.appointment.service;

import com.hms.appointment.dto.MedicineDTO;
import com.hms.appointment.entity.Medicine;
import com.hms.appointment.entity.Prescription;
import com.hms.appointment.exception.HmsException;
import com.hms.appointment.repository.MedicineRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MedicineServiceImpl implements MedicineService{
    MedicineRepository medicineRepository;

    @Override
    public Long saveMedicine(MedicineDTO medicineDTO) throws HmsException {
        return medicineRepository.save(medicineDTO.toMedicine()).getId();
    }

    @Override
    public List<MedicineDTO> saveAllMedicines(List<MedicineDTO> medicineDTOList) throws HmsException {
        return medicineRepository
                .saveAll(medicineDTOList.stream().map(MedicineDTO::toMedicine).toList())
                .stream()
                .map(Medicine::toMedicineDTO)
                .toList();
    }

    @Override
    public List<MedicineDTO> getAllMedicinesByPrescriptionId(Long prescriptionId) throws HmsException {
        return medicineRepository.findAllByPrescription_Id(prescriptionId)
                .stream()
                .map(Medicine::toMedicineDTO)
                .toList();
    }

    @Override
    public List<MedicineDTO> getMedicinesByPrescriptionIds(List<Long> prescriptionIds) throws HmsException {
        if (prescriptionIds == null || prescriptionIds.isEmpty()) return List.of();
        return medicineRepository.findAllByPrescription_IdIn(prescriptionIds)
                .stream()
                .map(Medicine::toMedicineDTO)
                .toList();
    }
}
