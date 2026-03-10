package com.hms.user.api;

import com.hms.user.dto.LoginDTO;
import com.hms.user.dto.ResponseDTO;
import com.hms.user.dto.UserDTO;
import com.hms.user.exception.HmsException;
import com.hms.user.jwt.JwtUtil;
import com.hms.user.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@RestController
@RequestMapping("/users")
@Validated
@CrossOrigin
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserAPI {
    UserService userService;
    UserDetailsService userDetailsService;
    AuthenticationManager authenticationManager;
    JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<ResponseDTO> registerUser(@RequestBody @Valid UserDTO userDTO) throws HmsException {
        userService.registerUser(userDTO);
        return new ResponseEntity<>(new ResponseDTO("Account created successfully"), HttpStatus.CREATED);

    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody @Valid LoginDTO loginDTO) throws HmsException {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword()));
        } catch (AuthenticationException e) {
            throw new HmsException("INVALID_CREDENTIALS");
        }
        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginDTO.getEmail());
        final String jwt = jwtUtil.generateToken(userDetails);
        return new ResponseEntity<>(jwt, HttpStatus.OK);
    }

}
