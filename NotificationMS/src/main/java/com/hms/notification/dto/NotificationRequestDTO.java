package com.hms.notification.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequestDTO {
    private Long recipientId;
    private String recipientRole;
    private String type;
    private String title;
    private String body;
    private String payload;
    private String channels;
    private String priority;
}
