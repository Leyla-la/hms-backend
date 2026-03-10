package com.hms.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hms.user.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDTO {
    Long id;

    @NotBlank(message = "Name cannot be blank")
    String name;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email cannot be blank")
    String email;

    @NotBlank(message = "Password cannot be blank")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
    String password;
    Roles role;

    public User toUser() {
        return new User(this.id, this.name, this.email, this.password, this.role);
    }
}
