package com.hms.appointment.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PatientDTO {
    Long id;
    String name;
    String email;
    LocalDate dob;
    String phone;
    String address;
    String citizenId;
    BloodGroup bloodGroup;
    String allergies;
    String chronicDiseases;
    Long profilePictureId;
}
