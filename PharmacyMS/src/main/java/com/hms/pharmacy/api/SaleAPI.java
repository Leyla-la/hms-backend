package com.hms.pharmacy.api;

import com.hms.pharmacy.dto.SaleDTO;
import com.hms.pharmacy.dto.SaleItemDTO;
import com.hms.pharmacy.exception.HmsException;
import com.hms.pharmacy.service.SaleService;
import com.hms.pharmacy.service.SaleItemService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/pharmacy/sales")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Pharmacy Sales", description = "Endpoints for processing medicine sales and retrieving transaction history.")
public class SaleAPI {
    final SaleService saleService;
    final SaleItemService saleItemService;

    @Operation(summary = "Record a new sale", description = "Processes a sales transaction and reduces inventory stock accordingly.")
    @PostMapping("/add")
    public ResponseEntity<Long> addSale(@RequestBody SaleDTO saleDTO) throws HmsException {
        return new ResponseEntity<>(saleService.createSale(saleDTO), HttpStatus.CREATED);
    }

    @Operation(summary = "Get sale by ID", description = "Retrieves details of a specific sales transaction.")
    @GetMapping("/get/{id}")
    public ResponseEntity<SaleDTO> getSaleById(@PathVariable("id") Long id) throws HmsException {
        return ResponseEntity.ok(saleService.getSaleById(id));
    }

    @Operation(summary = "Get all sales (Paginated)", description = "Retrieves a paginated list of all past pharmacy sales.")
    @GetMapping("/getAll")
    public ResponseEntity<Page<SaleDTO>> getAllSales(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws HmsException {
        return ResponseEntity.ok(saleService.getAllSales(PageRequest.of(page, size)));
    }

    @Operation(summary = "Get sale line items", description = "Retrieves all individual medicines and quantities within a specific sale transaction.")
    @GetMapping("/getSaleItems/{id}")
    public ResponseEntity<List<SaleItemDTO>> getSaleItemsBySaleId(@PathVariable("id") Long id) throws HmsException {
        return ResponseEntity.ok(saleItemService.getSaleItemsBySaleId(id));
    }
}
