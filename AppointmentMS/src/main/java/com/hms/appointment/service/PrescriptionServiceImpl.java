package com.hms.appointment.service;

import com.hms.appointment.dto.PrescriptionDTO;
import com.hms.appointment.exception.HmsException;
import com.hms.appointment.repository.PrescriptionRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PrescriptionServiceImpl implements PrescriptionService {
    PrescriptionRepository prescriptionRepository;
    MedicineService medicineService;

    @Override
    public Long savePrescription(PrescriptionDTO prescriptionDTO) throws HmsException {
        Long prescriptionId = prescriptionRepository.save(prescriptionDTO.toPrescription()).getId();
        prescriptionDTO.getMedicines().forEach(medicine -> medicine.setPrescriptionId(prescriptionId));
        medicineService.saveAllMedicines(prescriptionDTO.getMedicines());
        prescriptionDTO.setId(prescriptionId);
        return prescriptionId;
    }

    @Override
    public PrescriptionDTO getPrescriptionByAppointmentId(Long appointmentId) throws HmsException {
        PrescriptionDTO prescriptionDTO = prescriptionRepository.findByAppointment_Id(appointmentId)
                .orElseThrow(() -> new HmsException("PRESCRIPTION_NOT_FOUND")).toPrescriptionDTO();
        prescriptionDTO.setMedicines(medicineService.getAllMedicinesByPrescriptionId(prescriptionDTO.getId()));
        return prescriptionDTO;
    }

    @Override
    public PrescriptionDTO getPrescriptionById(Long prescriptionId) throws HmsException {
        PrescriptionDTO prescriptionDTO = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new HmsException("PRESCRIPTION_NOT_FOUND")).toPrescriptionDTO();
        prescriptionDTO.setMedicines(medicineService.getAllMedicinesByPrescriptionId(prescriptionDTO.getId()));
        return prescriptionDTO;
    }
}
