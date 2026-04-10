package com.hms.notification.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "notifications",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_notification_recipient_dedupe", columnNames = {"recipient_id", "dedupe_key"})
        },
        indexes = {
                @Index(name = "idx_notification_recipient_created", columnList = "recipient_id,created_at")
        }
)
@Data
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "attempts")
    private Integer attempts = 0;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Column(name = "channels")
    private String channels; // JSON type in DB, mapped as String for simplicity

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "dedupe_key", nullable = false, length = 255)
    private String dedupeKey;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "payload")
    private String payload; // JSON type in DB

    @Column(name = "priority")
    private String priority;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    @Column(name = "recipient_role", nullable = false)
    private String recipientRole;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @JsonProperty("isRead")
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false; // The real source of truth (0/1)

    @Column(name = "title", columnDefinition = "TEXT")
    private String title;

    @Column(name = "status", nullable = false)
    private String status = "UNREAD";

    @Column(name = "type", length = 100)
    private String type;
}
