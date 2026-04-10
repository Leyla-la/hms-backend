package com.hms.appointment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppointmentDetails {
    Long id;
    Long patientId;
    String patientName;
    String patientEmail;
    String patientPhone;
    String patientCitizenId;
    Long doctorId;
    String doctorName;
    String doctorEmail;
    String doctorPhone;
    String doctorSpecialization;
    String doctorLicenseNo; // Khớp với licenseNo trong Service

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime appointmentTime;

    Status status;
    String reason;
    String notes;
}