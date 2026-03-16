package com.hms.appointment.api;

import com.hms.appointment.dto.AppointmentRecordDTO;
import com.hms.appointment.exception.HmsException;
import com.hms.appointment.service.AppointmentRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/appointments/record")
@Validated
@RequiredArgsConstructor
@CrossOrigin
public class AppointmentRecordAPI {
    AppointmentRecordService appointmentRecordService;

    @PostMapping("/create")
    public ResponseEntity<Long> createAppointmentRecord(@RequestBody AppointmentRecordDTO appointmentRecordDTO) throws HmsException {
        return new ResponseEntity<>(appointmentRecordService.createAppointmentRecord(appointmentRecordDTO), HttpStatus.CREATED);
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateAppointmentRecord(@RequestBody AppointmentRecordDTO appointmentRecordDTO) throws HmsException {
        appointmentRecordService.updateAppointmentRecord(appointmentRecordDTO);
        return new ResponseEntity<>("Appointment record updated", HttpStatus.OK);
    }

    @GetMapping("/getByAppointmentId/{appointmentId}")
    public ResponseEntity<AppointmentRecordDTO> getAppointmentRecordByAppointmentId(@PathVariable Long appointmentId) throws HmsException {
        return new ResponseEntity<>(appointmentRecordService.getAppointmentRecordByAppointmentId(appointmentId), HttpStatus.OK);
    }

    @GetMapping("/getById/{appointmentRecordId}")
    public ResponseEntity<AppointmentRecordDTO> getAppointmentRecordById(@PathVariable Long appointmentRecordId) throws HmsException {
        return new ResponseEntity<>(appointmentRecordService.getAppointmentRecordById(appointmentRecordId), HttpStatus.OK);
    }

}
