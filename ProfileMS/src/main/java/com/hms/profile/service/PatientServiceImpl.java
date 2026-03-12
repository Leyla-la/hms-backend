package com.hms.profile.service;

import com.hms.profile.dto.PatientDTO;
import com.hms.profile.exception.HmsException;
import com.hms.profile.repository.PatientRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PatientServiceImpl implements PatientService {

    PatientRepository patientRepository;

    @Override
    public Long addPatient(PatientDTO patientDTO) throws HmsException {
        if (patientDTO.getEmail() != null && patientRepository.findByEmail(patientDTO.getEmail()).isPresent()) throw new HmsException("PATIENT_ALREADY_EXISTS");
        if (patientDTO.getCitizenId() != null && patientRepository.findByCitizenId(patientDTO.getCitizenId()).isPresent()) throw new HmsException("PATIENT_ALREADY_EXISTS");
        return (patientRepository.save(patientDTO.toPatient())).getId();
    }

    @Override
    public PatientDTO getPatientById(Long id) throws HmsException {
        return (patientRepository.findById(id).orElseThrow(() -> new HmsException("PATIENT_NOT_FOUND"))).toPatientDTO();
    }
}
