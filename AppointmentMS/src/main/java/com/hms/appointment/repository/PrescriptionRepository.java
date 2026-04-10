package com.hms.appointment.repository;

import com.hms.appointment.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    Optional<Prescription> findByAppointment_Id(Long appointmentId);
    
    @Query("SELECT p FROM Prescription p LEFT JOIN FETCH p.appointment WHERE p.patientId = :patientId ORDER BY p.id DESC")
    List<Prescription> findAllByPatientId(Long patientId);

    @Query("SELECT p FROM Prescription p LEFT JOIN FETCH p.appointment WHERE p.doctorId = :doctorId ORDER BY p.id DESC")
    List<Prescription> findAllByDoctorId(Long doctorId);
    
    // Efficiently fetch recent prescriptions for Sales modal
    List<Prescription> findTop50ByOrderByIdDesc();

    @Query("SELECT p FROM Prescription p LEFT JOIN FETCH p.appointment ORDER BY p.id DESC")
    List<Prescription> findAllOptimized();
}
