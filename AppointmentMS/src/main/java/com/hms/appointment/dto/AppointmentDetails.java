package com.hms.appointment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
    String doctorLicenseNumber;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime appointmentTime;

    Status status;
    String reason;
    String notes;
}
