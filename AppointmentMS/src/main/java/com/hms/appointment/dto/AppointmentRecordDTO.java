package com.hms.appointment.dto;

import com.hms.appointment.entity.Appointment;
import com.hms.appointment.entity.AppointmentRecord;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.hms.appointment.utility.StringListConverter.toCsv;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppointmentRecordDTO {
    Long id;
    Long patientId;
    Long doctorId;
    Long appointmentId;
    List<String> symptoms;
    String diagnosis;
    List<String> tests;
    String notes;
    String referral;
    PrescriptionDTO prescription;
    LocalDate followUpDate;
    LocalDateTime createdAt;

    /**
     * Converts this DTO to a JPA entity.
     *
     * Entity stores symptoms/tests as CSV strings, while the DTO uses lists.
     */
    public AppointmentRecord toAppointmentRecord() {
        Appointment appointment = (appointmentId == null) ? null : new Appointment(appointmentId);

        return new AppointmentRecord(
                this.id,
                this.patientId,
                this.doctorId,
                appointment,
                toCsv(this.symptoms),
                this.diagnosis,
                toCsv(this.tests),
                this.notes,
                this.referral,
                this.followUpDate,
                this.createdAt
        );
    }




}
