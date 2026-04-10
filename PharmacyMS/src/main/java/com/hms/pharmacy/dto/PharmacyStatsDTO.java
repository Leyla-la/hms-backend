package com.hms.pharmacy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PharmacyStatsDTO {
    private long totalMedicines;
    private long lowStockCount;
    private List<TopMedicineDTO> topMedicines;
    private List<MedicineInventoryDTO> lowStockItems;
    private List<SaleDTO> recentSales;
    private double totalRevenue;
    private long activeCount;
    private long expiredCount;
    private long soldOutCount;
    private List<TopPatientDTO> topPatients;
}
