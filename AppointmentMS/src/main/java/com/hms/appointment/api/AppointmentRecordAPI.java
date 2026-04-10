package com.hms.appointment.api;

import com.hms.appointment.dto.AppointmentRecordDTO;
import com.hms.appointment.dto.MedicineDTO;
import com.hms.appointment.dto.PrescriptionDetails;
import com.hms.appointment.exception.HmsException;
import com.hms.appointment.service.AppointmentRecordService;
import com.hms.appointment.service.MedicineService;
import com.hms.appointment.service.PrescriptionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import java.util.List;

@RestController
@RequestMapping("/appointments/record")
@Validated
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Appointment Record Management", description = "Endpoints for creating and retrieving medical records, prescriptions, and follow-up information.")
public class AppointmentRecordAPI {
    AppointmentRecordService appointmentRecordService;
    PrescriptionService prescriptionService;
    MedicineService medicineService;

    private static final Logger log = LoggerFactory.getLogger(AppointmentRecordAPI.class);

    @Operation(summary = "Create an appointment record", description = "Stores clinical details, symptoms, and diagnosis for a completed appointment.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Record created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid record data")
    })
    @PostMapping("/create")
    public ResponseEntity<Long> createAppointmentRecord(@RequestBody AppointmentRecordDTO appointmentRecordDTO) throws HmsException {
        try {
            log.info("[createAppointmentRecord] Incoming DTO: {}", appointmentRecordDTO);
            Long id = appointmentRecordService.createAppointmentRecord(appointmentRecordDTO);
            log.info("[createAppointmentRecord] Created appointment record id={}", id);
            return new ResponseEntity<>(id, HttpStatus.CREATED);
        } catch (HmsException he) {
            log.error("[createAppointmentRecord] HmsException while creating appointment record: {}", he.getMessage(), he);
            throw he;
        } catch (RuntimeException re) {
            log.error("[createAppointmentRecord] Unexpected error while creating appointment record", re);
            throw re;
        }
    }

    @Operation(summary = "Update an appointment record", description = "Updates clinical details or diagnosis for an existing medical record.")
    @ApiResponse(responseCode = "200", description = "Record updated successfully")
    @PutMapping("/update")
    public ResponseEntity<String> updateAppointmentRecord(@RequestBody AppointmentRecordDTO appointmentRecordDTO) throws HmsException {
        try {
            log.info("[updateAppointmentRecord] Incoming DTO: {}", appointmentRecordDTO);
            appointmentRecordService.updateAppointmentRecord(appointmentRecordDTO);
            log.info("[updateAppointmentRecord] Updated appointment record id={}", appointmentRecordDTO.getId());
            return new ResponseEntity<>("Appointment record updated", HttpStatus.OK);
        } catch (HmsException he) {
            log.error("[updateAppointmentRecord] HmsException while updating appointment record: {}", he.getMessage(), he);
            throw he;
        } catch (RuntimeException re) {
            log.error("[updateAppointmentRecord] Unexpected error while updating appointment record", re);
            throw re;
        }
    }

    @Operation(summary = "Get record by appointment ID", description = "Retrieves the medical record associated with a specific appointment.")
    @GetMapping("/getRecordByAppointmentId/{appointmentId}")
    public ResponseEntity<AppointmentRecordDTO> getAppointmentRecordByAppointmentId(@PathVariable("appointmentId") Long appointmentId) throws HmsException {
        try {
            log.info("[getAppointmentRecordByAppointmentId] appointmentId={}", appointmentId);
            AppointmentRecordDTO dto = appointmentRecordService.getAppointmentRecordByAppointmentId(appointmentId);
            log.info("[getAppointmentRecordByAppointmentId] result={}", dto);
            return new ResponseEntity<>(dto, HttpStatus.OK);
        } catch (HmsException he) {
            log.error("[getAppointmentRecordByAppointmentId] HmsException for appointmentId={}: {}", appointmentId, he.getMessage(), he);
            throw he;
        } catch (RuntimeException re) {
            log.error("[getAppointmentRecordByAppointmentId] Unexpected error for appointmentId={}", appointmentId, re);
            throw re;
        }
    }

    @Operation(summary = "Get record by record ID", description = "Retrieves a specific medical record by its internal unique ID.")
    @GetMapping("/getById/{appointmentRecordId}")
    public ResponseEntity<AppointmentRecordDTO> getAppointmentRecordById(@PathVariable("appointmentRecordId") Long appointmentRecordId) throws HmsException {
        try {
            log.info("[getAppointmentRecordById] appointmentRecordId={}", appointmentRecordId);
            AppointmentRecordDTO dto = appointmentRecordService.getAppointmentRecordById(appointmentRecordId);
            log.info("[getAppointmentRecordById] result={}", dto);
            return new ResponseEntity<>(dto, HttpStatus.OK);
        } catch (HmsException he) {
            log.error("[getAppointmentRecordById] HmsException for id={}: {}", appointmentRecordId, he.getMessage(), he);
            throw he;
        } catch (RuntimeException re) {
            log.error("[getAppointmentRecordById] Unexpected error for id={}", appointmentRecordId, re);
            throw re;
        }
    }

    @Operation(summary = "Get all records for a patient", description = "Retrieves all historical medical records for a specific patient.")
    @GetMapping("/getRecordsByPatientId/{patientId}")
    public ResponseEntity<List<AppointmentRecordDTO>> getAppointmentRecordsByPatientId(@PathVariable("patientId") String patientIdStr, HttpServletRequest request) throws HmsException {
        if ("undefined".equals(patientIdStr)) {
            log.warn("[getAppointmentRecordsByPatientId] Received 'undefined' patientId from frontend");
            return new ResponseEntity<>(java.util.Collections.emptyList(), HttpStatus.OK);
        }
        try {
            Long patientId = Long.valueOf(patientIdStr);
            log.info("[getAppointmentRecordsByPatientId] patientId={}", patientId);
            List<AppointmentRecordDTO> dtoList = appointmentRecordService.getAppointmentRecordsByPatientId(patientId);
            return new ResponseEntity<>(dtoList, HttpStatus.OK);
        } catch (Exception ex) {
            log.error("[getAppointmentRecordsByPatientId] Error processing request: {}", ex.getMessage());
            return new ResponseEntity<>(java.util.Collections.emptyList(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Check if record exists", description = "Checks whether a clinical record has already been created for a given appointment ID.")
    @GetMapping("/isRecordExists/{appointmentId}")
    public ResponseEntity<Boolean> isAppointmentRecordExists(@PathVariable("appointmentId") String appointmentIdStr) throws HmsException {
        if ("undefined".equals(appointmentIdStr)) {
            log.warn("[isAppointmentRecordExists] Received 'undefined' appointmentId from frontend");
            return new ResponseEntity<>(false, HttpStatus.OK);
        }
        try {
            Long appointmentId = Long.valueOf(appointmentIdStr);
            log.info("[isAppointmentRecordExists] appointmentId={}", appointmentId);
            Boolean exists = appointmentRecordService.isAppointmentRecordExists(appointmentId);
            return new ResponseEntity<>(exists, HttpStatus.OK);
        } catch (Exception e) {
            log.error("[isAppointmentRecordExists] Error checking record existence: {}", e.getMessage());
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Get prescriptions for a patient", description = "Retrieves all medical prescriptions issued to a specific patient.")
    @GetMapping("/getPrescriptionsByPatientId/{patientId}")
    public ResponseEntity<List<PrescriptionDetails>> getPrescriptionsByPatientId(@PathVariable("patientId") Long patientId) throws HmsException {
        try {
            log.info("[getPrescriptionsByPatientId] patientId={}", patientId);
            List<PrescriptionDetails> list = prescriptionService.getPrescriptionsByPatientId(patientId);
            log.info("[getPrescriptionsByPatientId] patientId={} prescriptionsCount={} sample={}", patientId, list == null ? 0 : list.size(), list != null && !list.isEmpty() ? list.get(0) : "(none)");
            return new ResponseEntity<>(list, HttpStatus.OK);
        } catch (RuntimeException re) {
            log.error("[getPrescriptionsByPatientId] Unexpected error for patientId={}", patientId, re);
            throw re;
        }
    }

    @Operation(summary = "Get prescriptions for a doctor", description = "Retrieves all medical prescriptions issued by a specific doctor.")
    @GetMapping("/getPrescriptionsByDoctorId/{doctorId}")
    public ResponseEntity<List<PrescriptionDetails>> getPrescriptionsByDoctorId(@PathVariable("doctorId") Long doctorId) throws HmsException {
        try {
            log.info("[getPrescriptionsByDoctorId] doctorId={}", doctorId);
            List<PrescriptionDetails> list = prescriptionService.getPrescriptionsByDoctorId(doctorId);
            return new ResponseEntity<>(list, HttpStatus.OK);
        } catch (HmsException he) {
            log.error("[getPrescriptionsByDoctorId] error: {}", he.getMessage());
            throw he;
        }
    }

    /** Returns only appointment records where followUpDate is set (ordered ascending). */
    @Operation(summary = "Get follow-ups for a patient", description = "Retrieves a list of medical records that have a scheduled follow-up date for a patient.")
    @GetMapping("/getFollowUpsByPatientId/{patientId}")
    public ResponseEntity<List<AppointmentRecordDTO>> getFollowUpsByPatientId(@PathVariable("patientId") Long patientId) throws HmsException {
        log.info("[getFollowUpsByPatientId] patientId={}", patientId);
        List<AppointmentRecordDTO> list = appointmentRecordService.getFollowUpsByPatientId(patientId);
        log.info("[getFollowUpsByPatientId] patientId={} count={}", patientId, list.size());
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @Operation(summary = "Get follow-ups for a doctor", description = "Retrieves a list of medical records that have a scheduled follow-up date for a specific doctor.")
    @GetMapping("/getFollowUpsByDoctorId/{doctorId}")
    public ResponseEntity<List<AppointmentRecordDTO>> getFollowUpsByDoctorId(@PathVariable("doctorId") Long doctorId) throws HmsException {
        log.info("[getFollowUpsByDoctorId] doctorId={}", doctorId);
        List<AppointmentRecordDTO> list = appointmentRecordService.getFollowUpsByDoctorId(doctorId);
        log.info("[getFollowUpsByDoctorId] doctorId={} count={}", doctorId, list.size());
        return new ResponseEntity<>(list, HttpStatus.OK);
    }
    @Operation(summary = "Get all prescriptions (Paginated)", description = "Retrieves a paginated list of all prescriptions in the system.")
    @GetMapping("/getAllPrescriptions")
    public ResponseEntity<Page<PrescriptionDetails>> getAllPrescriptions(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) throws HmsException {
        try {
            log.info("[getAllPrescriptions] called with pageable={}", pageable);
            Page<PrescriptionDetails> page = prescriptionService.getPrescriptions(pageable);
            return new ResponseEntity<>(page, HttpStatus.OK);
        } catch (Exception ex) {
            log.error("[getAllPrescriptions] Error: {}", ex.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(summary = "Get all prescriptions (Raw List)", description = "Retrieves a full list of all prescriptions without pagination.")
    @GetMapping("/getAllPrescriptionsList")
    public ResponseEntity<List<PrescriptionDetails>> getAllPrescriptionsList() throws HmsException {
        try {
            log.info("[getAllPrescriptionsList] Fetching all prescriptions list");
            List<PrescriptionDetails> list = prescriptionService.getAllPrescriptionsList();
            log.info("[getAllPrescriptionsList] Returned {} items", list.size());
            return new ResponseEntity<>(list, HttpStatus.OK);
        } catch (HmsException he) {
            log.error("[getAllPrescriptionsList] HmsException: {}", he.getMessage(), he);
            throw he;
        } catch (RuntimeException re) {
            log.error("[getAllPrescriptionsList] Unexpected error", re);
            throw re;
        }
    }

    @Operation(summary = "Get medicines by prescription ID", description = "Retrieves the list of specific medicines prescribed in a particular prescription record.")
    @GetMapping("/getMedicinesByPrescriptionId/{prescriptionId}")
    public ResponseEntity<List<MedicineDTO>> getMedicinesByPrescriptionId(@PathVariable("prescriptionId") Long prescriptionId) throws HmsException {
        try {
            log.info("[getMedicinesByPrescriptionId] prescriptionId={}", prescriptionId);
            List<MedicineDTO> list = medicineService.getAllMedicinesByPrescriptionId(prescriptionId);
            log.info("[getMedicinesByPrescriptionId] prescriptionId={} medicinesCount={} sample={}", prescriptionId, list == null ? 0 : list.size(), list != null && !list.isEmpty() ? list.get(0) : "(none)");
            return new ResponseEntity<>(list, HttpStatus.OK);
        } catch (HmsException he) {
            log.error("[getMedicinesByPrescriptionId] HmsException for prescriptionId={}: {}", prescriptionId, he.getMessage(), he);
            throw he;
        } catch (RuntimeException re) {
            log.error("[getMedicinesByPrescriptionId] Unexpected error for prescriptionId={}", prescriptionId, re);
            throw re;
        }
    }

    private String truncate(String s) {
        if (s == null) return "null";
        int max = 200;
        if (s.length() <= max) return s;
        return s.substring(0, max) + "...[truncated]";
    }

}

