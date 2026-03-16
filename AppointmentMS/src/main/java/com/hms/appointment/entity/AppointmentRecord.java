package com.hms.appointment.entity;

import com.hms.appointment.dto.AppointmentRecordDTO;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.hms.appointment.utility.StringListConverter.fromCsv;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppointmentRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    Long patientId;
    Long doctorId;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "appointment_id")
    Appointment appointment;

    String symptoms;
    String diagnosis;
    String tests;
    String notes;
    String referral;
    LocalDate followUpDate;
    LocalDateTime createdAt;

    public static AppointmentRecordDTO toAppointmentRecordDTO(AppointmentRecord record) {
        if (record == null) return null;

        Long appointmentId = record.getAppointment() != null ? record.getAppointment().getId() : null;

        return new AppointmentRecordDTO(
                record.getId(),
                record.getPatientId(),
                record.getDoctorId(),
                appointmentId,
                fromCsv(record.getSymptoms()),
                record.getDiagnosis(),
                fromCsv(record.getTests()),
                record.getNotes(),
                record.getReferral(),
                null, // prescription (set explicitly to null)
                record.getFollowUpDate(),
                record.getCreatedAt()
        );
    }
}
