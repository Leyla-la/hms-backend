package com.hms.user.jwt;

import com.hms.user.dto.UserDTO;
import com.hms.user.exception.HmsException;
import com.hms.user.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MyUserDetailsService implements UserDetailsService {
    UserService userService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            UserDTO userDTO = userService.getUser(email);
            return CustomUserDetails.builder()
        .id(userDTO.getId())
        .username(userDTO.getEmail())
        .email(userDTO.getEmail())
        .password(userDTO.getPassword())
        .role(userDTO.getRole())
        .name(userDTO.getName())
        .profileId(userDTO.getProfileId())
        .authorities(Collections.emptyList()) // Thay vì null
        .build();
        } catch (HmsException e) {
            e.printStackTrace();
        }
        return null;
    }

}
