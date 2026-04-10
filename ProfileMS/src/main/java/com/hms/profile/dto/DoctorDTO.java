package com.hms.profile.dto;

import com.hms.profile.entity.Doctor;
import com.hms.profile.entity.Patient;
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


    public Doctor toDoctor() {
        return new Doctor(this.id, this.name, this.email, this.dob, this.phone, this.address, this.licenseNo, this.specialization, this.department, this.totalExp, this.profilePictureId);
    }

}
