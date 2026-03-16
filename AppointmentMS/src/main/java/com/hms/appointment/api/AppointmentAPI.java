package com.hms.appointment.api;

import com.hms.appointment.dto.AppointmentDTO;
import com.hms.appointment.dto.AppointmentDetails;
import com.hms.appointment.exception.HmsException;
import com.hms.appointment.service.AppointmentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("appointments")
@Validated
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppointmentAPI {
    AppointmentService appointmentService;

    @PostMapping("/schedule")
    public ResponseEntity<Long> scheduleAppointment(@RequestBody AppointmentDTO appointmentDTO) throws HmsException {
        return new ResponseEntity<>(appointmentService.scheduleAppointment(appointmentDTO), HttpStatus.CREATED);
    }

    @PutMapping("/cancel/{appointmentId}")
    public ResponseEntity<String> cancelAppointment(@PathVariable Long appointmentId) throws HmsException {
        appointmentService.cancelAppointment(appointmentId);
        return new ResponseEntity<>("Appointment cancelled", HttpStatus.NO_CONTENT);
    }

    @GetMapping("/get/{appointmentId}")
    public ResponseEntity<AppointmentDTO> getAppointmentDetails(@PathVariable Long appointmentId) throws HmsException {
        return new ResponseEntity<>(appointmentService.getAppointmentDetails(appointmentId), HttpStatus.OK);
    }

    @GetMapping("/get/details/{appointmentId}")
    public ResponseEntity<AppointmentDetails> getAppointmentDetailsWithName(@PathVariable Long appointmentId) throws HmsException {
        return new ResponseEntity<>(appointmentService.getAppointmentDetailsWithName(appointmentId), HttpStatus.OK);
    }

    @GetMapping("getAllByPatient/{patientId}")
    public ResponseEntity<List<AppointmentDetails>> getAllAppointmentsByPatientId(@PathVariable Long patientId) throws HmsException {
        return new ResponseEntity<>(appointmentService.getAllAppointmentsByPatientId(patientId), HttpStatus.OK);
    }

    @GetMapping("getAllByDoctor/{doctorId}")
    public ResponseEntity<List<AppointmentDetails>> getAllAppointmentsByDoctorId(@PathVariable Long doctorId) throws HmsException {
        return new ResponseEntity<>(appointmentService.getAllAppointmentsByDoctorId(doctorId), HttpStatus.OK);
    }

}
