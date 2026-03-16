package com.hms.appointment.service;

import com.hms.appointment.dto.AppointmentRecordDTO;
import com.hms.appointment.entity.AppointmentRecord;
import com.hms.appointment.exception.HmsException;
import com.hms.appointment.repository.AppointmentRecordRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.hms.appointment.utility.StringListConverter.toCsv;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppointmentRecordServiceImpl implements AppointmentRecordService{

    AppointmentRecordRepository appointmentRecordRepository;
    PrescriptionService prescriptionService;

    @Override
    public Long createAppointmentRecord(AppointmentRecordDTO appointmentRecordDTO) throws HmsException {
        Optional<AppointmentRecord> existingRecord = appointmentRecordRepository.findByAppointment_Id(appointmentRecordDTO.getAppointmentId());

        if (existingRecord.isPresent()) {
            throw new HmsException("APPOINTMENT_RECORD_ALREADY_EXISTS");
        }

        Long appointmentRecordId = appointmentRecordRepository.save(appointmentRecordDTO.toAppointmentRecord()).getId();
        if (appointmentRecordDTO.getPrescription() != null) {
            appointmentRecordDTO.getPrescription().setAppointmentId(appointmentRecordDTO.getAppointmentId());
            prescriptionService.savePrescription(appointmentRecordDTO.getPrescription());
        }
        return appointmentRecordId;
    }

    @Override
    public void updateAppointmentRecord(AppointmentRecordDTO appointmentRecordDTO) throws HmsException {
        AppointmentRecord existingRecord = appointmentRecordRepository.findById(appointmentRecordDTO.getId()).orElseThrow(() -> new HmsException("APPOINTMENT_RECORD_NOT_FOUND"));
        existingRecord.setNotes(appointmentRecordDTO.getNotes());
        existingRecord.setDiagnosis(appointmentRecordDTO.getDiagnosis());
        existingRecord.setFollowUpDate(appointmentRecordDTO.getFollowUpDate());
        existingRecord.setSymptoms(toCsv(appointmentRecordDTO.getSymptoms()));
        existingRecord.setTests(toCsv(appointmentRecordDTO.getTests()));
        existingRecord.setReferral(appointmentRecordDTO.getReferral());



    }

    @Override
    public AppointmentRecordDTO getAppointmentRecordByAppointmentId(Long appointmentId) throws HmsException {
        return appointmentRecordRepository.findByAppointment_Id(appointmentId)
                .map(AppointmentRecord::toAppointmentRecordDTO)
                .orElseThrow(() -> new HmsException("APPOINTMENT_RECORD_NOT_FOUND"));
    }

    @Override
    public AppointmentRecordDTO getAppointmentRecordById(Long appointmentRecordId) throws HmsException {
        return appointmentRecordRepository.findById(appointmentRecordId)
                .map(AppointmentRecord::toAppointmentRecordDTO)
                .orElseThrow(() -> new HmsException("APPOINTMENT_RECORD_NOT_FOUND"));
    }

    @Override
    public AppointmentRecordDTO getAppointmentRecordDetailsByAppointmentId(Long appointmentId) throws HmsException {
        AppointmentRecordDTO record = appointmentRecordRepository.findByAppointment_Id(appointmentId)
                .map(AppointmentRecord::toAppointmentRecordDTO)
                .orElseThrow(() -> new HmsException("APPOINTMENT_RECORD_NOT_FOUND"));

        record.setPrescription(prescriptionService.getPrescriptionByAppointmentId(appointmentId));

        return record;
    }
}
