package com.hms.appointment.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

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
    LocalDate appointmentTime;
    Status status;
    String reason;
    String notes;
}
