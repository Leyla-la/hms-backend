package com.hms.profile.entity;

import com.hms.profile.dto.BloodGroup;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import com.hms.profile.entity.PatientDTO;

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
    String aadharNo;

    BloodGroup bloodGroup;

    public PatientDTO toPatientDTO() {
        return new PatientDTO(this.id, this.name, this.email, this.dob, this.phone, this.address, this.aadharNo, this.bloodGroup);
    }

}
