package com.hms.pharmacy.dto;

public class MedicineStat {
    private Long medicineId;
    private Long totalQty;

    public MedicineStat(Long medicineId, Long totalQty) {
        this.medicineId = medicineId;
        this.totalQty = totalQty;
    }

    // Getters and Setters
    public Long getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(Long medicineId) {
        this.medicineId = medicineId;
    }

    public Long getTotalQty() {
        return totalQty;
    }

    public void setTotalQty(Long totalQty) {
        this.totalQty = totalQty;
    }
}
