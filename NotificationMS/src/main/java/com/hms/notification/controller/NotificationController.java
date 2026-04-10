package com.hms.notification.controller;

import com.hms.notification.entity.Notification;
import com.hms.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "User Notifications", description = "Endpoints for retrieving and managing user-specific and role-based notifications.")
public class NotificationController {
    private final NotificationRepository notificationRepository;

    @Operation(summary = "List notifications", description = "Retrieves a paginated list of notifications for a user, filtered by their role.")
    @GetMapping
    public ResponseEntity<Page<Notification>> list(@RequestParam("recipientId") Long recipientId,
                                                   @RequestHeader(value = "X-User-Role", required = false) String role,
                                                   @RequestParam(value = "page", defaultValue = "0") int page,
                                                   @RequestParam(value = "size", defaultValue = "20") int size) {
        Page<Notification> items;
        String finalRole = (role != null) ? role.toUpperCase() : "UNKNOWN";
        
        if ("ADMIN".equalsIgnoreCase(finalRole)) {
            // Admins see their specific notifications PLUS everything tagged system-wide for ADMINS
            items = notificationRepository.findByRecipientIdOrAdminRole(recipientId, PageRequest.of(page, size));
        } else {
            // Other roles see only their specific notifications (recipientId matches Patient/Doctor profileId)
            items = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId, PageRequest.of(page, size));
        }
        return ResponseEntity.ok(items);
    }

    @Operation(summary = "Mark as read", description = "Marks a specific notification as 'read' by its ID.")
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable("id") Long id) {
        notificationRepository.markAsReadById(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Mark all as read", description = "Marks all currently unread notifications for a specific user and role as 'read'.")
    @PatchMapping("/mark-all-read")
    public ResponseEntity<Void> markAllRead(@RequestParam("recipientId") Long recipientId,
                                            @RequestHeader(value = "X-User-Role", required = false) String role) {
        String finalRole = (role != null) ? role.toUpperCase() : "UNKNOWN";
        notificationRepository.markAllAsRead(recipientId, finalRole);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get notifications by role", description = "Retrieves a paginated list of notifications filtered by a specific recipient role.")
    @GetMapping("/role/{role}")
    public ResponseEntity<Page<Notification>> getByRole(@PathVariable("role") String role,
                                                        @RequestParam("recipientId") Long recipientId,
                                                        @RequestParam(value = "page", defaultValue = "0") int page,
                                                        @RequestParam(value = "size", defaultValue = "10") int size) {
        Page<Notification> items = notificationRepository.findByRecipientIdAndRecipientRoleOrderByCreatedAtDesc(
                recipientId, role, PageRequest.of(page, size));
        return ResponseEntity.ok(items);
    }

    private final com.hms.notification.service.NotificationService service;

}

