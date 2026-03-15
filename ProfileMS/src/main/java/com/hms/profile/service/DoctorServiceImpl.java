package com.hms.profile.service;

import com.hms.profile.dto.DoctorDTO;
import com.hms.profile.exception.HmsException;
import com.hms.profile.repository.DoctorRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DoctorServiceImpl implements DoctorService{
    DoctorRepository doctorRepository;

    @Override
    public Long addDoctor(DoctorDTO doctorDTO) throws HmsException {
        if (doctorDTO.getEmail() != null && doctorRepository.findByEmail(doctorDTO.getEmail()).isPresent()) {
            throw new HmsException("PATIENT_ALREADY_EXISTS");
        }
        if (doctorDTO.getLicenseNo() != null && doctorRepository.findByLicenseNo(doctorDTO.getLicenseNo()).isPresent()) {
            throw new HmsException("PATIENT_ALREADY_EXISTS");
        }
        return (doctorRepository.save(doctorDTO.toDoctor())).getId();
    }

    @Override
    public DoctorDTO getDoctorById(Long doctorId) throws HmsException {
        return doctorRepository.findById(doctorId).orElseThrow(() -> new HmsException("DOCTOR_NOT_FOUND")).toDoctorDTO();
    }

    @Override
    public DoctorDTO updateDoctor(DoctorDTO doctorDTO) throws HmsException {
        if (doctorRepository.findById(doctorDTO.getId()).isEmpty()) {
            throw new HmsException("DOCTOR_NOT_FOUND");
        }
        return doctorRepository.save(doctorDTO.toDoctor()).toDoctorDTO();
    }

    @Override
    public Boolean doctorExists(Long doctorId) throws HmsException {
        return doctorRepository.existsById(doctorId);
    }

}
