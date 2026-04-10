package com.hms.profile.service;

import com.hms.profile.dto.DoctorDTO;
import com.hms.profile.exception.HmsException;
import java.util.List;
import com.hms.profile.dto.DoctorName;

public interface DoctorService {
    public Long addDoctor(DoctorDTO doctorDTO) throws HmsException;
    public DoctorDTO getDoctorById(Long doctorId) throws HmsException;
    DoctorDTO updateDoctor(DoctorDTO doctorDTO) throws HmsException;
    List<com.hms.profile.dto.DoctorDropdown> getDoctorDropdown() throws HmsException;
    List<DoctorDTO> getAllDoctors();
    List<DoctorName> getDoctorNamesByIds(List<Long> ids);
    long count();
    java.util.Map<String, Long> getDoctorsByDept();
    Boolean doctorExists(Long id) throws HmsException;
}
