package com.hms.appointment.dto;

import com.hms.appointment.entity.Appointment;
import com.hms.appointment.entity.Prescription;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PrescriptionDTO {
    Long id;
    Long patientId;
    Long doctorId;
    Long appointmentId;
    LocalDate prescriptionDate;
    List<MedicineDTO> medicines;
    String notes;

    public Prescription toPrescription() {
        return Prescription.builder()
                .id(this.id)
                .patientId(this.patientId)
                .doctorId(this.doctorId)
                .appointment(this.appointmentId == null ? null : new Appointment(this.appointmentId))
                .prescriptionDate(this.prescriptionDate)
                .medicines(this.medicines == null ? List.of() : this.medicines.stream().map(MedicineDTO::toMedicine).toList())
                .notes(this.notes)
                .build();
    }

}
