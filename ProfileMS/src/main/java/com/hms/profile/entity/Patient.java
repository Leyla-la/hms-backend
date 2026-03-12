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

    public PatientDTO toPatientDTO() {
        return new PatientDTO(this.id, this.name, this.email, this.dob, this.phone, this.address, this.citizenId, this.bloodGroup, this.allergies, this.chronicDiseases);
    }
}
