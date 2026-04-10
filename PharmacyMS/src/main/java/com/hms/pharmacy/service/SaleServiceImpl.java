package com.hms.pharmacy.service;

import com.hms.pharmacy.clients.ProfileClient;
import com.hms.pharmacy.dto.SaleDTO;
import com.hms.pharmacy.entity.Sale;
import com.hms.pharmacy.exception.HmsException;
import com.hms.pharmacy.notification.NotificationPublisher;
import com.hms.pharmacy.repository.SaleRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SaleServiceImpl implements SaleService {
    SaleRepository saleRepository;
    NotificationPublisher notificationPublisher;
    ProfileClient profileClient;
    com.hms.pharmacy.repository.MedicineRepository medicineRepository;
    com.hms.pharmacy.repository.SaleItemRepository saleItemRepository;

    @Override
    public Long createSale(SaleDTO saleDTO) throws HmsException {
        // Allow creating sale without prescriptionId (e.g., retail sale)
        if (saleDTO.getPrescriptionId() != null && saleRepository.existsByPrescriptionId(saleDTO.getPrescriptionId())) {
            throw new HmsException("SALE_ALREADY_EXISTS");
        }
        saleDTO.setSaleDate(LocalDateTime.now());
        log.info("[SaleService] Creating sale: totalAmount={}, buyer={}", saleDTO.getTotalAmount(), saleDTO.getBuyerName());
        
        Sale savedSale = saleRepository.save(saleDTO.toSale());

        // Process stock update
        if (saleDTO.getSaleItems() != null) {
            for (var itemDTO : saleDTO.getSaleItems()) {
                var medicine = medicineRepository.findById(itemDTO.getMedicineId())
                        .orElseThrow(() -> new HmsException("MEDICINE_NOT_FOUND: " + itemDTO.getMedicineId()));
                
                int currentStock = medicine.getStock() != null ? medicine.getStock() : 0;
                int requestedQty = itemDTO.getQuantity() != null ? itemDTO.getQuantity() : 0;
                
                if (currentStock < requestedQty) {
                    throw new HmsException("INSUFFICIENT_STOCK: " + medicine.getName());
                }
                
                medicine.setStock(currentStock - requestedQty);
                medicineRepository.save(medicine);
            }
        }

        // Publish notification
        notificationPublisher.publish(
                "hms.pharmacy.sale.created",
                savedSale.getId().toString(),
                savedSale.toSaleDTO(),
                "SALE_CREATED",
                "PharmacyMS",
                profileClient.getAdminIds());

        return savedSale.getId();
    }

    @Override
    public void updateSale(SaleDTO saleDTO) throws HmsException {
        Sale sale = saleRepository.findByPrescriptionId(saleDTO.getPrescriptionId())
                .orElseThrow(() -> new HmsException("SALE_NOT_FOUND"));
        sale.setSaleDate(saleDTO.getSaleDate());
        sale.setTotalAmount(saleDTO.getTotalAmount());
        saleRepository.save(sale);
    }

    @Override
    public SaleDTO getSaleById(Long id) throws HmsException {
        return saleRepository.findById(id).orElseThrow(() -> new HmsException("SALE_NOT_FOUND")).toSaleDTO();
    }

    @Override
    public SaleDTO getSaleByPrescriptionId(Long prescriptionId) throws HmsException {
        return saleRepository.findByPrescriptionId(prescriptionId).orElseThrow(() -> new HmsException("SALE_NOT_FOUND"))
                .toSaleDTO();
    }

    @Override
    public Page<SaleDTO> getAllSales(Pageable pageable) throws HmsException {
        return saleRepository.findAll(pageable).map(Sale::toSaleDTO);
    }

    @Override
    public List<SaleDTO> getRecentSales(int limit) throws HmsException {
        // limit is usually 5 as per repository method
        return saleRepository.findTop5ByOrderBySaleDateDesc().stream().map(Sale::toSaleDTO).toList();
    }

    @Override
    public double getTotalRevenue() throws HmsException {
        Double total = saleRepository.getTotalRevenue();
        return total != null ? total : 0.0;
    }

    @Override
    public List<com.hms.pharmacy.dto.TopPatientDTO> getTopPatients() throws HmsException {
        List<Object[]> rows = saleRepository.findTopPatientsByRevenue(org.springframework.data.domain.PageRequest.of(0, 5));
        return rows.stream().map(r -> new com.hms.pharmacy.dto.TopPatientDTO(
            (String) r[0], // buyerName
            (String) r[1], // buyerContact
            ((Number) r[2]).doubleValue() // totalAmount sum
        )).collect(Collectors.toList());
    }

    @Override
    public List<com.hms.pharmacy.dto.TopMedicineDTO> getTopMedicines() throws HmsException {
        List<Object[]> rows = saleItemRepository.findTopMedicinesByQuantity(org.springframework.data.domain.PageRequest.of(0, 5));
        return rows.stream().map(r -> new com.hms.pharmacy.dto.TopMedicineDTO(
            ((Number) r[0]).longValue(), // medicineId
            (String) r[1], // medicineName
            ((Number) r[2]).longValue() // quantity sum
        )).collect(Collectors.toList());
    }
}
