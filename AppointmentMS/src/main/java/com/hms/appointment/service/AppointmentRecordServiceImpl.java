package com.hms.appointment.service;

import com.hms.appointment.dto.AppointmentRecordDTO;
import com.hms.appointment.dto.Status;
import com.hms.appointment.entity.Appointment;
import com.hms.appointment.entity.AppointmentRecord;
import com.hms.appointment.exception.HmsException;
import com.hms.appointment.repository.AppointmentRepository;
import com.hms.appointment.repository.AppointmentRecordRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.hms.appointment.clients.ProfileClient;
import com.hms.appointment.dto.DoctorName;
import static com.hms.appointment.utility.StringListConverter.toCsv;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppointmentRecordServiceImpl implements AppointmentRecordService{

    AppointmentRecordRepository appointmentRecordRepository;
    AppointmentRepository appointmentRepository;
    PrescriptionService prescriptionService;
    ProfileClient profileClient;

    @Override
    public Long createAppointmentRecord(AppointmentRecordDTO DTO) throws HmsException {
        // 1. Check if record already exists
        if (appointmentRecordRepository.findByAppointment_Id(DTO.getAppointmentId()).isPresent()) {
            throw new HmsException("APPOINTMENT_RECORD_ALREADY_EXISTS");
        }

        // 2. Fetch the "real" managed Appointment entity to avoid detached object errors
        Appointment appointment = appointmentRepository.findById(DTO.getAppointmentId())
                .orElseThrow(() -> new HmsException("APPOINTMENT_NOT_FOUND"));

        // 3. Map DTO to Entity and associate with the managed Appointment
        AppointmentRecord record = DTO.toAppointmentRecord();
        record.setAppointment(appointment);

        // 4. Save the record
        Long recordId = appointmentRecordRepository.save(record).getId();

        // 5. Update appointment status to COMPLETED (as per business rules)
        appointment.setStatus(Status.COMPLETED);
        appointmentRepository.save(appointment);

        // 6. Save prescription if provided
        if (DTO.getPrescription() != null) {
            DTO.getPrescription().setAppointmentId(DTO.getAppointmentId());
            prescriptionService.savePrescription(DTO.getPrescription());
        }

        return recordId;
    }

    @Override
    public void updateAppointmentRecord(AppointmentRecordDTO appointmentRecordDTO) throws HmsException {
        AppointmentRecord existingRecord = appointmentRecordRepository.findById(appointmentRecordDTO.getId())
                .orElseThrow(() -> new HmsException("APPOINTMENT_RECORD_NOT_FOUND"));

        existingRecord.setNotes(appointmentRecordDTO.getNotes());
        existingRecord.setDiagnosis(appointmentRecordDTO.getDiagnosis());
        existingRecord.setFollowUpDate(appointmentRecordDTO.getFollowUpDate());
        existingRecord.setSymptoms(toCsv(appointmentRecordDTO.getSymptoms()));
        existingRecord.setTests(toCsv(appointmentRecordDTO.getTests()));
        existingRecord.setReferral(appointmentRecordDTO.getReferral());

        appointmentRecordRepository.save(existingRecord);

        // Also update prescription if present
        if (appointmentRecordDTO.getPrescription() != null) {
            appointmentRecordDTO.getPrescription().setAppointmentId(existingRecord.getAppointment().getId());
            prescriptionService.savePrescription(appointmentRecordDTO.getPrescription());
        }
    }

    @Override
    public AppointmentRecordDTO getAppointmentRecordByAppointmentId(Long appointmentId) throws HmsException {
        AppointmentRecordDTO record = appointmentRecordRepository.findByAppointment_Id(appointmentId)
                .map(AppointmentRecord::toAppointmentRecordDTO)
                .orElseThrow(() -> new HmsException("APPOINTMENT_RECORD_NOT_FOUND"));
        enrichNamesWithBatching(List.of(record));
        return record;
    }

    @Override
    public AppointmentRecordDTO getAppointmentRecordById(Long appointmentRecordId) throws HmsException {
        AppointmentRecordDTO record = appointmentRecordRepository.findById(appointmentRecordId)
                .map(AppointmentRecord::toAppointmentRecordDTO)
                .orElseThrow(() -> new HmsException("APPOINTMENT_RECORD_NOT_FOUND"));
        enrichNamesWithBatching(List.of(record));
        return record;
    }

    @Override
    public AppointmentRecordDTO getAppointmentRecordDetailsByAppointmentId(Long appointmentId) throws HmsException {
        AppointmentRecordDTO record = appointmentRecordRepository.findByAppointment_Id(appointmentId)
                .map(AppointmentRecord::toAppointmentRecordDTO)
                .orElseThrow(() -> new HmsException("APPOINTMENT_RECORD_NOT_FOUND"));

        record.setPrescription(prescriptionService.getPrescriptionByAppointmentId(appointmentId));
        enrichNamesWithBatching(List.of(record));

        return record;
    }

    @Override
    public List<AppointmentRecordDTO> getAppointmentRecordsByPatientId(Long patientId) throws HmsException {
        List<AppointmentRecordDTO> records = appointmentRecordRepository.findByAppointment_PatientId(patientId).stream()
                .map(AppointmentRecord::toAppointmentRecordDTO)
                .collect(Collectors.toList());
        enrichNamesWithBatching(records);
        return records;
    }

    @Override
    public boolean isAppointmentRecordExists(Long appointmentId) {
        return appointmentRecordRepository.findByAppointment_Id(appointmentId).isPresent();
    }

    @Override
    public List<AppointmentRecordDTO> getFollowUpsByPatientId(Long patientId) {
        List<AppointmentRecordDTO> records = appointmentRecordRepository.findByAppointment_PatientIdAndFollowUpDateAfter(patientId, LocalDate.now()).stream()
                .map(AppointmentRecord::toAppointmentRecordDTO)
                .collect(Collectors.toList());
        enrichNamesWithBatching(records);
        return records;
    }

    @Override
    public List<AppointmentRecordDTO> getFollowUpsByDoctorId(Long doctorId) {
        List<AppointmentRecordDTO> records = appointmentRecordRepository.findByAppointment_DoctorIdAndFollowUpDateAfter(doctorId, LocalDate.now()).stream()
                .map(AppointmentRecord::toAppointmentRecordDTO)
                .collect(Collectors.toList());
        enrichNamesWithBatching(records);
        return records;
    }

    private void enrichNamesWithBatching(List<AppointmentRecordDTO> records) {
        if (records == null || records.isEmpty()) return;

        // Extract unique IDs
        List<Long> doctorIds = records.stream()
                .map(AppointmentRecordDTO::getDoctorId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        List<Long> patientIds = records.stream()
                .map(AppointmentRecordDTO::getPatientId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        java.util.Map<Long, String> doctorMap = new java.util.HashMap<>();
        java.util.Map<Long, String> patientMap = new java.util.HashMap<>();

        // Batch fetch from ProfileMS (High Performance Pattern)
        try {
            if (!doctorIds.isEmpty()) {
                for (int i = 0; i < doctorIds.size(); i += 100) {
                    List<Long> chunk = doctorIds.subList(i, Math.min(i + 100, doctorIds.size()));
                    List<DoctorName> doctors = profileClient.getDoctorsById(chunk);
                    if (doctors != null) {
                        doctors.forEach(d -> {
                            if (d != null && d.getId() != null)
                                doctorMap.put(d.getId(), d.getName());
                        });
                    }
                }
            }
            if (!patientIds.isEmpty()) {
                for (int i = 0; i < patientIds.size(); i += 100) {
                    List<Long> chunk = patientIds.subList(i, Math.min(i + 100, patientIds.size()));
                    List<DoctorName> patients = profileClient.getPatientsById(chunk);
                    if (patients != null) {
                        patients.forEach(p -> {
                            if (p != null && p.getId() != null)
                                patientMap.put(p.getId(), p.getName());
                        });
                    }
                }
            }
        } catch (Exception ex) {
            org.slf4j.LoggerFactory.getLogger(AppointmentRecordServiceImpl.class)
                .error("[enrichNames] Inter-service call failed: {}", ex.getMessage());
        }

        // Final mapping
        for (AppointmentRecordDTO r : records) {
            String dName = doctorMap.get(r.getDoctorId());
            r.setDoctorName(dName != null ? dName : "Dr. ID: " + r.getDoctorId());

            String pName = patientMap.get(r.getPatientId());
            r.setPatientName(pName != null ? pName : "Patient ID: " + r.getPatientId());
        }
    }
}
