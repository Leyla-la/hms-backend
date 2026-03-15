package com.hms.profile.service;


import com.hms.profile.dto.PatientDTO;
import com.hms.profile.exception.HmsException;

public interface PatientService {

    Long addPatient(PatientDTO patientDTO) throws HmsException;
    PatientDTO getPatientById(Long id) throws HmsException;
    PatientDTO updatePatient(PatientDTO patientDTO) throws HmsException;
        Boolean patientExists(Long id) throws HmsException;

}
