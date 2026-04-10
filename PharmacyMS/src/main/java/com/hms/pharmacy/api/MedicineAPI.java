package com.hms.pharmacy.api;

import com.hms.pharmacy.dto.MedicineDTO;
import com.hms.pharmacy.dto.ResponseDTO;
import com.hms.pharmacy.exception.HmsException;
import com.hms.pharmacy.service.MedicineService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/pharmacy/medicines")
@Validated
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Medicine Catalog", description = "Endpoints for managing the master list of medicines available in the pharmacy.")
public class MedicineAPI {

    MedicineService medicineService;

    @Operation(summary = "Add a new medicine", description = "Adds a new medicine entry to the master catalog.")
    @PostMapping("/add")
    public ResponseEntity<Long> addMedicine(@RequestBody MedicineDTO medicineDTO) throws HmsException {
        return new ResponseEntity<>(medicineService.addMedicine(medicineDTO), HttpStatus.CREATED);
    }

    @Operation(summary = "Get medicine by ID", description = "Retrieves specific medicine details from the catalog by its ID.")
    @GetMapping("/get/{id}")
    public ResponseEntity<MedicineDTO> getMedicineById(@PathVariable("id") Long id) throws HmsException {
        return new ResponseEntity<>(medicineService.getMedicineById(id), HttpStatus.OK);
    }

    @Operation(summary = "Update medicine details", description = "Updates an existing medicine's metadata in the catalog.")
    @PutMapping("/update")
    public ResponseEntity<ResponseDTO> updateMedicine(@RequestBody MedicineDTO medicineDTO) throws HmsException {
        medicineService.updateMedicine(medicineDTO);
        return new ResponseEntity<>(new ResponseDTO("Medicine Update"), HttpStatus.OK);
    }

    @Operation(summary = "Get all medicines (Paginated)", description = "Retrieves a paginated list of all medicines in the catalog.")
    @GetMapping("/getAll")
    public ResponseEntity<Page<MedicineDTO>> getAllMedicines(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws HmsException {
        org.springframework.data.domain.Pageable pageable = (size == -1) 
                ? org.springframework.data.domain.Pageable.unpaged() 
                : PageRequest.of(page, size);
        return new ResponseEntity<>(medicineService.getAllMedicines(pageable), HttpStatus.OK);
    }
}
