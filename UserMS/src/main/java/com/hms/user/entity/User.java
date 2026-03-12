package com.hms.user.entity;

import com.hms.user.dto.Roles;
import com.hms.user.dto.UserDTO;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String name;

    @Column(unique = true)
    String email;

    String password;
    Roles role;
    Long profileId;

    public UserDTO toUserDTO() {
        return new UserDTO(this.id, this.name, this.email, this.password, this.role, this.profileId);
    }
}
