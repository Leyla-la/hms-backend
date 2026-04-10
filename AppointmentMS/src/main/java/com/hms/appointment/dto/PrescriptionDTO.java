package com.hms.appointment.dto;

import com.hms.appointment.entity.Appointment;
import com.hms.appointment.entity.Prescription;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
        // Map appointmentId sang object Appointment
        Appointment appointment = null;
        if (this.appointmentId != null) {
            // Giả định Entity Appointment có constructor nhận ID, 
            // hoặc bạn có thể sửa thành: appointment = new Appointment(); appointment.setId(this.appointmentId);
            appointment = new Appointment(this.appointmentId); 
        }

        return new Prescription(
                this.id,
                this.patientId,
                this.doctorId,
                appointment,
                this.prescriptionDate,
                this.medicines != null ? this.medicines.stream().map(MedicineDTO::toMedicine).toList() : null,
                this.notes
        );
    }
}