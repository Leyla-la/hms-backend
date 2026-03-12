package com.hms.profile.dto;

import com.hms.profile.entity.Patient;
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

    public Patient toPatient() {
        return new Patient(this.id, this.name, this.email, this.dob, this.phone, this.address, this.citizenId, this.bloodGroup);
    }

}
