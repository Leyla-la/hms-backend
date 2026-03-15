package com.hms.appointment.entity;

import com.hms.appointment.dto.AppointmentDTO;
import com.hms.appointment.dto.Status;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    Long patientId;
    Long doctorId;
    LocalDate appointmentTime;
    Status status;
    String reason;
    String notes;

    public AppointmentDTO toAppointmentDTO() {
        return new AppointmentDTO(this.id, this.patientId, this.doctorId, this.appointmentTime, this.status, this.reason, this.notes);
    }



}
