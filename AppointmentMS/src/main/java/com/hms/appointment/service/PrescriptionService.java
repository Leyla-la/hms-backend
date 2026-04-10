package com.hms.appointment.service;

import com.hms.appointment.dto.PrescriptionDTO;
import com.hms.appointment.dto.PrescriptionDetails;
import com.hms.appointment.exception.HmsException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PrescriptionService {
    Long savePrescription(PrescriptionDTO prescriptionDTO) throws HmsException;
    PrescriptionDTO getPrescriptionByAppointmentId(Long appointmentId) throws HmsException;
    PrescriptionDTO getPrescriptionById(Long prescriptionId) throws HmsException;
    List<PrescriptionDetails> getPrescriptionsByPatientId(Long patientId) throws HmsException;
    List<PrescriptionDetails> getPrescriptionsByDoctorId(Long doctorId) throws HmsException;
    
    // Server-side paginated retrieval for all prescriptions
    Page<PrescriptionDetails> getPrescriptions(Pageable pageable) throws HmsException;

    // Non-paginated optimized retrieval for all prescriptions (for modal use)
    List<PrescriptionDetails> getAllPrescriptionsList() throws HmsException;
}
