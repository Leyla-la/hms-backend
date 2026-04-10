package com.hms.profile.service;


import java.util.List;

import com.hms.profile.dto.DoctorName;
import com.hms.profile.dto.PatientDTO;
import com.hms.profile.exception.HmsException;

public interface PatientService {

    Long addPatient(PatientDTO patientDTO) throws HmsException;
    PatientDTO getPatientById(Long id) throws HmsException;
    PatientDTO updatePatient(PatientDTO patientDTO) throws HmsException;
    Boolean patientExists(Long id) throws HmsException;
    List<DoctorName> getPatientNamesByIds(List<Long> ids);
    List<PatientDTO> getAllPatients();
    long count();

}
