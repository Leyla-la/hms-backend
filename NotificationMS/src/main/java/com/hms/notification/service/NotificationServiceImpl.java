package com.hms.notification.service;

import com.hms.notification.entity.Notification;
import com.hms.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Deprecated
    public void sendNotification(Long recipientId, String recipientRole, String type, String title, String body, String dedupeKey, Map<String, Object> payload) {
        log.warn("Legacy method 'sendNotification' was called. This method is deprecated and should not be used for new features.");
    }

    @Override
    @Async
    @Transactional
    public void createAndSendNotifications(List<Long> recipientIds, String recipientRole, String type, String title, String message, String dedupeKeyPrefix) {
        if (recipientIds == null || recipientIds.isEmpty()) return;
        List<com.hms.notification.dto.RecipientDTO> recipients = recipientIds.stream()
                .map(id -> new com.hms.notification.dto.RecipientDTO(id, recipientRole))
                .toList();
        createAndSendNotifications(recipients, type, title, message, dedupeKeyPrefix);
    }

    @Override
    @Async
    @Transactional
    public void createAndSendNotifications(List<com.hms.notification.dto.RecipientDTO> recipients, String type, String title, String message, String dedupeKeyPrefix) {
        int recipientCount = (recipients == null) ? 0 : recipients.size();
        log.info("[ADMIN_NOTIFICATION_TRACE] Step 5: 'createAndSendNotifications' (Role-Aware) called for type '{}' with {} recipients.", type, recipientCount);

        if (recipients == null || recipients.isEmpty()) {
            log.info("[ADMIN_NOTIFICATION_TRACE] Step 5.0: Recipient list empty. Nothing to do.");
            return;
        }

        try {
            List<Notification> notificationsToSave = new ArrayList<>();

            for (com.hms.notification.dto.RecipientDTO recipient : recipients) {
                if (recipient == null || recipient.getId() == null) {
                    continue;
                }

                Long recipientId = recipient.getId();
                String recipientRole = recipient.getRole();
                String finalDedupeKey = dedupeKeyPrefix + ":" + recipientId;
                
                log.debug("[ADMIN_NOTIFICATION_TRACE] Step 5.1: Processing recipient ID {}. Checking for duplicate with key: {}", recipientId, finalDedupeKey);
                if (notificationRepository.findByRecipientIdAndDedupeKey(recipientId, finalDedupeKey).isPresent()) {
                    log.warn("[ADMIN_NOTIFICATION_TRACE] Step 5.2: Duplicate notification skipped for recipient {}. Key: {}", recipientId, finalDedupeKey);
                    continue;
                }

                Notification notification = new Notification();
                notification.setRecipientId(recipientId);
                notification.setRecipientRole(recipientRole != null ? recipientRole.toUpperCase() : "UNKNOWN");
                notification.setType(type);
                notification.setTitle(title);
                notification.setBody(message);
                notification.setIsRead(false);
                notification.setStatus("UNREAD"); 
                notification.setDedupeKey(finalDedupeKey);
                notification.setCreatedAt(LocalDateTime.now());
                notificationsToSave.add(notification);
                log.debug("[ADMIN_NOTIFICATION_TRACE] Step 5.3: Prepared notification for recipient {}: {}", recipientId, notification);
            }

            if (notificationsToSave.isEmpty()) {
                log.info("[ADMIN_NOTIFICATION_TRACE] Step 6: No new notifications to save (all were duplicates or list was empty).");
                return;
            }

            log.info("[ADMIN_NOTIFICATION_TRACE] Step 6: Saving {} new notifications to the database.", notificationsToSave.size());
            List<Notification> savedNotifications = notificationRepository.saveAll(notificationsToSave);
            log.info("[ADMIN_NOTIFICATION_TRACE] Step 7: Database save complete. Proceeding to send via WebSocket.");
            savedNotifications.forEach(this::sendToWebSocket);
        } catch (Exception e) {
            log.error("[ADMIN_NOTIFICATION_TRACE] CRITICAL_FAILURE: Failed to persist/broadcast notifications. type='{}', recipients={}, dedupeKeyPrefix='{}'. Error: {}",
                    type, recipientCount, dedupeKeyPrefix, e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public long clearOldNotifications() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysAgo = now.minusDays(30);
        
        log.info("Starting cleanup: Removing notifications expired before {} OR read and older than 30 days ({})", now, thirtyDaysAgo);
        
        try {
            int deletedCount = notificationRepository.deleteExpiredOrOldRead(now, thirtyDaysAgo);
            log.info("Cleanup successful. Removed {} notifications.", deletedCount);
            return (long) deletedCount;
        } catch (Exception e) {
            log.error("Failed to clear old notifications. Error: {}", e.getMessage(), e);
            return 0L;
        }
    }

    private void sendToWebSocket(Notification notification) {
        try {
            // Use convertAndSendToUser for targeted, secure delivery to a specific user's queue.
            String rolePart = (notification.getRecipientRole() != null) ? notification.getRecipientRole().toUpperCase() : "UNKNOWN";
            String destinationUser = rolePart + ":" + notification.getRecipientId();
            String destination = "/queue/notifications";
            log.info("[ADMIN_NOTIFICATION_TRACE] Step 8: Sending notification ID {} to user {} via WebSocket destination: {}", notification.getId(), destinationUser, destination);
            messagingTemplate.convertAndSendToUser(destinationUser, destination, notification);
            log.info("[ADMIN_NOTIFICATION_TRACE] Step 9: Successfully sent notification ID {} to user {}.", notification.getId(), destinationUser);
        } catch (Exception e) {
            log.error("[ADMIN_NOTIFICATION_TRACE] CRITICAL_FAILURE: Failed to send notification ID {} via WebSocket. Error: {}", notification.getId(), e.getMessage(), e);
        }
    }

}
