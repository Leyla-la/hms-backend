package com.hms.appointment.dto;

import com.hms.appointment.entity.Medicine;
import com.hms.appointment.entity.Prescription;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MedicineDTO {
    Long id;
    String name;
    Long medicineId;
    String dosage;
    String frequency;
    Integer duration; // in days
    String route; // e.g., oral, intravenous
    String type; // e.g., tablet, syrup
    String instructions;
    Long prescriptionId;

    public Medicine toMedicine() {
        return Medicine.builder()
                .id(this.id)
                .name(this.name)
                .medicineId(this.medicineId)
                .dosage(this.dosage)
                .frequency(this.frequency)
                .duration(this.duration)
                .route(this.route)
                .type(this.type)
                .instructions(this.instructions)
                .prescription(this.prescriptionId != null ? new Prescription(this.prescriptionId) : null)
                .build();
    }
}
