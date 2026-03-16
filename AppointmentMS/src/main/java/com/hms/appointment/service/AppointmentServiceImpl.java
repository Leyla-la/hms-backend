package com.hms.appointment.service;

import com.hms.appointment.clients.ProfileClient;
import com.hms.appointment.dto.*;
import com.hms.appointment.entity.Appointment;
import com.hms.appointment.exception.HmsException;
import com.hms.appointment.repository.AppointmentRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppointmentServiceImpl implements AppointmentService {
    AppointmentRepository appointmentRepository;
    ApiService apiService;
    ProfileClient profileClient;


    @Override
    public Long scheduleAppointment(AppointmentDTO appointmentDTO) throws HmsException {
        Boolean doctorExists = profileClient.doctorExists(appointmentDTO.getDoctorId());
        if (doctorExists == null || !doctorExists) {
            throw new HmsException("DOCTOR_NOT_FOUND");
        }
            Boolean patientExists = profileClient.patientExists(appointmentDTO.getPatientId());
        if (patientExists == null || !patientExists) {
            throw new HmsException("PATIENT_NOT_FOUND");
        }
        return appointmentRepository.save(appointmentDTO.toAppointment()).getId();
    }

    @Override
    public void cancelAppointment(Long appointmentId) throws HmsException{
        Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow(() -> new HmsException("APPOINTMENT_NOT_FOUND"));
        if (appointment.getStatus().equals(Status.CANCELLED)) {
            throw new HmsException("APPOINTMENT_ALREADY_CANCELED");
        } else if (appointment.getStatus().equals(Status.COMPLETED)) {
            throw new HmsException("APPOINTMENT_ALREADY_COMPLETED");
        }

        appointment.setStatus(Status.CANCELLED);
        appointmentRepository.save(appointment);
    }

    @Override
    public void completeAppointment(Long appointmentId) {

    }

    @Override
    public void rescheduleAppointment(Long appointmentId, String newAppointmentDate) {

    }

    @Override
    public AppointmentDTO getAppointmentDetails(Long appointmentId) throws HmsException {
        return appointmentRepository.findById(appointmentId).orElseThrow(() -> new HmsException("APPOINTMENT_NOT_FOUND")).toAppointmentDTO();
    }

    @Override
    public AppointmentDetails getAppointmentDetailsWithName(Long appointmentId) throws HmsException {
        AppointmentDTO appointmentDTO = appointmentRepository.findById(appointmentId).orElseThrow(() -> new HmsException("APPOINTMENT_NOT_FOUND")).toAppointmentDTO();
        DoctorDTO doctorDTO = profileClient.getDoctorById(appointmentDTO.getDoctorId());
        PatientDTO patientDTO = profileClient.getPatientById(appointmentDTO.getPatientId());
        return new AppointmentDetails(appointmentDTO.getId(), appointmentDTO.getPatientId(), patientDTO.getName(), patientDTO.getEmail(), patientDTO.getPhone(), patientDTO.getCitizenId(), appointmentDTO.getDoctorId(), doctorDTO.getName(), doctorDTO.getEmail(), doctorDTO.getPhone(), doctorDTO.getSpecialization(), doctorDTO.getLicenseNo(), appointmentDTO.getAppointmentTime(), appointmentDTO.getStatus(), appointmentDTO.getReason(), appointmentDTO.getNotes());

    }

    @Override
    public List<AppointmentDetails> getAllAppointmentsByPatientId(Long patientId) throws HmsException {
        return appointmentRepository.findAllByPatientId(patientId).stream()
                .map(appointment -> {
                    DoctorDTO doctorDTO = profileClient.getDoctorById(appointment.getDoctorId());
                    appointment.setDoctorName(doctorDTO.getName());
                    return appointment;
                }).toList();
    }

    @Override
    public List<AppointmentDetails> getAllAppointmentsByDoctorId(Long doctorId) throws HmsException {
        return appointmentRepository.findAllByDoctorId(doctorId).stream()
                .map(appointment -> {
                    PatientDTO patientDTO = profileClient.getPatientById(appointment.getPatientId());
                    appointment.setPatientName(patientDTO.getName());
                    appointment.setPatientEmail(patientDTO.getEmail());
                    appointment.setPatientPhone(patientDTO.getPhone());
                    return appointment;
                }).toList();
    }
}
