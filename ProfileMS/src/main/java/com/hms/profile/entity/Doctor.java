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

    @Column(name = "profile_picture_id")
    Long profilePictureId;


    public DoctorDTO toDoctorDTO() {
        return DoctorDTO.builder()
                .id(this.id)
                .name(this.name)
                .email(this.email)
                .dob(this.dob)
                .phone(this.phone)
                .address(this.address)
                .licenseNo(this.licenseNo)
                .specialization(this.specialization)
                .department(this.department)
                .totalExp(this.totalExp)
                .profilePictureId(this.profilePictureId)
                .build();
    }

}
