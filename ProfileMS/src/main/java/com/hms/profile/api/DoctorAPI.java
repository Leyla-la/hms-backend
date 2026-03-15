package com.hms.profile.api;

import com.hms.profile.dto.DoctorDTO;
import com.hms.profile.exception.HmsException;
import com.hms.profile.service.DoctorService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("profile/doctor")
@Validated
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DoctorAPI {
    DoctorService doctorService;

    @PostMapping("/add")
    public ResponseEntity<Long> addDoctor(@RequestBody DoctorDTO DoctorDTO) throws HmsException {
        return new ResponseEntity<>(doctorService.addDoctor(DoctorDTO), HttpStatus.CREATED);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<DoctorDTO> getDoctorById(@PathVariable Long id) throws HmsException {
        return new ResponseEntity<>(doctorService.getDoctorById(id), HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<DoctorDTO> updateDoctor(@RequestBody DoctorDTO DoctorDTO) throws HmsException {
        return new ResponseEntity<>(doctorService.updateDoctor(DoctorDTO), HttpStatus.OK);
    }

    @GetMapping("/exists/{id}")
    public ResponseEntity<Boolean> doctorExists(@PathVariable Long id) throws HmsException {
        return new ResponseEntity<>(doctorService.doctorExists(id), HttpStatus.OK);
    }

}
