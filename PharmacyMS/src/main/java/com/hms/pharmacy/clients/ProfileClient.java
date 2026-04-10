package com.hms.pharmacy.clients;

import com.hms.pharmacy.config.FeignSecurityInterceptor;
import com.hms.pharmacy.dto.DoctorDTO;
import com.hms.pharmacy.dto.PatientDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

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
}
