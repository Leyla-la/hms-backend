package com.hms.appointment.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PrescriptionDetails {
    Long id;
    Long appointmentId;
    
    // Thông tin bệnh nhân
    Long patientId;
    String patientName; // Sẽ được set sau từ ProfileClient
    
    // Thông tin bác sĩ
    Long doctorId;
    String doctorName;  // Sẽ được set sau từ ProfileClient
    
    LocalDate prescriptionDate;
    List<MedicineDTO> medicines; // Sẽ được load từ MedicineService
    String notes;
}