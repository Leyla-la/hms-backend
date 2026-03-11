package com.hms.profile.service;


import com.hms.profile.entity.PatientDTO;
import com.hms.profile.exception.HmsException;

public interface PatientService {

    public Long addPatient(PatientDTO patientDTO) throws HmsException;
    public PatientDTO getPatientById(Long id) throws HmsException;

}
