package com.hms.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final NotificationService notificationService;

    /**
     * Runs daily at 2:00 AM to clean up expired and old notifications.
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void scheduleNotificationCleanup() {
        log.info("[SCHEDULED_TASK] Starting daily notification cleanup...");
        long deletedCount = notificationService.clearOldNotifications();
        log.info("[SCHEDULED_TASK] Cleanup finished. Total removed: {}", deletedCount);
    }
}
