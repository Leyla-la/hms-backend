package com.hms.profile.entity;

import com.hms.profile.dto.BloodGroup;
import com.hms.profile.dto.DoctorDTO;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String name;

    @Column(unique = true)
    String email;

    LocalDate dob;
    String phone;
    String address;

    @Column(unique = true)
    String licenseNo;
    String specialization;
    String department;
    Integer totalExp;


    public DoctorDTO toDoctorDTO() {
        return new DoctorDTO(this.id, this.name, this.email, this.dob, this.phone, this.address, this.licenseNo, this.specialization, this.department, this.totalExp);
    }

}
