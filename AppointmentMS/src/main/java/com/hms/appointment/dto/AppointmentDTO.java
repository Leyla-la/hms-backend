package com.hms.appointment.dto;

import com.hms.appointment.entity.Appointment;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

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
    LocalDate appointmentTime;
    Status status;
    String reason;
    String notes;

    public Appointment toAppointment() {
        return new Appointment(this.id, this.patientId, this.doctorId, this.appointmentTime, this.status, this.reason, this.notes);
    }



}
