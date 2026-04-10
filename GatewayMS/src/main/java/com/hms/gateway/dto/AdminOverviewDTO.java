package com.hms.gateway.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminOverviewDTO {
    Map<String, Object> kpis;
    List<StatusCount> appointmentStatus;
    long lowStockCount;
    List<Object> lowStockItems;
    List<Object> topMedicines;
    List<Object> topPatients;
    List<Object> recentSales;
    double totalRevenue;
    Map<String, Long> doctorsByDept;
    String lastUpdated;

    @Data
    @AllArgsConstructor
    public static class StatusCount {
        String status;
        long count;
    }
}
