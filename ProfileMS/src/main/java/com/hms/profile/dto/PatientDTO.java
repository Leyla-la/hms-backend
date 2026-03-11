package com.hms.profile.entity;

import com.hms.profile.dto.BloodGroup;
import jakarta.persistence.*;
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
    String aadharNo;
    BloodGroup bloodGroup;

    public Patient toPatient() {
        return new Patient(this.id, this.name, this.email, this.dob, this.phone, this.address, this.aadharNo, this.bloodGroup);
    }

}
