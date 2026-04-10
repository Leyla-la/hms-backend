package com.hms.appointment.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoctorDTO {
    Long id;
    String name;
    String email;
    LocalDate dob;
    String phone;
    String address;
    String licenseNo;
    String specialization;
    String department;
    Integer totalExp;
    Long profilePictureId;

}
