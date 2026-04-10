package com.hms.appointment.clients;

import com.hms.appointment.config.FeignSecurityInterceptor;
import com.hms.appointment.dto.DoctorDTO;
import com.hms.appointment.dto.DoctorName;
import com.hms.appointment.dto.PatientDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
@FeignClient(name = "ProfileMS", configuration = FeignSecurityInterceptor.class)
public interface ProfileClient {

    @GetMapping("profile/patient/exists/{id}")
    Boolean patientExists(@PathVariable("id") Long id);

    @GetMapping("profile/doctor/exists/{id}")
    Boolean doctorExists(@PathVariable("id") Long id);

    @GetMapping("profile/patient/get/{id}")
    PatientDTO getPatientById(@PathVariable("id") Long id);

    @GetMapping("profile/doctor/get/{id}")
    DoctorDTO getDoctorById(@PathVariable("id") Long id);

    @GetMapping("profile/admin/ids")
    List<Long> getAdminIds();

    @GetMapping("profile/patient/get-names")
    List<DoctorName> getPatientsById(@RequestParam("ids") List<Long> ids);

    @GetMapping("profile/doctor/get-names")
    List<DoctorName> getDoctorsById(@RequestParam("ids") List<Long> ids);
}
