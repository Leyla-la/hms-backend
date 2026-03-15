package com.hms.profile.service;

import com.hms.profile.dto.DoctorDTO;
import com.hms.profile.exception.HmsException;

public interface DoctorService {
    public Long addDoctor(DoctorDTO doctorDTO) throws HmsException;
    public DoctorDTO getDoctorById(Long doctorId) throws HmsException;
    DoctorDTO updateDoctor(DoctorDTO doctorDTO) throws HmsException;
    public Boolean doctorExists(Long doctorId) throws HmsException;
}
