package com.hms.appointment.entity;

import com.hms.appointment.dto.MedicineDTO;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Medicine {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    Long id;
    String name;
    Long medicineId;
    String dosage;
    String frequency;
    Integer duration; // in days
    String route; // e.g., oral, intravenous
    String type; // e.g., tablet, syrup
    String instructions;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id")
    Prescription prescription;

    public MedicineDTO toMedicineDTO() {
        return new MedicineDTO(this.id, this.name, this.medicineId, this.dosage, this.frequency, this.duration, this.route, this.type, this.instructions, prescription.getId());
    }
}
