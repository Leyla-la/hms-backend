package com.hms.notification.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/**
 * Robust DTO designed to handle polymorphic field names from different microservices.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationEvent {
    
    @JsonAlias({"type", "eventType"})
    private String eventType;
    
    private String source;
    private String timestamp;
    
    @JsonAlias({"payload", "metadata", "data"})
    private Map<String, Object> payload;
    
    private String key;

    // Helper fields for legacy or direct mapper support
    private String patientName;
    private String doctorName;
    private String appointmentTime;
    
    private Long adminId;
    private String message;
}
