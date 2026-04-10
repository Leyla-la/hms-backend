package com.hms.appointment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hms.appointment.clients.ProfileClient;
import com.hms.appointment.clients.UserClient;
import com.hms.appointment.dto.*;
import com.hms.appointment.entity.Appointment;
import com.hms.appointment.exception.HmsException;
import com.hms.appointment.notification.NotificationPublisher;
import com.hms.appointment.repository.AppointmentRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {
    AppointmentRepository appointmentRepository;
    ApiService apiService;
    ProfileClient profileClient;
    UserClient userClient; // Thêm UserClient
    KafkaTemplate<String, String> kafkaTemplate;
    NotificationPublisher notificationPublisher;
    org.springframework.context.ApplicationEventPublisher applicationEventPublisher;
    ObjectMapper objectMapper = new ObjectMapper();

    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 300000) // 5 minutes
    public void scanOverdueAppointments() {
        LocalDateTime now = LocalDateTime.now();
        List<Appointment> overdue = appointmentRepository.findAllByStatusAndAppointmentTimeBefore(Status.SCHEDULED, now);
        if (overdue == null || overdue.isEmpty()) return;
        for (Appointment a : overdue) {
            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("id", a.getId());
                payload.put("time", a.getAppointmentTime().toString());
                
                // Fetch admins to include in overdue notifications
                List<Long> adminIds = resolveAdminIds();
                List<Long> recipients = new ArrayList<>(adminIds);
                if (a.getPatientId() != null) recipients.add(a.getPatientId());
                if (a.getDoctorId() != null) recipients.add(a.getDoctorId());

                // Use centralized publisher to ensure wrapper DTO is used correctly
                notificationPublisher.publish(
                    "hms.appointment.overdue", 
                    String.valueOf(a.getId()), 
                    payload, 
                    "appointment.overdue", 
                    "AppointmentMS", 
                    recipients
                );
                log.info("Published overdue event via publisher for Appointment ID: {} to {} recipients", a.getId(), recipients.size());
            } catch (Exception e) { 
                log.error("Failed to publish overdue event: {}", e.getMessage()); 
            }
        }
    }


    @Override
    @Transactional
    public Long scheduleAppointment(AppointmentDTO appointmentDTO, Map<String, String> actor) throws HmsException {
        Boolean doctorExists = profileClient.doctorExists(appointmentDTO.getDoctorId());
        if (doctorExists == null || !doctorExists) {
            throw new HmsException("DOCTOR_NOT_FOUND");
        }
        Boolean patientExists = profileClient.patientExists(appointmentDTO.getPatientId());
        if (patientExists == null || !patientExists) {
            throw new HmsException("PATIENT_NOT_FOUND");
        }
        Appointment appt = appointmentDTO.toAppointment();
        if (appt.getStatus() == null) {
            appt.setStatus(Status.SCHEDULED);
        }
        Appointment saved = appointmentRepository.save(appt);

        // Resolve recipients: Patient, Doctor, and Admins
        List<Long> recipients = new ArrayList<>();
        if (saved.getPatientId() != null) recipients.add(saved.getPatientId());
        if (saved.getDoctorId() != null) recipients.add(saved.getDoctorId());
        try {
            List<Long> adminIds = resolveAdminIds();
            if (adminIds != null) recipients.addAll(adminIds);
        } catch (Exception e) {
            log.warn("Failed to resolve admin IDs for appointment notification: {}", e.getMessage());
        }

        notificationPublisher.publish(
            "hms.appointment.events", 
            String.valueOf(saved.getId()), 
            saved.toAppointmentDTO(), 
            "APPOINTMENT_CREATED", 
            "AppointmentMS", 
            recipients
        );

        publishAppointmentNotificationEvent("created", saved, actor);
        
        return saved.getId();
    }

    @Override
    @Transactional
    public void cancelAppointment(Long appointmentId, Map<String, String> actor) throws HmsException{
        Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow(() -> new HmsException("APPOINTMENT_NOT_FOUND"));
        if (appointment.getStatus().equals(Status.CANCELLED)) {
            throw new HmsException("APPOINTMENT_ALREADY_CANCELED");
        } else if (appointment.getStatus().equals(Status.COMPLETED)) {
            throw new HmsException("APPOINTMENT_ALREADY_COMPLETED");
        }

        appointment.setStatus(Status.CANCELLED);
        Appointment saved = appointmentRepository.save(appointment);

        // Resolve recipients
        List<Long> recipients = new ArrayList<>();
        if (saved.getPatientId() != null) recipients.add(saved.getPatientId());
        if (saved.getDoctorId() != null) recipients.add(saved.getDoctorId());
        try {
            List<Long> adminIds = resolveAdminIds();
            if (adminIds != null) recipients.addAll(adminIds);
        } catch (Exception e) {
            log.warn("Failed to resolve admin IDs for appointment cancellation: {}", e.getMessage());
        }

        notificationPublisher.publish(
            "hms.appointment.events", 
            String.valueOf(saved.getId()), 
            saved.toAppointmentDTO(), 
            "APPOINTMENT_CANCELLED", 
            "AppointmentMS", 
            recipients
        );

        publishAppointmentNotificationEvent("cancelled", appointment, actor);
    }

    @Override
    @Transactional
    public void completeAppointment(Long appointmentId, Map<String, String> actor) throws HmsException {
        Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow(() -> new HmsException("APPOINTMENT_NOT_FOUND"));
        if (appointment.getStatus().equals(Status.COMPLETED)) {
            throw new HmsException("APPOINTMENT_ALREADY_COMPLETED");
        }
        appointment.setStatus(Status.COMPLETED);
        Appointment saved = appointmentRepository.save(appointment);

        // Resolve recipients
        List<Long> recipients = new ArrayList<>();
        if (saved.getPatientId() != null) recipients.add(saved.getPatientId());
        if (saved.getDoctorId() != null) recipients.add(saved.getDoctorId());
        try {
            List<Long> adminIds = resolveAdminIds();
            if (adminIds != null) recipients.addAll(adminIds);
        } catch (Exception e) {
            log.warn("Failed to resolve admin IDs for appointment completion: {}", e.getMessage());
        }

        notificationPublisher.publish(
            "hms.appointment.events", 
            String.valueOf(saved.getId()), 
            saved.toAppointmentDTO(), 
            "APPOINTMENT_COMPLETED", 
            "AppointmentMS", 
            recipients
        );

        publishAppointmentNotificationEvent("completed", saved, actor);
    }

    private void publishAppointmentNotificationEvent(String action, Appointment appointment, Map<String, String> actor) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("_event", "appointment.notification");
            event.put("action", action);
            event.put("appointment", appointment);
            event.put("actor", actor);
            applicationEventPublisher.publishEvent(event);
        } catch (Exception e) {
            log.error("Failed to publish local appointment notification event action={}: {}", action, e.getMessage(), e);
        }
    }

    @Override
    public void rescheduleAppointment(Long appointmentId, String newAppointmentDate) throws HmsException {
        Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow(() -> new HmsException("APPOINTMENT_NOT_FOUND"));
        LocalDateTime oldTime = appointment.getAppointmentTime();
        LocalDateTime newTime = null;
        try {
            // try ISO parse first, then fallback to common formatter
            try {
                newTime = LocalDateTime.parse(newAppointmentDate);
            } catch (Exception ex) {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                newTime = LocalDateTime.parse(newAppointmentDate, fmt);
            }
        } catch (Exception e) {
            throw new HmsException("INVALID_DATE_FORMAT");
        }

        appointment.setAppointmentTime(newTime);
        appointment.setStatus(Status.SCHEDULED);
        Appointment saved = appointmentRepository.save(appointment);

        // publish rescheduled event
        // This part still needs the actor information.
        // For now, I will leave it as is and focus on the main tasks.
        try {
            DoctorDTO doctor = null;
            PatientDTO patient = null;
            try { doctor = profileClient.getDoctorById(saved.getDoctorId()); } catch (Exception ignored) {}
            try { patient = profileClient.getPatientById(saved.getPatientId()); } catch (Exception ignored) {}

            Map<String, Object> appt = new HashMap<>();
            appt.put("id", saved.getId());
            appt.put("patientId", saved.getPatientId());
            appt.put("patientName", patient == null ? null : patient.getName());
            appt.put("patientEmail", patient == null ? null : patient.getEmail());
            appt.put("doctorId", saved.getDoctorId());
            appt.put("doctorName", doctor == null ? null : doctor.getName());
            appt.put("oldAppointmentTime", oldTime == null ? null : oldTime.toString());
            appt.put("newAppointmentTime", newTime.toString());
            appt.put("status", saved.getStatus() == null ? null : saved.getStatus().name());
            appt.put("reason", saved.getReason());

            // Actor info is missing here. This needs to be addressed later.

            notificationPublisher.publish("hms.appointment.rescheduled", String.valueOf(saved.getId()), Collections.singletonMap("appointment", appt), "appointment.rescheduled", "AppointmentMS", List.of(saved.getPatientId(), saved.getDoctorId()));
        } catch (Exception e) {
            System.err.println("Failed to publish hms.appointment.rescheduled: " + e.getMessage());
        }
    }

    @Override
    public AppointmentDTO getAppointmentDetails(Long appointmentId) throws HmsException {
        return appointmentRepository.findById(appointmentId).orElseThrow(() -> new HmsException("APPOINTMENT_NOT_FOUND")).toAppointmentDTO();
    }

    @Override
    public AppointmentDetails getAppointmentDetailsWithName(Long appointmentId) throws HmsException {
        AppointmentDTO appointmentDTO = appointmentRepository.findById(appointmentId).orElseThrow(() -> new HmsException("APPOINTMENT_NOT_FOUND")).toAppointmentDTO();
        DoctorDTO doctorDTO = profileClient.getDoctorById(appointmentDTO.getDoctorId());
        PatientDTO patientDTO = profileClient.getPatientById(appointmentDTO.getPatientId());
        
        return AppointmentDetails.builder()
                .id(appointmentDTO.getId())
                .patientId(appointmentDTO.getPatientId())
                .patientName(patientDTO.getName())
                .patientEmail(patientDTO.getEmail())
                .patientPhone(patientDTO.getPhone())
                .patientCitizenId(patientDTO.getCitizenId())
                .doctorId(appointmentDTO.getDoctorId())
                .doctorName(doctorDTO.getName())
                .doctorEmail(doctorDTO.getEmail())
                .doctorPhone(doctorDTO.getPhone())
                .doctorSpecialization(doctorDTO.getSpecialization())
                .doctorLicenseNo(doctorDTO.getLicenseNo())
                .appointmentTime(appointmentDTO.getAppointmentTime())
                .status(appointmentDTO.getStatus())
                .reason(appointmentDTO.getReason())
                .notes(appointmentDTO.getNotes())
                .build();
    }

    @Override
    public List<AppointmentDTO> getAppointmentsByPatientId(Long patientId) throws HmsException {
        List<Appointment> appointments = appointmentRepository.findByPatientId(patientId);
        if (appointments.isEmpty()) return List.of();
        
        List<AppointmentDTO> appointmentDTOs = appointments.stream()
                .map(Appointment::toAppointmentDTO)
                .collect(Collectors.toList());
        
        enrichNames(appointmentDTOs);
        return appointmentDTOs;
    }

    @Override
    public List<AppointmentDTO> getMedicalHistoryByPatientId(Long patientId) throws HmsException {
        List<Appointment> appointments = appointmentRepository.findByPatientIdAndStatusOrderByAppointmentTimeDesc(patientId, Status.COMPLETED);
        if (appointments.isEmpty()) return List.of();
        
        List<AppointmentDTO> appointmentDTOs = appointments.stream()
                .map(Appointment::toAppointmentDTO)
                .collect(Collectors.toList());
        
        enrichNames(appointmentDTOs);
        return appointmentDTOs;
    }

    @Override
    public List<AppointmentDetails> getAllAppointmentsByPatientId(Long patientId) throws HmsException {
        return appointmentRepository.findAllByPatientId(patientId).stream()
                .map(appointment -> {
                    try {
                        DoctorDTO doctorDTO = profileClient.getDoctorById(appointment.getDoctorId());
                        if (doctorDTO != null) {
                            appointment.setDoctorName(doctorDTO.getName());
                        }
                    } catch (Exception e) {
                        appointment.setDoctorName("Unknown Doctor");
                    }
                    return appointment;
                }).toList();
    }

    @Override
    public List<AppointmentDetails> getAllAppointmentsByDoctorId(Long doctorId) throws HmsException {
        return appointmentRepository.findAllByDoctorId(doctorId).stream()
                .map(appointment -> {
                    PatientDTO patientDTO = profileClient.getPatientById(appointment.getPatientId());
                    appointment.setPatientName(patientDTO.getName());
                    appointment.setPatientEmail(patientDTO.getEmail());
                    appointment.setPatientPhone(patientDTO.getPhone());
                    return appointment;
                }).toList();
    }

    @Override
    public List<AppointmentDTO> getAppointmentsByDoctorId(Long doctorId) throws HmsException {
        List<Appointment> appointments = appointmentRepository.findByDoctorId(doctorId);
        if (appointments == null || appointments.isEmpty()) return List.of();
        List<AppointmentDTO> dtos = appointments.stream().map(Appointment::toAppointmentDTO).collect(Collectors.toList());
        enrichNames(dtos);
        return dtos;
    }

    @Override
    public AppointmentSummaryDTO getSummaryForDoctor(Long doctorId) throws HmsException {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        // counts by status for ALL records (Admin view)
        List<Object[]> rows = appointmentRepository.countByStatusAll();
        long scheduled = 0, completed = 0, cancelled = 0, totalToday = 0;
        
        log.info("[Dashboard] All-time row counts: {}", rows.size());
        for (Object[] r : rows) {
            if (r == null || r.length < 2) continue;
            String s = String.valueOf(r[0]);
            long c = ((Number) r[1]).longValue();
            
            if (s.contains("SCHEDULED") || s.equals("0")) scheduled += c;
            else if (s.contains("COMPLETED") || s.equals("1")) completed += c;
            else if (s.contains("CANCELLED") || s.equals("2")) cancelled += c;
        }
        
        // count Today specifics
        totalToday = appointmentRepository.findTodayAll(start, end).size();

        long totalAll = appointmentRepository.count();
        long overdue = appointmentRepository.findAllByStatusAndAppointmentTimeBefore(Status.SCHEDULED, LocalDateTime.now()).size();
        
        log.info("[Dashboard] Final Aggregation - Total: {}, Today: {}, Scheduled: {}, Completed: {}, Cancelled: {}, Overdue: {}", 
                 totalAll, totalToday, scheduled, completed, cancelled, overdue);
                 
        return new AppointmentSummaryDTO(totalAll, totalToday, scheduled, completed, cancelled, overdue);
    }

    @Override
    public List<AppointmentDTO> getUpcomingForPatient(Long patientId, int limit) throws HmsException {
        LocalDateTime now = LocalDateTime.now();
        List<Appointment> list = appointmentRepository.findUpcomingByPatient(patientId, now);
        if (list == null) return Collections.emptyList();
        return list.stream().limit(limit).map(Appointment::toAppointmentDTO).toList();
    }

    @Override
    public List<AppointmentDTO> getTodayForDoctor(Long doctorId) throws HmsException {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        List<Appointment> list = appointmentRepository.findTodayByDoctor(doctorId, start, end);
        if (list == null) return Collections.emptyList();
        List<AppointmentDTO> dtos = list.stream().map(Appointment::toAppointmentDTO).toList();
        log.info("[DB-TRUTH] Today apps for doctor {}: {}", doctorId, dtos.size());
        return dtos;
    }

    @Override
    public List<AppointmentDTO> getTodayAll() throws HmsException {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        log.info("[Dashboard] Fetching all today appointments: {} -> {}", start, end);
        List<Appointment> list = appointmentRepository.findTodayAll(start, end);
        if (list == null) return Collections.emptyList();
        log.info("[DB-TRUTH] Today ALL apps: {}", list.size());
        return list.stream().map(Appointment::toAppointmentDTO).toList();
    }

    @Override
    public TrendDTO getTrend(LocalDate from, LocalDate to) throws HmsException {
        LocalDateTime fromDt = from.atStartOfDay();
        LocalDateTime toDt = to.plusDays(1).atStartOfDay();
        List<Object[]> rows = appointmentRepository.countGroupByDate(fromDt, toDt);
        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (Object[] r : rows) {
            if (r == null || r.length < 2) continue;
            Object day = r[0];
            Number cnt = (Number) r[1];
            String label = day == null ? "" : day.toString();
            labels.add(label);
            values.add(cnt == null ? 0L : cnt.longValue());
        }
        return new TrendDTO(labels, values);
    }

    @Override
    public TrendDTO getTrendForPatient(Long patientId, LocalDate from, LocalDate to) throws HmsException {
        LocalDateTime fromDt = from.atStartOfDay();
        LocalDateTime toDt = to.plusDays(1).atStartOfDay();
        List<Object[]> rows = appointmentRepository.countGroupByDateForPatient(patientId, fromDt, toDt);
        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();
        for (Object[] r : rows) {
            if (r == null || r.length < 2) continue;
            Object day = r[0];
            Number cnt = (Number) r[1];
            String label = day == null ? "" : day.toString();
            labels.add(label);
            values.add(cnt == null ? 0L : cnt.longValue());
        }
        return new TrendDTO(labels, values);
    }

    @Override
    public List<AppointmentDetails> getUpcomingWithDetailsForPatient(Long patientId, int limit) throws HmsException {
        LocalDateTime now = LocalDateTime.now();
        List<Appointment> list = appointmentRepository.findUpcomingByPatient(patientId, now);
        if (list == null) return Collections.emptyList();
        return list.stream().limit(limit).map(a -> {
            String doctorName = null;
            String specialization = null;
            try {
                DoctorDTO doc = profileClient.getDoctorById(a.getDoctorId());
                if (doc != null) {
                    doctorName = doc.getName();
                    specialization = doc.getSpecialization();
                }
            } catch (Exception ignored) {}
            return new AppointmentDetails(
                    a.getId(), a.getPatientId(), null, null, null, null,
                    a.getDoctorId(), doctorName, null, null, specialization, null,
                    a.getAppointmentTime(), a.getStatus(), a.getReason(), a.getNotes()
            );
        }).toList();
    }

    @Override
    public long count() {
        return appointmentRepository.count();
    }

    private List<Long> resolveAdminIds() {
        try {
            List<Long> adminIds = userClient.getAdminIds();
            if (adminIds != null && !adminIds.isEmpty()) {
                return adminIds;
            }
        } catch (Exception e) {
            log.warn("Failed to resolve admin ids from UserMS. Falling back to default admin ID 9. Error={}", e.getMessage());
        }
        return List.of(9L);
    }

    private void enrichNames(List<AppointmentDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) return;

        List<Long> doctorIds = dtos.stream()
                .map(AppointmentDTO::getDoctorId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        List<Long> patientIds = dtos.stream()
                .map(AppointmentDTO::getPatientId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> doctorMap = new HashMap<>();
        Map<Long, String> patientMap = new HashMap<>();

        try {
            if (!doctorIds.isEmpty()) {
                for (int i = 0; i < doctorIds.size(); i += 50) {
                    List<Long> chunk = doctorIds.subList(i, Math.min(i + 50, doctorIds.size()));
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
                for (int i = 0; i < patientIds.size(); i += 50) {
                    List<Long> chunk = patientIds.subList(i, Math.min(i + 50, patientIds.size()));
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

        for (AppointmentDTO d : dtos) {
            String dName = doctorMap.get(d.getDoctorId());
            d.setDoctorName(dName != null ? dName : "Dr. ID: " + d.getDoctorId());
            String pName = patientMap.get(d.getPatientId());
            d.setPatientName(pName != null ? pName : "Patient ID: " + d.getPatientId());
        }
    }
}

