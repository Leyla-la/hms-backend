package com.hms.pharmacy.service;

import com.hms.pharmacy.dto.SaleItemDTO;
import com.hms.pharmacy.entity.SaleItem;
import com.hms.pharmacy.exception.HmsException;
import com.hms.pharmacy.repository.SaleItemRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SaleItemServiceImpl implements SaleItemService {

    SaleItemRepository saleItemRepository;

    @Override
    public Long createSaleItem(SaleItemDTO saleItemDTO) throws HmsException {
        return saleItemRepository.save(saleItemDTO.toSaleItem()).getId();
    }

    @Override
    public void createMultipleSaleItem(Long saleId, Long medicineId, List<SaleItemDTO> saleItemDTOs) throws HmsException {
        // set saleId and medicineId on each DTO then save
        saleItemDTOs.forEach(x -> {
            x.setSaleId(saleId);
            x.setMedicineId(medicineId);
            saleItemRepository.save(x.toSaleItem());
        });
    }


    @Override
    public void updateSaleItem(SaleItemDTO saleItemDTO) throws HmsException {
        SaleItem existing = saleItemRepository.findById(saleItemDTO.getId()).orElseThrow(() -> new HmsException("SALE_ITEM_NOT_FOUND"));
        existing.setBatchNo(saleItemDTO.getBatchNo());
        existing.setQuantity(saleItemDTO.getQuantity());
        existing.setUnitPrice(saleItemDTO.getUnitPrice());
        saleItemRepository.save(existing);
    }

    @Override
    public List<SaleItemDTO> getSaleItemsBySaleId(Long saleId) throws HmsException {
        List<SaleItem> items = saleItemRepository.findBySaleId(saleId);
        if (items == null || items.isEmpty()) {
            throw new HmsException("SALE_ITEMS_NOT_FOUND");
        }
        return items.stream().map(SaleItem::toSaleItemDTO).collect(Collectors.toList());
    }

    @Override
    public SaleItemDTO getSaleItem(Long id) throws HmsException {
        return saleItemRepository.findById(id)
                .map(SaleItem::toSaleItemDTO)
                .orElseThrow(() -> new HmsException("SALE_ITEM_NOT_FOUND"));
    }
}
