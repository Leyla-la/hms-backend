package com.hms.notification.repository;

import com.hms.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId OR n.recipientRole = 'ADMIN' ORDER BY n.createdAt DESC")
    Page<Notification> findByRecipientIdOrAdminRole(@Param("recipientId") Long recipientId, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId OR n.recipientRole = :role ORDER BY n.createdAt DESC")
    Page<Notification> findByRecipientIdOrRecipientRole(@Param("recipientId") Long recipientId, @Param("role") String role, Pageable pageable);

    Page<Notification> findByRecipientIdAndRecipientRoleOrderByCreatedAtDesc(Long recipientId, String recipientRole, Pageable pageable);

    Optional<Notification> findByRecipientIdAndDedupeKey(Long recipientId, String dedupeKey);

    @Modifying
    @Transactional
    @Query("update Notification n set n.isRead = true, n.readAt = CURRENT_TIMESTAMP, n.status = 'READ' where n.id = :id")
    void markAsReadById(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("update Notification n set n.isRead = true, n.readAt = CURRENT_TIMESTAMP, n.status = 'READ' where (n.recipientId = :recipientId OR n.recipientRole = :role) AND n.isRead = false")
    void markAllAsRead(@Param("recipientId") Long recipientId, @Param("role") String role);

    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.expiresAt < :now OR (n.isRead = true AND n.createdAt < :thirtyDaysAgo)")
    int deleteExpiredOrOldRead(@Param("now") LocalDateTime now, @Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo);

    long deleteByIsReadAndCreatedAtBefore(Boolean isRead, LocalDateTime createdAt);
}
