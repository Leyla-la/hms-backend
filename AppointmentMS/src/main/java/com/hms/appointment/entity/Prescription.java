package com.hms.appointment.entity;

import com.hms.appointment.dto.PrescriptionDTO;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Prescription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    Long patientId;
    Long doctorId;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    Appointment appointment;

    LocalDate prescriptionDate;
    @OneToMany(fetch = FetchType.LAZY)
    List<Medicine> medicines;
    String notes;

    public Prescription(Long prescriptionId) {
        this.id = prescriptionId;
    }

    public PrescriptionDTO toPrescriptionDTO() {
        return new PrescriptionDTO(this.id, this.patientId, this.doctorId, this.appointment != null ? this.appointment.getId() : null, this.prescriptionDate, this.medicines != null ? this.medicines.stream().map(Medicine::toMedicineDTO).toList() : List.of(), this.notes);
    }
}
