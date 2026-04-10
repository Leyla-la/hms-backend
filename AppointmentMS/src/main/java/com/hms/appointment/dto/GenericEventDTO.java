package com.hms.appointment.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GenericEventDTO<T> {
    String eventId;
    String eventType;
    T payload;
    String source;
    LocalDateTime timestamp;
    List<String> recipientIds; // Dùng String để đồng bộ với API của UserMS
}