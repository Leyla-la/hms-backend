package com.hms.profile.service;

import com.hms.profile.dto.DoctorDTO;
import com.hms.profile.dto.DoctorName;
import com.hms.profile.exception.HmsException;
import com.hms.profile.repository.DoctorRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DoctorServiceImpl implements DoctorService{
    final DoctorRepository doctorRepository;

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
    public List<com.hms.profile.dto.DoctorDropdown> getDoctorDropdown() throws HmsException {
        return doctorRepository.findAllDoctorDropdown();
    }

    @Override
    public List<DoctorDTO> getAllDoctors() {
        return doctorRepository.findAll().stream().map(d -> d.toDoctorDTO()).toList();
    }

    @Override
    public List<DoctorName> getDoctorNamesByIds(List<Long> ids) {

        return doctorRepository.findAllById(ids)
                .stream()
                .map(doctor -> new DoctorName(doctor.getId(), doctor.getName()))
                .toList();
    }

    @Override
    public long count() {
        return doctorRepository.count();
    }

    @Override
    public Map<String, Long> getDoctorsByDept() {
        return doctorRepository.countByDepartment().stream()
                .collect(Collectors.toMap(
                    row -> row[0] == null ? "General" : (String) row[0],
                    row -> (Long) row[1]
                ));
    }
    @Override
    public Boolean doctorExists(Long id) throws HmsException {
        return doctorRepository.existsById(id);
    }
}
