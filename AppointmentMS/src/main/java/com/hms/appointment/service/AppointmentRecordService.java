package com.hms.appointment.service;
import java.util.List;
import com.hms.appointment.dto.AppointmentRecordDTO;
import com.hms.appointment.exception.HmsException;

public interface AppointmentRecordService {
    Long createAppointmentRecord(AppointmentRecordDTO appointmentRecordDTO) throws HmsException;
    void updateAppointmentRecord(AppointmentRecordDTO appointmentRecordDTO) throws HmsException;
    AppointmentRecordDTO getAppointmentRecordByAppointmentId(Long appointmentId) throws HmsException;
    AppointmentRecordDTO getAppointmentRecordById(Long appointmentRecordId) throws HmsException;
    AppointmentRecordDTO getAppointmentRecordDetailsByAppointmentId(Long appointmentId) throws HmsException;
    List<AppointmentRecordDTO> getAppointmentRecordsByPatientId(Long patientId) throws HmsException;
    boolean isAppointmentRecordExists(Long appointmentId) throws HmsException;
    List<AppointmentRecordDTO> getFollowUpsByPatientId(Long patientId) throws HmsException;
    List<AppointmentRecordDTO> getFollowUpsByDoctorId(Long doctorId) throws HmsException;
}
