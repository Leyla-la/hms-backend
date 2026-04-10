package com.hms.appointment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hms.appointment.entity.Appointment;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppointmentDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    Long patientId;
    Long doctorId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @com.fasterxml.jackson.annotation.JsonAlias({"appointmentTime", "time"})
    LocalDateTime appointmentTime;

    public void setAppointmentTime(Object time) {
        if (time == null) {
            this.appointmentTime = null;
            return;
        }
        if (time instanceof String str) {
            try {
                if (str.contains("T")) {
                    this.appointmentTime = LocalDateTime.parse(str.substring(0, 19));
                } else {
                    java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    this.appointmentTime = LocalDateTime.parse(str, fmt);
                }
            } catch (Exception e) {
                try {
                    this.appointmentTime = LocalDateTime.ofInstant(java.time.Instant.parse(str), java.time.ZoneId.systemDefault());
                } catch (Exception e2) {
                    this.appointmentTime = null;
                }
            }
        } else if (time instanceof LocalDateTime ldt) {
            this.appointmentTime = ldt;
        }
    }

    Status status;
    String reason;
    String notes;
    String doctorName;
    String patientName;

    public Appointment toAppointment() {
        return new Appointment(this.id, this.patientId, this.doctorId, this.appointmentTime, this.status, this.reason, this.notes);
    }



}
