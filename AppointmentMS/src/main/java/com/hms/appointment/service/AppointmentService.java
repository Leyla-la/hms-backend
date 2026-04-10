package com.hms.appointment.service;


import com.hms.appointment.dto.AppointmentDTO;
import com.hms.appointment.dto.AppointmentDetails;
import com.hms.appointment.dto.AppointmentSummaryDTO;
import com.hms.appointment.dto.TrendDTO;
import com.hms.appointment.exception.HmsException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface AppointmentService {
    Long scheduleAppointment(AppointmentDTO appointmentDTO, Map<String, String> actor) throws HmsException;
    void cancelAppointment(Long appointmentId, Map<String, String> actor) throws HmsException;
    void completeAppointment(Long appointmentId, Map<String, String> actor) throws HmsException;
    void rescheduleAppointment(Long appointmentId, String newAppointmentDate) throws HmsException;
    AppointmentDTO getAppointmentDetails(Long appointmentId) throws HmsException;
    AppointmentDetails getAppointmentDetailsWithName(Long appointmentId) throws HmsException;
    List<AppointmentDetails> getAllAppointmentsByPatientId(Long patientId) throws HmsException;
    List<AppointmentDetails> getAllAppointmentsByDoctorId(Long doctorId) throws HmsException;
    List<AppointmentDTO> getAppointmentsByPatientId(Long patientId) throws HmsException;
    List<AppointmentDTO> getMedicalHistoryByPatientId(Long patientId) throws HmsException;
    List<AppointmentDTO> getAppointmentsByDoctorId(Long doctorId) throws HmsException;

    // dashboard methods
    AppointmentSummaryDTO getSummaryForDoctor(Long doctorId) throws HmsException;
    List<AppointmentDTO> getUpcomingForPatient(Long patientId, int limit) throws HmsException;
    List<AppointmentDetails> getUpcomingWithDetailsForPatient(Long patientId, int limit) throws HmsException;
    List<AppointmentDTO> getTodayForDoctor(Long doctorId) throws HmsException;
    List<AppointmentDTO> getTodayAll() throws HmsException;
    TrendDTO getTrend(LocalDate from, LocalDate to) throws HmsException;
    TrendDTO getTrendForPatient(Long patientId, LocalDate from, LocalDate to) throws HmsException;
    long count();
}
