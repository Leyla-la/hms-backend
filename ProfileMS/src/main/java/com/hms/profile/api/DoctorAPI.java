package com.hms.profile.api;

import com.hms.profile.dto.DoctorDTO;
import com.hms.profile.dto.DoctorName;
import com.hms.profile.exception.HmsException;
import com.hms.profile.service.DoctorService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("profile/doctor")
@Validated
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Doctor Profile Management", description = "Endpoints for creating, updating, and retrieving doctor profiles and specialties.")
public class DoctorAPI {
    DoctorService doctorService;

    @Operation(summary = "Add a new doctor", description = "Registers a new doctor profile in the system.")
    @PostMapping("/add")
    public ResponseEntity<Long> addDoctor(@RequestBody DoctorDTO DoctorDTO) throws HmsException {
        return new ResponseEntity<>(doctorService.addDoctor(DoctorDTO), HttpStatus.CREATED);
    }

    @Operation(summary = "Get doctor by ID", description = "Retrieves full profile details for a specific doctor.")
    @GetMapping("/get/{id}")
    public ResponseEntity<DoctorDTO> getDoctorById(@PathVariable("id") Long id) throws HmsException {
        return new ResponseEntity<>(doctorService.getDoctorById(id), HttpStatus.OK);
    }

    @Operation(summary = "Update doctor profile", description = "Updates an existing doctor's professional or contact information.")
    @PutMapping("/update")
    public ResponseEntity<DoctorDTO> updateDoctor(@RequestBody DoctorDTO DoctorDTO) throws HmsException {
        return new ResponseEntity<>(doctorService.updateDoctor(DoctorDTO), HttpStatus.OK);
    }

    @Operation(summary = "Get doctor names by IDs", description = "Retrieves a mapping of IDs to names for a list of doctors.")
    @GetMapping("/get-names")
    public ResponseEntity<List<DoctorName>> getDoctorNames(@RequestParam("ids") List<Long> ids) {
        return ResponseEntity.ok(doctorService.getDoctorNamesByIds(ids));
    }

    @Operation(summary = "Get doctor dropdown list", description = "Retrieves a simplified list of doctors for selection in UI dropdowns.")
    @GetMapping("/dropdowns")
    public ResponseEntity<List<com.hms.profile.dto.DoctorDropdown>> getDoctorDropdown() throws HmsException {
        return ResponseEntity.ok(doctorService.getDoctorDropdown());
    }

    @Operation(summary = "Get all doctors", description = "Retrieves a list of all registered doctor profiles.")
    @GetMapping("/getAll")
    public ResponseEntity<List<DoctorDTO>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    @Operation(summary = "Count registered doctors", description = "Returns the total number of doctor profiles currently in the system.")
    @GetMapping("/count")
    public long count() {
        return doctorService.count();
    }

    @Operation(summary = "Get doctors count by department", description = "Returns the number of doctors grouped by their departments for dashboard analytics.")
    @GetMapping("/countByDept")
    public ResponseEntity<Map<String, Long>> getDoctorsByDept() {
        return ResponseEntity.ok(doctorService.getDoctorsByDept());
    }

    @Operation(summary = "Check if doctor exists", description = "Checks whether a doctor profile exists for a given ID.")
    @GetMapping("/exists/{id}")
    public Boolean doctorExists(@PathVariable("id") Long id) throws HmsException {
        return doctorService.doctorExists(id);
    }
}
