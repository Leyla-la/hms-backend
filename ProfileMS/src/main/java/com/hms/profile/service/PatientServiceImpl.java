package com.hms.profile.service;

import com.hms.profile.dto.DoctorName;
import com.hms.profile.dto.PatientDTO;
import com.hms.profile.exception.HmsException;
import com.hms.profile.repository.PatientRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PatientServiceImpl implements PatientService {

    final PatientRepository patientRepository;

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

    @Override
    public PatientDTO updatePatient(PatientDTO patientDTO) throws HmsException {
        if (patientDTO.getId() == null || patientRepository.findById(patientDTO.getId()).isEmpty()) {
            throw new HmsException("PATIENT_NOT_FOUND");
        }
        return (patientRepository.save(patientDTO.toPatient())).toPatientDTO();
    }

    @Override
    public Boolean patientExists(Long id) throws HmsException {
        return patientRepository.existsById(id);
    }

    @Override
    public List<DoctorName> getPatientNamesByIds(List<Long> ids) {

        return patientRepository.findAllById(ids)
                .stream()
                .map(patient -> new DoctorName(patient.getId(), patient.getName()))
                .toList();
    }

    @Override
    public List<PatientDTO> getAllPatients() {
        return patientRepository.findAll().stream().map(p -> p.toPatientDTO()).toList();
    }
    @Override
    public long count() {
        return patientRepository.count();
    }

}
