package com.hms.appointment.service;

import com.hms.appointment.clients.ProfileClient;
import com.hms.appointment.dto.PrescriptionDTO;
import com.hms.appointment.dto.PrescriptionDetails;
import com.hms.appointment.entity.Prescription;
import com.hms.appointment.dto.DoctorName;
import com.hms.appointment.dto.MedicineDTO;
import com.hms.appointment.entity.Medicine;
import com.hms.appointment.exception.HmsException;
import com.hms.appointment.notification.NotificationPublisher;
import com.hms.appointment.repository.PrescriptionRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PrescriptionServiceImpl implements PrescriptionService {
    PrescriptionRepository prescriptionRepository;
    MedicineService medicineService;
    ProfileClient profileClient;
    NotificationPublisher notificationPublisher;

    private static final Logger log = LoggerFactory.getLogger(PrescriptionServiceImpl.class);

    @Override
    public Long savePrescription(PrescriptionDTO prescriptionDTO) throws HmsException {
        // Build prescription entity from DTO (medicines will be converted to Medicine
        // entities with null prescription reference)
        Prescription pres = prescriptionDTO.toPrescription();

        // Ensure back-reference is set on each child Medicine so that cascade persist
        // works correctly
        if (pres.getMedicines() != null) {
            for (Medicine m : pres.getMedicines()) {
                m.setPrescription(pres);
            }
        }

        Long prescriptionId = prescriptionRepository.save(pres).getId();
        prescriptionDTO.setId(prescriptionId);

        // publish prescription.created event for notifications
        try {
            Map<String, Object> payload = Map.of("prescription", Map.of(
                    "id", prescriptionId,
                    "appointmentId", pres.getAppointment() == null ? null : pres.getAppointment().getId(),
                    "patientId", pres.getPatientId(),
                    "doctorId", pres.getDoctorId(),
                    "prescriptionDate", pres.getPrescriptionDate(),
                    "medicines", prescriptionDTO.getMedicines()));
            notificationPublisher.publish("hms.prescription.created", String.valueOf(prescriptionId), payload,
                    "prescription.created", "AppointmentMS", List.of(pres.getPatientId(), pres.getDoctorId()));
        } catch (Exception e) {
            log.error("Failed to publish prescription.created: {}", e.getMessage(), e);
        }

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

    @Override
    public List<PrescriptionDetails> getPrescriptionsByPatientId(Long patientId) throws HmsException {
        List<Prescription> prescriptions = prescriptionRepository.findAllByPatientId(patientId);
        if (prescriptions == null || prescriptions.isEmpty())
            return List.of();

        List<PrescriptionDetails> prescriptionDetails = prescriptions.stream()
                .map(Prescription::toPrescriptionDetails)
                .toList();

        // Batch load medicines for all prescriptions
        List<Long> prescriptionIds = prescriptionDetails.stream().map(PrescriptionDetails::getId).toList();
        List<MedicineDTO> allMedicines = medicineService.getMedicinesByPrescriptionIds(prescriptionIds);
        Map<Long, List<MedicineDTO>> medicineMap = allMedicines.stream()
                .collect(Collectors.groupingBy(MedicineDTO::getPrescriptionId));

        prescriptionDetails
                .forEach(details -> details.setMedicines(medicineMap.getOrDefault(details.getId(), List.of())));

        // Batch load doctor and patient names
        enrichNamesWithBatching(prescriptionDetails);

        return prescriptionDetails;
    }

    @Override
    public List<PrescriptionDetails> getPrescriptionsByDoctorId(Long doctorId) throws HmsException {
        List<Prescription> prescriptions = prescriptionRepository.findAllByDoctorId(doctorId);
        if (prescriptions == null || prescriptions.isEmpty())
            return List.of();

        List<PrescriptionDetails> prescriptionDetails = prescriptions.stream()
                .map(Prescription::toPrescriptionDetails)
                .toList();

        // Batch load medicines
        List<Long> prescriptionIds = prescriptionDetails.stream().map(PrescriptionDetails::getId).toList();
        List<MedicineDTO> allMedicines = medicineService.getMedicinesByPrescriptionIds(prescriptionIds);
        Map<Long, List<MedicineDTO>> medicineMap = allMedicines.stream()
                .collect(Collectors.groupingBy(MedicineDTO::getPrescriptionId));

        prescriptionDetails
                .forEach(details -> details.setMedicines(medicineMap.getOrDefault(details.getId(), List.of())));

        // Batch load doctor and patient names
        enrichNamesWithBatching(prescriptionDetails);

        return prescriptionDetails;
    }

    @Override
    public Page<PrescriptionDetails> getPrescriptions(Pageable pageable) throws HmsException {
        log.debug("[getPrescriptions] Fetching paged prescriptions: {}", pageable);
        Page<Prescription> prescriptionPage = prescriptionRepository.findAll(pageable);
        if (prescriptionPage.isEmpty()) {
            return Page.empty(pageable);
        }

        List<PrescriptionDetails> prescriptionDetails = prescriptionPage.getContent().stream()
                .map(Prescription::toPrescriptionDetails)
                .collect(Collectors.toList());

        enrichNamesWithBatching(prescriptionDetails);

        return new PageImpl<>(prescriptionDetails, pageable, prescriptionPage.getTotalElements());
    }

    @Override
    public List<PrescriptionDetails> getAllPrescriptionsList() throws HmsException {
        log.info("[getAllPrescriptionsList] Fetching ALL prescriptions (optimized)");
        List<Prescription> prescriptions = prescriptionRepository.findAllOptimized();
        if (prescriptions == null || prescriptions.isEmpty())
            return List.of();

        List<PrescriptionDetails> prescriptionDetails = prescriptions.stream()
                .map(Prescription::toPrescriptionDetails)
                .collect(Collectors.toList());

        // Batch fetch names from ProfileMS
        enrichNamesWithBatching(prescriptionDetails);

        // Batch fetch medicines (Crucial for Sales Import)
        List<Long> prescriptionIds = prescriptionDetails.stream().map(PrescriptionDetails::getId).toList();
        List<MedicineDTO> allMedicines = medicineService.getMedicinesByPrescriptionIds(prescriptionIds);
        Map<Long, List<MedicineDTO>> medicineMap = allMedicines.stream()
                .collect(Collectors.groupingBy(MedicineDTO::getPrescriptionId));

        prescriptionDetails.forEach(details ->
                details.setMedicines(medicineMap.getOrDefault(details.getId(), List.of())));

        return prescriptionDetails;
    }

    private void enrichNamesWithBatching(List<PrescriptionDetails> prescriptionDetails) {
        // Extract unique IDs
        List<Long> doctorIds = prescriptionDetails.stream()
                .map(PrescriptionDetails::getDoctorId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        List<Long> patientIds = prescriptionDetails.stream()
                .map(PrescriptionDetails::getPatientId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, String> doctorMap = new java.util.HashMap<>();
        Map<Long, String> patientMap = new java.util.HashMap<>();

        // Batch fetch from ProfileMS (High Performance)
        try {
            if (!doctorIds.isEmpty()) {
                // Chunking to avoid URL length issues if too many IDs
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
            log.error("[enrichNames] Inter-service call failed: {}", ex.getMessage());
        }

        // Final mapping
        for (PrescriptionDetails details : prescriptionDetails) {
            String dName = doctorMap.get(details.getDoctorId());
            details.setDoctorName(dName != null ? dName : "Dr. ID: " + details.getDoctorId());

            String pName = patientMap.get(details.getPatientId());
            details.setPatientName(pName != null ? pName : "Patient ID: " + details.getPatientId());
        }
    }
}
