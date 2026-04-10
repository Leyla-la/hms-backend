package com.hms.notification.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service interface for handling notifications.
 */
public interface NotificationService {

    /**
     * @deprecated This method is for backward compatibility.
     * Use {@link #createAndSendNotifications(List, String, String, String, String)} instead.
     */
    @Deprecated
    void sendNotification(Long recipientId, String recipientRole, String type, String title, String body, String dedupeKey, Map<String, Object> payload);

    /**
     * Creates and sends notifications to a list of recipients. This is the primary method for the new event-driven architecture.
     *
     * @param recipientIds    List of user IDs to receive the notification.
     * @param recipientRole   Common role for all recipients (if applicable).
     * @param type            The type of the notification (e.g., "USER_REGISTERED").
     * @param title           The title of the notification.
     * @param message         The main content of the notification.
     * @param dedupeKeyPrefix A prefix for the deduplication key to prevent duplicate notifications.
     */
    void createAndSendNotifications(List<Long> recipientIds, String recipientRole, String type, String title, String message, String dedupeKeyPrefix);

    /**
     * Creates and sends notifications to a list of recipients with specific roles.
     */
    void createAndSendNotifications(List<com.hms.notification.dto.RecipientDTO> recipients, String type, String title, String message, String dedupeKeyPrefix);

    /**
     * Deletes notifications that are older than a specified period (e.g., one week).
     *
     * @return The number of notifications that were deleted.
     */
    long clearOldNotifications();
}
