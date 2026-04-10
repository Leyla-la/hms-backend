package com.hms.appointment.service;

import com.hms.appointment.clients.ProfileClient;
import com.hms.appointment.clients.UserClient;
import com.hms.appointment.dto.DoctorDTO;
import com.hms.appointment.dto.PatientDTO;
import com.hms.appointment.entity.Appointment;
import com.hms.appointment.notification.NotificationPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentEventListener {

    private final ProfileClient profileClient;
    private final UserClient userClient;
    private final NotificationPublisher notificationPublisher;

    @org.springframework.scheduling.annotation.Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @SuppressWarnings("unchecked")
    public void onAppointmentNotificationEvent(Map<String, Object> event) {
        if (event == null || !"appointment.notification".equals(String.valueOf(event.get("_event")))) {
            return;
        }

        String action = String.valueOf(event.get("action"));
        Object apptObj = event.get("appointment");
        Object actorObj = event.get("actor");

        if (!(apptObj instanceof Appointment) || !(actorObj instanceof Map)) {
            log.warn("Ignoring appointment.notification event due to unexpected types. appointment={}, actor={}",
                    apptObj == null ? "null" : apptObj.getClass().getName(),
                    actorObj == null ? "null" : actorObj.getClass().getName());
            return;
        }

        Appointment appointment = (Appointment) apptObj;
        Map<String, String> actor = (Map<String, String>) actorObj;

        String actorId = safeTrim(actor.get("id"));
        String actorName = safeTrim(actor.get("name"));
        String actorRole = normalizeRole(actor.get("role"));

        List<Long> recipients = computeRecipients(actorRole, actorId, appointment, resolveAdminRecipients());
        if (recipients.isEmpty()) {
            log.warn("No recipients computed for action={} appointmentId={} actorRole={} actorId={}", action,
                    appointment.getId(), actorRole, actorId);
            return;
        }

        String message = buildEnglishMessage(actorRole, actorName, action);
        String adminMessage = buildAdminEnglishMessage(actorRole, actorName, action, appointment);
        String topic = topicForAction(action);
        if (topic == null) {
            log.warn("Unsupported appointment notification action='{}'", action);
            return;
        }

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("appointmentId", appointment.getId());
            payload.put("patientId", appointment.getPatientId());
            payload.put("doctorId", appointment.getDoctorId());
            payload.put("appointmentTime",
                    appointment.getAppointmentTime() == null ? null : appointment.getAppointmentTime().toString());

            payload.put("actorId", actorId);
            payload.put("actorName", actorName);
            payload.put("actorRole", actorRole);

            payload.put("action", action);
            payload.put("message", message);
            payload.put("adminMessage", adminMessage);
            payload.put("recipients", recipients);

            log.info("Publishing appointment notification action={} appointmentId={} recipients={}", action,
                    appointment.getId(), recipients);

            notificationPublisher.publish(
                    topic,
                    String.valueOf(appointment.getId()),
                    payload,
                    eventTypeForAction(action),
                    "AppointmentMS",
                    recipients
            );
        } catch (Exception e) {
            log.error("Failed to publish appointment notification action={}: {}", action, e.getMessage(), e);
        }
    }

    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    private static String normalizeRole(String role) {
        String r = safeTrim(role).toUpperCase();
        if ("DOCTOR".equals(r) || "PATIENT".equals(r))
            return r;
        return r;
    }

    private List<Long> resolveAdminRecipients() {
        try {
            List<Long> adminIds = userClient.getAdminIds();
            if (adminIds != null && !adminIds.isEmpty()) {
                return adminIds;
            }
            log.warn("UserMS returned no admin ids for appointment notification fan-out. Falling back to admin id 9.");
        } catch (Exception e) {
            log.warn("Failed to resolve admin ids from UserMS for appointment notification fan-out. Falling back to admin id 9. Error={}", e.getMessage());
        }
        return List.of(9L);
    }

    private static List<Long> computeRecipients(String actorRole, String actorId, Appointment appointment,
            List<Long> adminRecipients) {
        List<Long> recipients = new ArrayList<>();

        for (Long adminId : adminRecipients) {
            addIfNotSelf(recipients, adminId, actorId);
        }

        if (appointment == null)
            return recipients;

        if ("DOCTOR".equals(actorRole)) {
            addIfNotSelf(recipients, appointment.getPatientId(), actorId);
        } else if ("PATIENT".equals(actorRole)) {
            addIfNotSelf(recipients, appointment.getDoctorId(), actorId);
        }

        return recipients;
    }

    private static void addIfNotSelf(List<Long> recipients, Long candidate, String actorId) {
        if (candidate == null)
            return;
        if (actorId != null && !actorId.isBlank()) {
            try {
                long actorLong = Long.parseLong(actorId);
                if (actorLong == candidate)
                    return;
            } catch (NumberFormatException ignored) {
                // if actorId is not numeric, we cannot compare reliably; still add recipient
            }
        }
        recipients.add(candidate);
    }

    private static String buildEnglishMessage(String actorRole, String actorName, String action) {
        String verb = normalizeActionVerb(action);
        String name = (actorName == null || actorName.isBlank()) ? "Unknown" : actorName;

        if ("DOCTOR".equals(actorRole)) {
            return "Doctor " + name + " has " + verb + " your appointment.";
        }
        if ("PATIENT".equals(actorRole)) {
            return "Patient " + name + " has " + verb + " the appointment.";
        }
        return name + " has " + verb + " the appointment.";
    }

    private String buildAdminEnglishMessage(String actorRole, String actorName, String action,
            Appointment appointment) {
        String verb = normalizeAdminActionVerb(action);

        String patientName = "Unknown";
        String doctorName = "Unknown";
        try {
            if (appointment != null && appointment.getPatientId() != null) {
                PatientDTO p = profileClient.getPatientById(appointment.getPatientId());
                if (p != null && p.getName() != null && !p.getName().isBlank())
                    patientName = p.getName();
            }
        } catch (Exception ignored) {
        }
        try {
            if (appointment != null && appointment.getDoctorId() != null) {
                DoctorDTO d = profileClient.getDoctorById(appointment.getDoctorId());
                if (d != null && d.getName() != null && !d.getName().isBlank())
                    doctorName = d.getName();
            }
        } catch (Exception ignored) {
        }

        // Admin(9) always sees the same format, regardless of actor
        return "Doctor " + doctorName + " " + verb + " appointment with Patient " + patientName + ".";
    }

    private static String normalizeAdminActionVerb(String action) {
        String a = safeTrim(action).toLowerCase();
        return switch (a) {
            case "created" -> "created";
            case "completed" -> "completed";
            case "cancelled", "canceled" -> "canceled";
            default -> a.isBlank() ? "updated" : a;
        };
    }

    private static String normalizeActionVerb(String action) {
        String a = safeTrim(action).toLowerCase();
        return switch (a) {
            case "created" -> "created";
            case "completed" -> "completed";
            case "cancelled" -> "cancelled";
            default -> a.isBlank() ? "updated" : a;
        };
    }

    private static String topicForAction(String action) {
        String a = safeTrim(action).toLowerCase();
        return switch (a) {
            case "created", "completed", "cancelled", "canceled" -> "hms.appointment.events";
            default -> null;
        };
    }

    private static String eventTypeForAction(String action) {
        String a = safeTrim(action).toLowerCase();
        return switch (a) {
            case "created" -> "APPOINTMENT_CREATED";
            case "completed" -> "APPOINTMENT_COMPLETED";
            case "cancelled", "canceled" -> "APPOINTMENT_CANCELLED";
            default -> a.toUpperCase();
        };
    }
}
