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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/users")
@Validated
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "User Management", description = "Endpoints for user authentication, registration, and management")
public class UserAPI {
    UserService userService;
    UserDetailsService userDetailsService;
    AuthenticationManager authenticationManager;
    JwtUtil jwtUtil;

    @Operation(summary = "Register a new user", description = "Creates a new user account in the system with the provided credentials and details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User account created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user input or email already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<ResponseDTO> registerUser(@RequestBody @Valid UserDTO userDTO) throws HmsException {
        userService.registerUser(userDTO);
        return new ResponseEntity<>(new ResponseDTO("Account created successfully"), HttpStatus.CREATED);

    }

    @Operation(summary = "Authenticate user", description = "Authenticates a user via email and password, returning a JWT token upon successful login.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully authenticated and JWT token returned"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials provided")
    })
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

    @Operation(summary = "Get System Administrator IDs", description = "Retrieves a list of internal user IDs mapped to individuals with the ADMIN role.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of admin IDs")
    @GetMapping("/admin/ids")
    public List<Long> getAdminIds() throws HmsException {
        return userService.getAdminIds();
    }

    @Operation(summary = "Count registered users", description = "Retrieve the total number of registered users across all roles within the system.")
    @ApiResponse(responseCode = "200", description = "Successfully returned user count")
    @GetMapping("/count")
    public long count() {
        return userService.count();
    }

    @Operation(summary = "Retrieve associated Profile ID", description = "Given a user ID, retrieves their associated specific Profile ID (Patient ID, Doctor ID, etc.).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile ID successfully found and returned"),
            @ApiResponse(responseCode = "404", description = "User ID doesn't have an associated profile")
    })
    @GetMapping("/getProfile/{id}")
    public ResponseEntity<java.util.Map<String, Object>> getProfile(@PathVariable Long id) throws HmsException {
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("profileId", userService.getProfile(id));
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }
}
