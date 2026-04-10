package com.hms.appointment.api;

import com.hms.appointment.dto.*;
import com.hms.appointment.exception.HmsException;
import com.hms.appointment.service.AppointmentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("appointments")
@Validated
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Appointment Management", description = "Endpoints for scheduling, canceling, and managing medical appointments.")
public class AppointmentAPI {
    AppointmentService appointmentService;

    @Operation(summary = "Schedule a new appointment", description = "Creates a new appointment booking for a patient with a specific doctor.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Appointment scheduled successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid appointment details provided")
    })
    @PostMapping("/schedule")
    public ResponseEntity<Long> scheduleAppointment(@RequestBody AppointmentDTO appointmentDTO,
                                @RequestHeader(value = "X-Actor-Id", required = false) String actorId,
                                @RequestHeader(value = "X-Actor-Name", required = false) String actorName,
                                @RequestHeader(value = "X-Actor-Role", required = false) String actorRole,
                                @RequestHeader(value = "X-User-Id", required = false) String userId,
                                @RequestHeader(value = "X-User-Role", required = false) String userRole) throws HmsException {
        String effectiveActorId = actorId != null ? actorId : userId;
        String effectiveActorName = actorName != null ? actorName : "Unknown";
        String effectiveActorRole = actorRole != null ? actorRole : userRole;

        Map<String, String> actor = new java.util.HashMap<>();
        actor.put("id", effectiveActorId != null ? effectiveActorId : "");
        actor.put("name", effectiveActorName != null ? effectiveActorName : "Unknown");
        actor.put("role", effectiveActorRole != null ? effectiveActorRole : "");

        return new ResponseEntity<>(appointmentService.scheduleAppointment(appointmentDTO, actor), HttpStatus.CREATED);
    }

    @Operation(summary = "Cancel an appointment", description = "Cancels an existing appointment by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Appointment cancelled successfully"),
            @ApiResponse(responseCode = "404", description = "Appointment not found")
    })
    @PutMapping("/cancel/{appointmentId}")
    public ResponseEntity<String> cancelAppointment(@PathVariable("appointmentId") Long appointmentId,
                                @RequestHeader(value = "X-Actor-Id", required = false) String actorId,
                                @RequestHeader(value = "X-Actor-Name", required = false) String actorName,
                                @RequestHeader(value = "X-Actor-Role", required = false) String actorRole,
                                @RequestHeader(value = "X-User-Id", required = false) String userId,
                                @RequestHeader(value = "X-User-Role", required = false) String userRole) throws HmsException {
        String effectiveActorId = actorId != null ? actorId : userId;
        String effectiveActorName = actorName != null ? actorName : "Unknown";
        String effectiveActorRole = actorRole != null ? actorRole : userRole;

        Map<String, String> actor = Map.of(
            "id", effectiveActorId == null ? "" : effectiveActorId,
            "name", effectiveActorName,
            "role", effectiveActorRole == null ? "" : effectiveActorRole
        );
        appointmentService.cancelAppointment(appointmentId, actor);
        return new ResponseEntity<>("Appointment cancelled", HttpStatus.NO_CONTENT);
    }

        @Operation(summary = "Complete an appointment", description = "Marks an appointment as completed after the visit.")
    @ApiResponse(responseCode = "204", description = "Appointment marked as completed")
    @PutMapping("/complete/{appointmentId}")
    public ResponseEntity<String> completeAppointment(@PathVariable("appointmentId") Long appointmentId,
                                  @RequestHeader(value = "X-Actor-Id", required = false) String actorId,
                                  @RequestHeader(value = "X-Actor-Name", required = false) String actorName,
                                  @RequestHeader(value = "X-Actor-Role", required = false) String actorRole,
                                  @RequestHeader(value = "X-User-Id", required = false) String userId,
                                  @RequestHeader(value = "X-User-Role", required = false) String userRole) throws HmsException {
        String effectiveActorId = actorId != null ? actorId : userId;
        String effectiveActorName = actorName != null ? actorName : "Unknown";
        String effectiveActorRole = actorRole != null ? actorRole : userRole;

        Map<String, String> actor = Map.of(
            "id", effectiveActorId == null ? "" : effectiveActorId,
            "name", effectiveActorName,
            "role", effectiveActorRole == null ? "" : effectiveActorRole
        );

        appointmentService.completeAppointment(appointmentId, actor);
        return new ResponseEntity<>("Appointment completed", HttpStatus.NO_CONTENT);
        }

    @Operation(summary = "Get appointment details", description = "Retrieves core data for a specific appointment.")
    @GetMapping("/get/{appointmentId}")
    public ResponseEntity<AppointmentDTO> getAppointmentDetails(@PathVariable("appointmentId") Long appointmentId) throws HmsException {
        return new ResponseEntity<>(appointmentService.getAppointmentDetails(appointmentId), HttpStatus.OK);
    }

    @Operation(summary = "Get appointment details with names", description = "Retrieves enriched appointment data including doctor and patient names.")
    @GetMapping("/get/details/{appointmentId}")
    public ResponseEntity<AppointmentDetails> getAppointmentDetailsWithName(@PathVariable("appointmentId") Long appointmentId) throws HmsException {
        return new ResponseEntity<>(appointmentService.getAppointmentDetailsWithName(appointmentId), HttpStatus.OK);
    }

    @Operation(summary = "Get all appointments for a patient", description = "Retrieves all appointments (past and future) associated with a specific patient.")
    @GetMapping("/getAllByPatient/{patientId}")
    public ResponseEntity<List<AppointmentDetails>> getAllAppointmentsByPatientId(@PathVariable("patientId") Long patientId) throws HmsException {
        return new ResponseEntity<>(appointmentService.getAllAppointmentsByPatientId(patientId), HttpStatus.OK);
    }

    @Operation(summary = "Get medical history for a patient", description = "Retrieves historical appointment records for a specific patient.")
    @GetMapping("/getHistoryByPatient/{patientId}")
    public ResponseEntity<List<AppointmentDTO>> getMedicalHistoryByPatientId(@PathVariable("patientId") Long patientId) throws HmsException {
        return new ResponseEntity<>(appointmentService.getMedicalHistoryByPatientId(patientId), HttpStatus.OK);
    }

    @Operation(summary = "Get all appointments for a doctor", description = "Retrieves all appointments (past and future) assigned to a specific doctor.")
    @GetMapping("/getAllByDoctor/{doctorId}")
    public ResponseEntity<List<AppointmentDetails>> getAllAppointmentsByDoctorId(@PathVariable("doctorId") Long doctorId) throws HmsException {
        return new ResponseEntity<>(appointmentService.getAllAppointmentsByDoctorId(doctorId), HttpStatus.OK);
    }

    // --- Dashboard endpoints ---
    @Operation(summary = "Get appointment summary for doctor", description = "Provides KPIs like total, upcoming, and completed appointments for a doctor's dashboard.")
    @GetMapping("/dashboard/summary")
    public ResponseEntity<AppointmentSummaryDTO> getSummaryForDoctor(@RequestParam(value = "doctorId", required = false) Long doctorId) throws HmsException {
        return new ResponseEntity<>(appointmentService.getSummaryForDoctor(doctorId), HttpStatus.OK);
    }

    @Operation(summary = "Get upcoming appointments for patient", description = "Retrieves the next set of upcoming appointments for a patient dashboard.")
    @GetMapping("/dashboard/upcoming")
    public ResponseEntity<List<AppointmentDTO>> getUpcomingForPatient(@RequestParam("patientId") Long patientId, @RequestParam(value = "limit", defaultValue = "5") int limit) throws HmsException {
        return new ResponseEntity<>(appointmentService.getUpcomingForPatient(patientId, limit), HttpStatus.OK);
    }

    /** Upcoming appointments for patient with enriched doctor name/specialization. */
    @Operation(summary = "Get detailed upcoming appointments for patient", description = "Retrieves upcoming appointments with enriched detail (doctor names, etc.) for a patient.")
    @GetMapping("/dashboard/upcoming/details")
    public ResponseEntity<List<AppointmentDetails>> getUpcomingDetailsForPatient(
            @RequestParam("patientId") Long patientId,
            @RequestParam(value = "limit", defaultValue = "5") int limit) throws HmsException {
        return new ResponseEntity<>(appointmentService.getUpcomingWithDetailsForPatient(patientId, limit), HttpStatus.OK);
    }

    @Operation(summary = "Get today's appointments for doctor", description = "Retrieves all appointments scheduled for the current date for a specific doctor.")
    @GetMapping("/dashboard/today")
    public ResponseEntity<List<AppointmentDTO>> getTodayForDoctor(@RequestParam("doctorId") Long doctorId) throws HmsException {
        return new ResponseEntity<>(appointmentService.getTodayForDoctor(doctorId), HttpStatus.OK);
    }

    /** Admin dashboard: all today's appointments across all doctors */
    @Operation(summary = "Get all today's appointments", description = "Retrieves all appointments scheduled for the current date across the entire system (Admin use).")
    @GetMapping("/dashboard/today/all")
    public ResponseEntity<List<AppointmentDTO>> getTodayAll() throws HmsException {
        return new ResponseEntity<>(appointmentService.getTodayAll(), HttpStatus.OK);
    }

    @Operation(summary = "Get appointment trend", description = "Calculates the trend of appointments over a specified date range.")
    @GetMapping("/dashboard/trend")
    public ResponseEntity<TrendDTO> getTrend(@RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                             @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) throws HmsException {
        return new ResponseEntity<>(appointmentService.getTrend(from, to), HttpStatus.OK);
    }

    @GetMapping("/dashboard/trend/patient")
    public ResponseEntity<TrendDTO> getTrendForPatient(@RequestParam("patientId") Long patientId,
                                                       @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                       @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) throws HmsException {
        return new ResponseEntity<>(appointmentService.getTrendForPatient(patientId, from, to), HttpStatus.OK);
    }

    @Operation(summary = "Count all appointments", description = "Returns the total number of appointments in the system.")
    @GetMapping("/count")
    public long count() {
        return appointmentService.count();
    }

}

