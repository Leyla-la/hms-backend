package com.hms.gateway.controller;

import com.hms.gateway.dto.AdminOverviewDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/dashboard")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AdminDashboardAPI {

    private static final Logger log = LoggerFactory.getLogger(AdminDashboardAPI.class);
    private static final String INTERNAL_SECRET = "SECRET";

    private final WebClient userWebClient;
    private final WebClient profileWebClient;
    private final WebClient appointmentWebClient;
    private final WebClient pharmacyWebClient;

    public AdminDashboardAPI(@Qualifier("userWebClient") WebClient userWebClient,
                             @Qualifier("profileWebClient") WebClient profileWebClient,
                             @Qualifier("appointmentWebClient") WebClient appointmentWebClient,
                             @Qualifier("pharmacyWebClient") WebClient pharmacyWebClient) {
        this.userWebClient = userWebClient;
        this.profileWebClient = profileWebClient;
        this.appointmentWebClient = appointmentWebClient;
        this.pharmacyWebClient = pharmacyWebClient;
    }

    @GetMapping("")
    public Mono<ResponseEntity<AdminOverviewDTO>> getAdminDashboard(
            @RequestHeader(value = "X-Secret-Key", required = false) String secretKey) {
        log.info("[Dashboard] Fetching admin overview from all microservices...");

        // totalUsers (Patients): GET /profile/patient/count → returns long
        Mono<Long> totalUsersMono = profileWebClient.get()
                .uri("/profile/patient/count")
                .header("X-Secret-Key", INTERNAL_SECRET)
                .retrieve()
                .bodyToMono(Long.class)
                .doOnNext(v -> log.info("[Dashboard] totalPatients={}", v))
                .onErrorResume(e -> {
                    log.error("[Dashboard] FAILED totalPatients (ProfileMS /profile/patient/count): {}", e.getMessage());
                    return Mono.just(0L);
                })
                .subscribeOn(Schedulers.boundedElastic());

        // totalDoctors: GET /profile/doctor/count → returns long
        Mono<Long> totalDoctorsMono = profileWebClient.get()
                .uri("/profile/doctor/count")
                .header("X-Secret-Key", INTERNAL_SECRET)
                .retrieve()
                .bodyToMono(Long.class)
                .doOnNext(v -> log.info("[Dashboard] totalDoctors={}", v))
                .onErrorResume(e -> {
                    log.error("[Dashboard] FAILED totalDoctors (ProfileMS /profile/doctor/count): {}", e.getMessage());
                    return Mono.just(0L);
                })
                .subscribeOn(Schedulers.boundedElastic());

        Mono<Map<String, Long>> doctorsByDeptMono = profileWebClient.get()
                .uri("/profile/doctor/countByDept")
                .header("X-Secret-Key", INTERNAL_SECRET)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Long>>() {})
                .doOnNext(v -> log.info("[Dashboard] doctorsByDept={}", v))
                .onErrorResume(e -> {
                    log.error("[Dashboard] FAILED doctorsByDept: {}", e.getMessage());
                    return Mono.just(new HashMap<>());
                })
                .subscribeOn(Schedulers.boundedElastic());

        // appointment summary: GET /appointments/dashboard/summary
        // AppointmentSummaryDTO fields: totalAll, totalToday, scheduled, completed, cancelled, overdue
        Mono<Map<String, Object>> appSummaryMono = appointmentWebClient.get()
                .uri("/appointments/dashboard/summary")
                .header("X-Secret-Key", INTERNAL_SECRET)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .doOnNext(v -> log.info("[Dashboard] appointmentSummary={}", v))
                .onErrorResume(e -> {
                    log.error("[Dashboard] FAILED appointmentSummary (AppointmentMS /appointments/dashboard/summary): {}", e.getMessage());
                    return Mono.just(new HashMap<>());
                })
                .subscribeOn(Schedulers.boundedElastic());

        // pharmacy stats: GET /pharmacy/inventory/statistics/overview
        // PharmacyStatsDTO fields: totalMedicines, lowStockCount, topMedicines, recentSales
        Mono<Map<String, Object>> pharmacyStatsMono = pharmacyWebClient.get()
                .uri("/pharmacy/inventory/statistics/overview")
                .header("X-Secret-Key", INTERNAL_SECRET)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .doOnNext(v -> log.info("[Dashboard] pharmacyStats keys={}", v.keySet()))
                .onErrorResume(e -> {
                    log.error("[Dashboard] FAILED pharmacyStats (PharmacyMS /pharmacy/inventory/statistics/overview): {}", e.getMessage());
                    return Mono.just(new HashMap<>());
                })
                .subscribeOn(Schedulers.boundedElastic());

        return Mono.zip(totalUsersMono, totalDoctorsMono, appSummaryMono, pharmacyStatsMono)
                .zipWith(doctorsByDeptMono, (tuple, doctorsByDept) -> {
                    long totalUsers   = tuple.getT1();
                    long totalDoctors = tuple.getT2();
                    Map<String, Object> summary = tuple.getT3();
                    Map<String, Object> phStats = tuple.getT4();
                    log.info("[Dashboard] summary data: {}", summary);
                    log.info("[Dashboard] pharmacy data: {}", phStats);

                    // AppointmentSummaryDTO actual field names: totalAll, totalToday, scheduled, completed, cancelled
                    long totalAppointments = toLong(summary.getOrDefault("totalAll", 0));
                    long todayAppointments = toLong(summary.getOrDefault("totalToday", 0));
                    long scheduled         = toLong(summary.getOrDefault("scheduled", 0));
                    long completed         = toLong(summary.getOrDefault("completed", 0));
                    long cancelled         = toLong(summary.getOrDefault("cancelled", 0));
                    long overdue           = toLong(summary.getOrDefault("overdue", 0));

                    // PharmacyStatsDTO actual field names: totalMedicines, lowStockCount, topMedicines, recentSales, totalRevenue
                    long totalMedicines = toLong(phStats.getOrDefault("totalMedicines", 0));
                    long lowStockCount  = toLong(phStats.getOrDefault("lowStockCount", 0));
                    double totalRevenue = toDouble(phStats.getOrDefault("totalRevenue", 0.0));
                    long activeCount    = toLong(phStats.getOrDefault("activeCount", 0));
                    long expiredCount   = toLong(phStats.getOrDefault("expiredCount", 0));
                    long soldOutCount   = toLong(phStats.getOrDefault("soldOutCount", 0));

                    Map<String, Object> kpis = new HashMap<>();
                    kpis.put("totalUsers",        totalUsers);
                    kpis.put("totalDoctors",      totalDoctors);
                    kpis.put("totalMedicines",    totalMedicines);
                    kpis.put("totalAppointments", totalAppointments);
                    kpis.put("todayAppointments", todayAppointments);
                    kpis.put("totalRevenue",      totalRevenue);
                    kpis.put("activeMedicines",   activeCount);
                    kpis.put("expiredMedicines",  expiredCount);
                    kpis.put("soldOutMedicines",  soldOutCount);
                    kpis.put("overdueAppointments", overdue);

                    List<AdminOverviewDTO.StatusCount> appStatuses = new ArrayList<>();
                    appStatuses.add(new AdminOverviewDTO.StatusCount("Scheduled", scheduled));
                    appStatuses.add(new AdminOverviewDTO.StatusCount("Completed", completed));
                    appStatuses.add(new AdminOverviewDTO.StatusCount("Cancelled", cancelled));

                    @SuppressWarnings("unchecked")
                    List<Object> topMeds = (List<Object>) phStats.getOrDefault("topMedicines", new ArrayList<>());
                    @SuppressWarnings("unchecked")
                    List<Object> topPatients = (List<Object>) phStats.getOrDefault("topPatients", new ArrayList<>());
                    @SuppressWarnings("unchecked")
                    List<Object> recentSalesList = (List<Object>) phStats.getOrDefault("recentSales", new ArrayList<>());
                    @SuppressWarnings("unchecked")
                    List<Object> lowStockItems = (List<Object>) phStats.getOrDefault("lowStockItems", new ArrayList<>());

                    AdminOverviewDTO dto = AdminOverviewDTO.builder()
                            .kpis(kpis)
                            .appointmentStatus(appStatuses)
                            .lowStockCount(lowStockCount)
                            .lowStockItems(lowStockItems)
                            .topMedicines(topMeds)
                            .topPatients(topPatients)
                            .recentSales(recentSalesList)
                            .totalRevenue(totalRevenue)
                            .doctorsByDept(doctorsByDept)
                            .lastUpdated(LocalDateTime.now().toString())
                            .build();

                    log.info("[Dashboard] SUCCESS: users={}, doctors={}, meds={}, apps={}, revenue={}, active={}, exp={}, soldout={}",
                            totalUsers, totalDoctors, totalMedicines, totalAppointments, totalRevenue, activeCount, expiredCount, soldOutCount);
                    return ResponseEntity.ok(dto);
                })
                .doOnError(e -> log.error("[Dashboard] Fatal aggregation error", e))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    @GetMapping("/stats")
    public Mono<ResponseEntity<Map<String, Object>>> getDashboardStats(
            @RequestHeader(value = "X-Secret-Key", required = false) String secretKey) {
        return getAdminDashboard(secretKey).map(resp -> {
            if (resp.getBody() != null) {
                return ResponseEntity.ok(resp.getBody().getKpis());
            }
            return ResponseEntity.status(resp.getStatusCode()).build();
        });
    }

    /** Safe numeric conversion — JSON deserializes numbers as Integer/Long/Double depending on magnitude */
    private long toLong(Object val) {
        if (val == null) return 0L;
        if (val instanceof Number) return ((Number) val).longValue();
        try { return Long.parseLong(val.toString()); } catch (NumberFormatException ignore) { return 0L; }
    }

    private double toDouble(Object val) {
        if (val == null) return 0.0;
        if (val instanceof Number) return ((Number) val).doubleValue();
        try { return Double.parseDouble(val.toString()); } catch (NumberFormatException ignore) { return 0.0; }
    }
}