package com.hms.pharmacy.api;

import com.hms.pharmacy.dto.MedicineInventoryDTO;
import com.hms.pharmacy.dto.ResponseDTO;
import com.hms.pharmacy.exception.HmsException;
import com.hms.pharmacy.service.MedicineInventoryService;
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
@RequestMapping("/pharmacy/inventory")
@Validated
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Pharmacy Inventory", description = "Endpoints for managing stock levels, batches, and inventory statistics.")
public class MedicineInventoryAPI {
    MedicineInventoryService medicineInventoryService;

    @Operation(summary = "Add inventory batch", description = "Adds a specific batch of medicine stock to the inventory.")
    @PostMapping("/add")
    public ResponseEntity<MedicineInventoryDTO> addMedicineInventory(@RequestBody MedicineInventoryDTO medicineInventoryDTO) throws HmsException {
        return new ResponseEntity<>(medicineInventoryService.addMedicine(medicineInventoryDTO), HttpStatus.CREATED);
    }

    @Operation(summary = "Update inventory record", description = "Updates stock quantity or batch details for an inventory item.")
    @PutMapping("/update")
    public ResponseEntity<MedicineInventoryDTO> updateMedicineInventory(@RequestBody MedicineInventoryDTO medicineInventoryDTO) throws HmsException {
        return new ResponseEntity<>(medicineInventoryService.updateMedicine(medicineInventoryDTO), HttpStatus.OK);
    }

    @Operation(summary = "Get inventory by ID", description = "Retrieves specific inventory details by its unique internal ID.")
    @GetMapping("/get/{id}")
    public ResponseEntity<MedicineInventoryDTO> getMedicineInventoryById(@PathVariable("id") Long id) throws HmsException {
        return new ResponseEntity<>(medicineInventoryService.getMedicineById(id), HttpStatus.OK);
    }

    @Operation(summary = "Get all inventory (Paginated)", description = "Retrieves a paginated list of all stock items in the pharmacy inventory.")
    @GetMapping("/getAll")
    public ResponseEntity<Page<MedicineInventoryDTO>> getAllMedicineInventories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws HmsException {
        org.springframework.data.domain.Pageable pageable = (size == -1) 
                ? org.springframework.data.domain.Pageable.unpaged() 
                : PageRequest.of(page, size);
        return new ResponseEntity<>(medicineInventoryService.getAllMedicines(pageable), HttpStatus.OK);
    }

    @Operation(summary = "Delete inventory record", description = "Permanently removes an inventory batch record from the system.")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ResponseDTO> deleteMedicineInventory(@PathVariable("id") Long id) throws HmsException {
        medicineInventoryService.deleteMedicine(id);
        return new ResponseEntity<>(new ResponseDTO("Medicine inventory deleted"), HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Get total inventory count", description = "Returns the total number of unique inventory records (batches).")
    @GetMapping("/count")
    public long count() {
        return medicineInventoryService.count();
    }

    @Operation(summary = "Get low stock count", description = "Returns the number of medicine batches where stock level is below the specified threshold.")
    @GetMapping("/low-stock")
    public long getLowStockCount(@RequestParam(value = "threshold", defaultValue = "10") Integer threshold) {
        return medicineInventoryService.getLowStockCount(threshold);
    }

    @Operation(summary = "Get pharmacy statistics", description = "Provides an overview of pharmacy health, including total items, low stock alerts, and top-selling medicines.")
    @GetMapping("/statistics/overview")
    public ResponseEntity<com.hms.pharmacy.dto.PharmacyStatsDTO> getPharmacyStats() throws HmsException {
        return new ResponseEntity<>(medicineInventoryService.getPharmacyStats(), HttpStatus.OK);
    }
}
