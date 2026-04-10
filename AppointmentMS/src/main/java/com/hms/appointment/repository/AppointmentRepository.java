package com.hms.appointment.repository;

import com.hms.appointment.dto.AppointmentDetails;
import com.hms.appointment.entity.Appointment;
import com.hms.appointment.dto.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findByDoctorId(Long doctorId);

    @Query("SELECT new com.hms.appointment.dto.AppointmentDetails(" +
           "a.id, a.patientId, null, null, null, null, " +
           "a.doctorId, null, null, null, null, null, " +
           "a.appointmentTime, a.status, a.reason, a.notes) " +
           "FROM Appointment a WHERE a.patientId = :patientId")
    List<AppointmentDetails> findAllByPatientId(@Param("patientId") Long patientId);

    @Query("SELECT new com.hms.appointment.dto.AppointmentDetails(" +
           "a.id, a.patientId, null, null, null, null, " +
           "a.doctorId, null, null, null, null, null, " +
           "a.appointmentTime, a.status, a.reason, a.notes) " +
           "FROM Appointment a WHERE a.doctorId = :doctorId")
    List<AppointmentDetails> findAllByDoctorId(@Param("doctorId") Long doctorId);

    List<Appointment> findAllByStatusAndAppointmentTimeBefore(Status status, LocalDateTime time);

    List<Appointment> findByPatientIdAndStatusOrderByAppointmentTimeDesc(Long patientId, Status status);

    @Query("SELECT a FROM Appointment a WHERE a.patientId = :patientId AND a.appointmentTime > :now AND a.status = com.hms.appointment.dto.Status.SCHEDULED ORDER BY a.appointmentTime ASC")
    List<Appointment> findUpcomingByPatient(@Param("patientId") Long patientId, @Param("now") LocalDateTime now);

    @Query("SELECT a FROM Appointment a WHERE a.doctorId = :doctorId AND a.appointmentTime >= :start AND a.appointmentTime < :end ORDER BY a.appointmentTime ASC")
    List<Appointment> findTodayByDoctor(@Param("doctorId") Long doctorId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT a FROM Appointment a WHERE a.appointmentTime >= :start AND a.appointmentTime < :end ORDER BY a.appointmentTime ASC")
    List<Appointment> findTodayAll(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT FUNCTION('DATE', a.appointmentTime), COUNT(a) FROM Appointment a " +
           "WHERE a.appointmentTime >= :from AND a.appointmentTime < :to " +
           "GROUP BY FUNCTION('DATE', a.appointmentTime) ORDER BY FUNCTION('DATE', a.appointmentTime)")
    List<Object[]> countGroupByDate(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT FUNCTION('DATE', a.appointmentTime), COUNT(a) FROM Appointment a " +
           "WHERE a.patientId = :patientId AND a.appointmentTime >= :from AND a.appointmentTime < :to " +
           "GROUP BY FUNCTION('DATE', a.appointmentTime) ORDER BY FUNCTION('DATE', a.appointmentTime)")
    List<Object[]> countGroupByDateForPatient(@Param("patientId") Long patientId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT a.status, COUNT(a) FROM Appointment a GROUP BY a.status")
    List<Object[]> countByStatusAll();
}