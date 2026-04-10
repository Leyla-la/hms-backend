package com.hms.profile.entity;

import com.hms.profile.dto.BloodGroup;
import com.hms.profile.dto.PatientDTO;
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
public class Patient {
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
    String citizenId;

    BloodGroup bloodGroup;
    String allergies;
    String chronicDiseases;

    @Column(name = "profile_picture_id")
    Long profilePictureId;

    public PatientDTO toPatientDTO() {
        return PatientDTO.builder()
                .id(this.id)
                .name(this.name)
                .email(this.email)
                .dob(this.dob)
                .phone(this.phone)
                .address(this.address)
                .citizenId(this.citizenId)
                .bloodGroup(this.bloodGroup)
                .allergies(this.allergies)
                .chronicDiseases(this.chronicDiseases)
                .profilePictureId(this.profilePictureId)
                .build();
    }
}
