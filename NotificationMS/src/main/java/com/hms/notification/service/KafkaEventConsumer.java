package com.hms.notification.service;

import com.hms.notification.clients.UserClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Lazy(false)
@RequiredArgsConstructor
@Slf4j
public class KafkaEventConsumer {

    private final NotificationService notificationService;
    private final UserClient userClient;

    @KafkaListener(topics = "hms.user.registered", groupId = "user-events")
    public void handleUserRegistered(@Payload Map<String, Object> event) {
        try {
            log.info("[NOTIFICATION_TRACE] Step 4: Received 'hms.user.registered' event. Full event: {}", event);
            List<Long> recipientIds = extractRecipientIds(event);

            if (recipientIds.isEmpty()) {
                try {
                    log.warn("[NOTIFICATION_TRACE] Step 4.1: USER.REGISTERED event has no recipientIds. Falling back to fetch admin IDs from UserMS.");
                    recipientIds = userClient.getAdminIds();
                } catch (Exception ex) {
                    log.error("[NOTIFICATION_TRACE] CRITICAL_FAILURE: Could not fetch admin IDs for USER.REGISTERED fallback. Error: {}", ex.getMessage(), ex);
                    recipientIds = List.of();
                }

                if (recipientIds == null || recipientIds.isEmpty()) {
                    log.warn("[NOTIFICATION_TRACE] Step 4.2: No admin IDs resolved for USER.REGISTERED. Skipping notification.");
                    return;
                }
            }

            log.info("[NOTIFICATION_TRACE] Step 4.2: Event contains {} recipient IDs. Proceeding to create notifications.", recipientIds.size());

            Map<String, Object> payload = extractPayload(event);
            Object displayName = payload.containsKey("fullName") ? payload.get("fullName") : payload.get("name");
            Object userId = payload.get("userId");

            String message = String.format("New user registered: %s (ID: %s)", displayName, userId);
            String dedupeKey = "user-registered:" + userId;

            notificationService.createAndSendNotifications(recipientIds, "ADMIN", "USER_REGISTERED", "New User Registration", message, dedupeKey);
        } catch (Exception e) {
            log.error("[ADMIN_NOTIFICATION_TRACE] CRITICAL_FAILURE: Failed to process 'hms.user.registered' event. Error: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = {"hms.appointment.events", "hms.appointment.created", "hms.appointment.completed", "hms.appointment.cancelled"}, groupId = "appointment-events")
    public void handleAppointmentEvent(@Payload String eventStr) {
        try {
            log.info("[ADMIN_NOTIFICATION_TRACE] Step 3.9: Raw Kafka String Received: {}", eventStr);
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> event = mapper.readValue(eventStr, Map.class);
            log.info("[ADMIN_NOTIFICATION_TRACE] Step 4: Parsed appointment event. Full event: {}", event);
            List<Long> recipientIds = extractRecipientIds(event);
            if (recipientIds.isEmpty()) {
                log.warn("[ADMIN_NOTIFICATION_TRACE] Step 4.1: No recipient IDs found in appointment event. Skipping notification.");
                return;
            }
            log.info("[ADMIN_NOTIFICATION_TRACE] Step 4.2: Event contains {} recipient IDs. Proceeding to create notifications.", recipientIds.size());
            List<com.hms.notification.dto.RecipientDTO> recipients = new ArrayList<>();
            Map<String, Object> payload = extractPayload(event);
            String eventType = normalizeEventType(asString(event.get("eventType")));
            
            // Smarter Role Resolution for Appointment events if not explicitly passed
            Long patientId = coerceToLong(payload.get("patientId"));
            Long doctorId = coerceToLong(payload.get("doctorId"));

            for (Long rid : recipientIds) {
                String role = "ADMIN"; // Default
                if (rid.equals(patientId)) role = "PATIENT";
                else if (rid.equals(doctorId)) role = "DOCTOR";
                recipients.add(new com.hms.notification.dto.RecipientDTO(rid, role));
            }

            Object apptIdRaw = payload.get("appointmentId");
            if (apptIdRaw == null) apptIdRaw = payload.get("id");
            String apptId = asString(apptIdRaw);
            
            Object doctorNameRaw = payload.get("doctorName");
            String doctorName = (doctorNameRaw != null) ? asString(doctorNameRaw) : "ID " + asString(payload.get("doctorId"));

            String message;
            String title;
            String dedupeKey = "appointment:" + eventType.toLowerCase(Locale.ROOT) + ":" + apptId;

            if ("APPOINTMENT_CREATED".equals(eventType)) {
                title = "New Appointment Created";
                message = String.format("A new appointment (ID: %s) has been created for Dr. %s.",
                        apptId, doctorName);
            } else if ("APPOINTMENT_CANCELLED".equals(eventType)) {
                title = "Appointment Cancelled";
                message = String.format("Appointment (ID: %s) with Dr. %s has been cancelled.",
                        apptId, doctorName);
            } else if ("APPOINTMENT_COMPLETED".equals(eventType)) {
                title = "Appointment Completed";
                message = String.format("Appointment (ID: %s) with Dr. %s has been completed.",
                        apptId, doctorName);
            } else if ("APPOINTMENT_RESCHEDULED".equals(eventType)) {
                title = "Appointment Rescheduled";
                message = String.format("Appointment (ID: %s) with Dr. %s has been rescheduled to %s.",
                        apptId, doctorName, payload.get("appointmentTime"));
            } else if ("APPOINTMENT_OVERDUE".equals(eventType)) {
                title = "Appointment Overdue";
                message = String.format("Appointment (ID: %s) with Dr. %s is overdue.",
                        apptId, doctorName);
            } else {
                log.warn("[ADMIN_NOTIFICATION_TRACE] Unknown or unhandled appointment event type: '{}'. Event: {}", eventType, event);
                return;
            }

            log.info("[ADMIN_NOTIFICATION_TRACE] Step 4.3: Dispatching notification creation for type '{}' to {} recipients.", eventType, recipients.size());
            notificationService.createAndSendNotifications(recipients, eventType, title, message, dedupeKey);
        } catch (Exception e) {
            log.error("[ADMIN_NOTIFICATION_TRACE] CRITICAL_FAILURE: Failed to process appointment event. Error: {}", e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> extractPayload(Map<String, Object> event) {
        if (event == null) {
            return Map.of();
        }

        if (event.containsKey("payload")) {
            Object payload = event.get("payload");
            if (payload instanceof Map<?, ?> payloadMap) {
                return (Map<String, Object>) payloadMap;
            }
            return Map.of();
        }

        if (event.containsKey("userId") || event.containsKey("email") || event.containsKey("role")) {
            return event;
        }

        return Map.of();
    }

    private static List<Long> extractRecipientIds(Map<String, Object> event) {
        if (event == null) {
            return List.of();
        }

        Object ids = event.get("recipientIds");
        if (!(ids instanceof List<?> rawList)) {
            return List.of();
        }

        List<Long> out = new ArrayList<>(rawList.size());
        for (Object raw : rawList) {
            Long parsed = coerceToLong(raw);
            if (parsed != null) {
                out.add(parsed);
            }
        }
        return out;
    }

    private static Long coerceToLong(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof Long l) {
            return l;
        }
        if (raw instanceof Integer i) {
            return i.longValue();
        }
        if (raw instanceof Number n) {
            return n.longValue();
        }
        if (raw instanceof String s) {
            try {
                return Long.parseLong(s.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static String normalizeEventType(String eventType) {
        if (eventType == null) {
            return "";
        }
        String trimmed = eventType.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        return trimmed.toUpperCase(Locale.ROOT);
    }
}


