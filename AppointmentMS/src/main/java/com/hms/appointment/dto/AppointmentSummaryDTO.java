package com.hms.appointment.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppointmentSummaryDTO {
    long totalAll;
    long totalToday;
    long scheduled;
    long completed;
    long cancelled;
    long overdue; // Trường cuối cùng bạn truyền 0 trong Service
}