package com.hms.appointment.entity;

import com.hms.appointment.dto.PrescriptionDTO;
import com.hms.appointment.dto.PrescriptionDetails;

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
    @OneToMany(mappedBy = "prescription", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    List<Medicine> medicines;
    String notes;

    public Prescription(Long prescriptionId) {
        this.id = prescriptionId;
    }

    public PrescriptionDTO toPrescriptionDTO() {
        return PrescriptionDTO.builder()
                .id(this.id)
                .patientId(this.patientId)
                .doctorId(this.doctorId)
                .appointmentId(this.appointment != null ? this.appointment.getId() : null)
                .prescriptionDate(this.prescriptionDate)
                .medicines(this.medicines != null ? this.medicines.stream().map(Medicine::toMedicineDTO).toList() : List.of())
                .notes(this.notes)
                .build();
    }

    // Method bổ sung để phục vụ PrescriptionServiceImpl
    public PrescriptionDetails toPrescriptionDetails() {
        return PrescriptionDetails.builder()
                .id(this.id)
                .appointmentId(this.appointment != null ? this.appointment.getId() : null)
                .patientId(this.patientId)
                .doctorId(this.doctorId)
                .prescriptionDate(this.prescriptionDate)
                .notes(this.notes)
                // Medicines và Names sẽ được Service set sau
                .build();
    }


}
