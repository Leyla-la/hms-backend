package com.hms.appointment.dto;

import com.hms.appointment.entity.Medicine;
import com.hms.appointment.entity.Prescription;
import jakarta.persistence.*;
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
        return new Medicine(this.id, this.name, this.medicineId, this.dosage, this.frequency, this.duration, this.route, this.type, this.instructions, new Prescription(prescriptionId));
    }
}
