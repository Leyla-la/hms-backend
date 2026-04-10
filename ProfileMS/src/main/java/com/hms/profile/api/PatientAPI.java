package com.hms.profile.api;

import com.hms.profile.dto.DoctorName;
import com.hms.profile.dto.PatientDTO;
import com.hms.profile.exception.HmsException;
import com.hms.profile.service.PatientService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("profile/patient")
@Validated
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Patient Profile Management", description = "Endpoints for creating, updating, and retrieving patient medical profiles.")
public class PatientAPI {
    PatientService patientService;

    @Operation(summary = "Add a new patient", description = "Registers a new patient profile in the system.")
    @PostMapping("/add")
    public ResponseEntity<Long> addPatient(@RequestBody PatientDTO patientDTO) throws HmsException {
        return new ResponseEntity<>(patientService.addPatient(patientDTO), HttpStatus.CREATED);
    }

    @Operation(summary = "Get patient by ID", description = "Retrieves full profile details for a specific patient.")
    @GetMapping("/get/{id}")
    public ResponseEntity<PatientDTO> getPatientById(@PathVariable("id") Long id) throws HmsException {
        return new ResponseEntity<>(patientService.getPatientById(id), HttpStatus.OK);
    }

    @Operation(summary = "Update patient profile", description = "Updates an existing patient's demographic or contact information.")
    @PutMapping("/update")
    public ResponseEntity<PatientDTO> updatePatient(@RequestBody PatientDTO patientDTO) throws HmsException {
        return new ResponseEntity<>(patientService.updatePatient(patientDTO), HttpStatus.OK);
    }

    @Operation(summary = "Get patient names by IDs", description = "Retrieves a mapping of IDs to names for a list of patients.")
    @GetMapping("/get-names")
    public ResponseEntity<List<DoctorName>> getPatientNames(@RequestParam("ids") List<Long> ids) {
        return ResponseEntity.ok(patientService.getPatientNamesByIds(ids));
    }

    @Operation(summary = "Get all patients", description = "Retrieves a list of all registered patients.")
    @GetMapping("/getAll")
    public ResponseEntity<List<PatientDTO>> getAllPatients() {
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    @Operation(summary = "Count registered patients", description = "Returns the total number of patient profiles.")
    @GetMapping("/count")
    public long count() {
        return patientService.count();
    }

    @Operation(summary = "Check if patient exists", description = "Checks whether a patient profile exists for a given ID.")
    @GetMapping("/exists/{id}")
    public Boolean patientExists(@PathVariable("id") Long id) throws HmsException {
        return patientService.patientExists(id);
    }
}
