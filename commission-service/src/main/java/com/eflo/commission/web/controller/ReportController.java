package com.eflo.commission.web.controller;

import com.eflo.commission.domain.entity.Commission;
import com.eflo.commission.domain.enums.CommissionStatus;
import com.eflo.commission.domain.model.response.ApiResponse;
import com.eflo.commission.domain.model.response.CommissionSummaryResponse;
import com.eflo.commission.domain.repository.CommissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final CommissionRepository commissionRepository;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<CommissionSummaryResponse>> getCommissionSummary(
            @RequestParam(required = false) Long businessUnitId,
            @RequestParam(required = false) Long salespersonId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {

        log.info("Generating commission summary report");

        Specification<Commission> spec = buildSpecification(businessUnitId, salespersonId, dateFrom, dateTo);
        List<Commission> commissions = commissionRepository.findAll(spec);

        CommissionSummaryResponse summary = buildSummary(commissions);

        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/by-salesperson")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCommissionsBySalesperson(
            @RequestParam(required = false) Long businessUnitId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {

        log.info("Generating commissions by salesperson report");

        Specification<Commission> spec = buildSpecification(businessUnitId, null, dateFrom, dateTo);
        List<Commission> commissions = commissionRepository.findAll(spec);

        Map<String, Object> report = commissions.stream()
                .collect(Collectors.groupingBy(
                        Commission::getSalespersonName,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    Map<String, Object> stats = new HashMap<>();
                                    stats.put("count", list.size());
                                    stats.put("totalAmount", list.stream()
                                            .map(Commission::getTotalCommissionExclTax)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add));
                                    stats.put("averageAmount", list.stream()
                                            .map(Commission::getTotalCommissionExclTax)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                                            .divide(BigDecimal.valueOf(list.size()),
                                                    2, RoundingMode.HALF_UP));
                                    return stats;
                                }
                        )
                ));

        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/by-business-unit")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCommissionsByBusinessUnit(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {

        log.info("Generating commissions by business unit report");

        Specification<Commission> spec = buildSpecification(null, null, dateFrom, dateTo);
        List<Commission> commissions = commissionRepository.findAll(spec);

        Map<String, Object> report = commissions.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getBusinessUnitName() != null ? c.getBusinessUnitName() : "Unknown",
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    Map<String, Object> stats = new HashMap<>();
                                    stats.put("count", list.size());
                                    stats.put("totalAmount", list.stream()
                                            .map(Commission::getTotalCommissionExclTax)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add));
                                    return stats;
                                }
                        )
                ));

        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/by-status")
    public ResponseEntity<ApiResponse<Map<CommissionStatus, Object>>> getCommissionsByStatus(
            @RequestParam(required = false) Long businessUnitId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {

        log.info("Generating commissions by status report");

        Specification<Commission> spec = buildSpecification(businessUnitId, null, dateFrom, dateTo);
        List<Commission> commissions = commissionRepository.findAll(spec);

        Map<CommissionStatus, Object> report = commissions.stream()
                .collect(Collectors.groupingBy(
                        Commission::getStatus,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    Map<String, Object> stats = new HashMap<>();
                                    stats.put("count", list.size());
                                    stats.put("totalAmount", list.stream()
                                            .map(Commission::getTotalCommissionExclTax)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add));
                                    return stats;
                                }
                        )
                ));

        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/by-period")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCommissionsByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "MONTH") String groupBy) {

        log.info("Generating commissions by period report: {}", groupBy);

        Specification<Commission> spec = buildSpecification(null, null, dateFrom, dateTo);
        List<Commission> commissions = commissionRepository.findAll(spec);

        Map<String, Object> report = commissions.stream()
                .collect(Collectors.groupingBy(
                        c -> {
                            if (c.getCalculationDate() == null) return "Unknown";
                            LocalDate date = c.getCalculationDate().toLocalDate();
                            return switch (groupBy.toUpperCase()) {
                                case "DAY" -> date.toString();
                                case "WEEK" -> date.getYear() + "-W" +
                                        String.format("%02d", getWeekOfYear(date));
                                case "MONTH" -> date.getYear() + "-" +
                                        String.format("%02d", date.getMonthValue());
                                case "YEAR" -> String.valueOf(date.getYear());
                                default -> date.toString();
                            };
                        },
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    Map<String, Object> stats = new HashMap<>();
                                    stats.put("count", list.size());
                                    stats.put("totalAmount", list.stream()
                                            .map(Commission::getTotalCommissionExclTax)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add));
                                    return stats;
                                }
                        )
                ));

        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/top-performers")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTopPerformers(
            @RequestParam(required = false) Long businessUnitId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "10") int limit) {

        log.info("Generating top performers report");

        Specification<Commission> spec = buildSpecification(businessUnitId, null, dateFrom, dateTo);
        List<Commission> commissions = commissionRepository.findAll(spec);

        List<Map<String, Object>> topPerformers = commissions.stream()
                .collect(Collectors.groupingBy(
                        Commission::getSalespersonId,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    Map<String, Object> stats = new HashMap<>();
                                    stats.put("salespersonId", list.get(0).getSalespersonId());
                                    stats.put("salespersonName", list.get(0).getSalespersonName());
                                    stats.put("commissionCount", list.size());
                                    BigDecimal total = list.stream()
                                            .map(Commission::getTotalCommissionExclTax)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                                    stats.put("totalCommission", total);
                                    stats.put("averageCommission", total.divide(
                                            BigDecimal.valueOf(list.size()),
                                            2, RoundingMode.HALF_UP));
                                    return stats;
                                }
                        )
                ))
                .values()
                .stream()
                .sorted((a, b) -> ((BigDecimal) b.get("totalCommission"))
                        .compareTo((BigDecimal) a.get("totalCommission")))
                .limit(limit)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(topPerformers));
    }

    private Specification<Commission> buildSpecification(Long businessUnitId, Long salespersonId,
                                                         LocalDate dateFrom, LocalDate dateTo) {
        Specification<Commission> spec = Specification.where(null);

        if (businessUnitId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("businessUnitId"), businessUnitId));
        }
        if (salespersonId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("salespersonId"), salespersonId));
        }
        if (dateFrom != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("calculationDate"),
                            dateFrom.atStartOfDay()));
        }
        if (dateTo != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("calculationDate"),
                            dateTo.atTime(23, 59, 59)));
        }

        return spec;
    }

    private CommissionSummaryResponse buildSummary(List<Commission> commissions) {
        BigDecimal totalExclTax = commissions.stream()
                .map(Commission::getTotalCommissionExclTax)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalInclTax = commissions.stream()
                .map(Commission::getTotalCommissionInclTax)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal average = commissions.isEmpty() ? BigDecimal.ZERO :
                totalExclTax.divide(BigDecimal.valueOf(commissions.size()),
                        2, RoundingMode.HALF_UP);

        Map<String, Integer> byStatus = commissions.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getStatus().name(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        Map<String, BigDecimal> byType = commissions.stream()
                .collect(Collectors.groupingBy(
                        Commission::getOrderType,
                        Collectors.mapping(
                                Commission::getTotalCommissionExclTax,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )
                ));

        Map<String, BigDecimal> bySalesperson = commissions.stream()
                .collect(Collectors.groupingBy(
                        Commission::getSalespersonName,
                        Collectors.mapping(
                                Commission::getTotalCommissionExclTax,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )
                ));

        Map<String, BigDecimal> byBusinessUnit = commissions.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getBusinessUnitName() != null ? c.getBusinessUnitName() : "Unknown",
                        Collectors.mapping(
                                Commission::getTotalCommissionExclTax,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )
                ));

        return CommissionSummaryResponse.builder()
                .totalCommissions(commissions.size())
                .totalAmountExclTax(totalExclTax)
                .totalAmountInclTax(totalInclTax)
                .averageCommission(average)
                .commissionsByStatus(byStatus)
                .commissionsByType(byType)
                .commissionsBySalesperson(bySalesperson)
                .commissionsByBusinessUnit(byBusinessUnit)
                .build();
    }

    private int getWeekOfYear(LocalDate date) {
        return date.getDayOfYear() / 7 + 1;
    }
}
