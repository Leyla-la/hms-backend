package com.hms.profile.jwt;

import com.hms.user.dto.Roles;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomUserDetails implements UserDetails {
    Long id;
    String username;
    String email;
    String password;
    Roles role;
    String name;
    Collection<? extends GrantedAuthority> authorities;
}
