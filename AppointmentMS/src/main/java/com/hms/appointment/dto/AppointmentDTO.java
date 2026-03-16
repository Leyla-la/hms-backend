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
    LocalDateTime appointmentTime;

    Status status;
    String reason;
    String notes;

    public Appointment toAppointment() {
        return new Appointment(this.id, this.patientId, this.doctorId, this.appointmentTime, this.status, this.reason, this.notes);
    }



}
