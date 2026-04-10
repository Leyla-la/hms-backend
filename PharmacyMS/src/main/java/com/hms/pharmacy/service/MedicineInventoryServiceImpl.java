package com.hms.pharmacy.service;

import com.hms.pharmacy.dto.PharmacyStatsDTO;
import com.hms.pharmacy.dto.SaleDTO;
import com.hms.pharmacy.dto.StockStatus;
import com.hms.pharmacy.entity.Medicine;
import com.hms.pharmacy.entity.MedicineInventory;
import com.hms.pharmacy.dto.MedicineInventoryDTO;
import com.hms.pharmacy.exception.HmsException;
import com.hms.pharmacy.repository.MedicineInventoryRepository;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MedicineInventoryServiceImpl implements MedicineInventoryService {

    MedicineInventoryRepository medicineInventoryRepository;
    MedicineService medicineService;
    SaleService saleService;

    @Override
    public Page<MedicineInventoryDTO> getAllMedicines(Pageable pageable) throws HmsException {
        return medicineInventoryRepository.findAll(pageable)
                .map(inventory -> {
                    MedicineInventoryDTO dto = inventory.toMedicineInventoryDTO();
                    if (inventory.getMedicine() != null) {
                        dto.setMedicineName(inventory.getMedicine().getName());
                        dto.setManufacturer(inventory.getMedicine().getManufacturer());
                    }
                    return dto;
                });
    }

    @Override
    public MedicineInventoryDTO getMedicineById(Long id) throws HmsException {
        return medicineInventoryRepository.findById(id)
                .map(MedicineInventory::toMedicineInventoryDTO)
                .orElseThrow(() -> new HmsException("MEDICINE_NOT_FOUND"));
    }

    @Override
    public MedicineInventoryDTO addMedicine(MedicineInventoryDTO medicineInventoryDTO) throws HmsException {
        medicineInventoryDTO.setAddedDate(LocalDate.now());
        medicineService.addStock(medicineInventoryDTO.getMedicineId(), medicineInventoryDTO.getQuantity());
        medicineInventoryDTO.setInitialQuantity(medicineInventoryDTO.getQuantity());
        medicineInventoryDTO.setStatus(StockStatus.ACTIVE);
        MedicineInventory saved = medicineInventoryRepository.save(medicineInventoryDTO.toMedicineInventory());
        return saved.toMedicineInventoryDTO();
    }

    @Override
    public MedicineInventoryDTO updateMedicine(MedicineInventoryDTO medicine) throws HmsException {
        MedicineInventory existing = medicineInventoryRepository.findById(medicine.getId())
                .orElseThrow(() -> new HmsException("MEDICINE_NOT_FOUND"));
        // update fields
        existing.setBatchNo(medicine.getBatchNo());
        existing.setExpiryDate(medicine.getExpiryDate());
        existing.setQuantity(medicine.getQuantity());
        existing.setAddedDate(medicine.getAddedDate());

        if (existing.getQuantity() < medicine.getQuantity()) {
            medicineService.addStock(medicine.getMedicineId(), medicine.getQuantity() - existing.getQuantity());
        } else if (existing.getQuantity() > medicine.getQuantity()) {
            medicineService.removeStock(medicine.getMedicineId(), existing.getQuantity() - medicine.getQuantity());
        }

        existing.setInitialQuantity(medicine.getQuantity());
        // medicine reference (ManyToOne) - only update id
        existing.setMedicine(new Medicine(medicine.getMedicineId()));
        MedicineInventory saved = medicineInventoryRepository.save(existing);
        return saved.toMedicineInventoryDTO();
    }

    @Override
    public void deleteMedicine(Long id) throws HmsException {
        if (!medicineInventoryRepository.existsById(id)) {
            throw new HmsException("MEDICINE_NOT_FOUND");
        }
        medicineInventoryRepository.deleteById(id);
    }

    @Override
    @Scheduled(cron = "0 30 14 * * ?")
    public void deleteExpiredMedicines() throws HmsException {
        System.out.println("Deleting expired medicines at: " + LocalDateTime.now());
        List<MedicineInventory> expiredMedicines = medicineInventoryRepository.findByExpiryDateBefore(LocalDate.now());
        for (MedicineInventory medicine : expiredMedicines) {
            medicineService.removeStock(medicine.getMedicine().getId(), medicine.getQuantity());
        }
        this.markExpired(expiredMedicines);
    }

    private void markExpired(List<MedicineInventory> inventories) throws HmsException {
        for (MedicineInventory inventory : inventories) {
            inventory.setStatus(StockStatus.EXPIRED);
        }

        medicineInventoryRepository.saveAll(inventories);
    }

    @Override
    public long count() {
        return medicineService.count();
    }

    @Override
    public long getLowStockCount(Integer threshold) {
        return medicineService.getLowStockCount(threshold);
    }

    @Override
    public PharmacyStatsDTO getPharmacyStats() throws HmsException {
        long totalMedicines = medicineService.count();
        long lowStockCount = medicineService.getLowStockCount(10);
        long totalInventoryRecords = medicineInventoryRepository.count();

        log.info("[PharmacyStats] Aggregating: totalMeds={}, lowStock={}, totalInventory={}", 
            totalMedicines, lowStockCount, totalInventoryRecords);
        
        // If medicineService.count is returning 0 but inventory has items, 
        // fallback to counting distinct medicines in inventory to ensure dashboard isn't empty.
        if (totalMedicines == 0 && totalInventoryRecords > 0) {
            log.warn("[PharmacyStats] Medicine table count is 0 but Inventory has data. Using inventory record count for totalMedicines.");
            totalMedicines = totalInventoryRecords;
        }
        
        // Top medicines (based on actual sales)
        List<com.hms.pharmacy.dto.TopMedicineDTO> topMedicines = saleService.getTopMedicines();

        // Low stock items — capped to 20 to keep stats response small
        List<MedicineInventoryDTO> lowStockItems = medicineInventoryRepository.findByQuantityLessThan(10)
                .stream()
                .limit(20)
                .map(MedicineInventory::toMedicineInventoryDTO)
                .collect(Collectors.toList());

        // Recent sales — strip saleItems to keep payload minimal
        List<SaleDTO> recentSales = saleService.getRecentSales(5).stream()
                .map(s -> { s.setSaleItems(null); return s; })
                .collect(Collectors.toList());
        double totalRevenue = saleService.getTotalRevenue();

        long activeCount = medicineInventoryRepository.countByStatus(StockStatus.ACTIVE);
        long expiredCount = medicineInventoryRepository.countByStatus(StockStatus.EXPIRED);
        long soldOutCount = medicineInventoryRepository.countByStatus(StockStatus.SOLD_OUT);

        List<com.hms.pharmacy.dto.TopPatientDTO> topPatients = saleService.getTopPatients();
        return new PharmacyStatsDTO(totalMedicines, lowStockCount, topMedicines, lowStockItems, recentSales, totalRevenue, activeCount, expiredCount, soldOutCount, topPatients);
    }
}
