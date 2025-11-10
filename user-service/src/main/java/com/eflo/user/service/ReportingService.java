package com.eflo.user.service;

import com.eflo.user.domain.entity.User;
import com.eflo.user.domain.repository.BusinessUnitRepository;
import com.eflo.user.domain.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for generating reports on users and business units.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportingService {

    private final UserRepository userRepository;
    private final BusinessUnitRepository businessUnitRepository;

    /**
     * Generate overall user statistics report.
     *
     * @return user statistics
     */
    @Transactional(readOnly = true)
    public UserStatisticsReport getUserStatistics() {
        log.info("Generating user statistics report");

        List<User> allUsers = userRepository.findAll();
        long totalUsers = allUsers.size();
        long activeUsers = allUsers.stream().filter(User::isActive).count();
        long inactiveUsers = totalUsers - activeUsers;

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long recentlyCreated = allUsers.stream()
                .filter(user -> user.getCreatedAt().isAfter(thirtyDaysAgo))
                .count();

        return UserStatisticsReport.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .inactiveUsers(inactiveUsers)
                .recentlyCreatedUsers(recentlyCreated)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Generate business unit statistics report.
     *
     * @return business unit statistics
     */
    @Transactional(readOnly = true)
    public BusinessUnitStatisticsReport getBusinessUnitStatistics() {
        log.info("Generating business unit statistics report");

        long totalBusinessUnits = businessUnitRepository.count();

        return BusinessUnitStatisticsReport.builder()
                .totalBusinessUnits(totalBusinessUnits)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Generate user growth report by month.
     *
     * @param months number of months to include
     * @return map of month to user count
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getUserGrowthReport(int months) {
        log.info("Generating user growth report for {} months", months);

        List<User> allUsers = userRepository.findAll();
        Map<String, Long> growthMap = new HashMap<>();

        LocalDate now = LocalDate.now();
        for (int i = 0; i < months; i++) {
            LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);

            long count = allUsers.stream()
                    .filter(user -> {
                        LocalDate createdDate = user.getCreatedAt().toLocalDate();
                        return !createdDate.isBefore(monthStart) && !createdDate.isAfter(monthEnd);
                    })
                    .count();

            growthMap.put(monthStart.toString(), count);
        }

        return growthMap;
    }

    /**
     * Generate user distribution by business unit report.
     *
     * @return map of business unit name to user count
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getUserDistributionByBusinessUnit() {
        log.info("Generating user distribution by business unit report");

        List<User> allUsers = userRepository.findAll();

        return allUsers.stream()
                .flatMap(user -> user.getUserBusinessUnits() != null
                        ? user.getUserBusinessUnits().stream()
                        : java.util.stream.Stream.empty())
                .collect(Collectors.groupingBy(
                        ubu -> ubu.getBusinessUnit().getName(),
                        Collectors.counting()
                ));
    }

    /**
     * User statistics report DTO.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserStatisticsReport {
        private long totalUsers;
        private long activeUsers;
        private long inactiveUsers;
        private long recentlyCreatedUsers;
        private LocalDateTime generatedAt;
    }

    /**
     * Business unit statistics report DTO.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusinessUnitStatisticsReport {
        private long totalBusinessUnits;
        private LocalDateTime generatedAt;
    }
}
