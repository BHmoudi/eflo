package com.eflo.user.service;

import com.eflo.user.domain.dto.ActivityLogDTO;
import com.eflo.user.domain.entity.UserActivityLog;
import com.eflo.user.domain.enums.ActivityType;
import com.eflo.user.domain.repository.UserActivityLogRepository;
import com.eflo.user.mapper.ActivityLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for advanced audit log queries and reporting.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final UserActivityLogRepository activityLogRepository;
    private final ActivityLogMapper activityLogMapper;

    /**
     * Get activity logs for a specific user.
     *
     * @param userId the user ID
     * @param page   page number
     * @param size   page size
     * @return page of activity logs
     */
    @Transactional(readOnly = true)
    public Page<ActivityLogDTO> getUserActivityLogs(String userId, int page, int size) {
        log.debug("Fetching activity logs for user: {}", userId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "performedAt"));
        Page<UserActivityLog> logs = activityLogRepository.findByUserId(Long.parseLong(userId), pageable);
        return logs.map(activityLogMapper::toDTO);
    }

    /**
     * Get activity logs by activity type.
     *
     * @param activityType the activity type
     * @param page         page number
     * @param size         page size
     * @return page of activity logs
     */
    @Transactional(readOnly = true)
    public Page<ActivityLogDTO> getActivityLogsByActivityType(ActivityType activityType, int page, int size) {
        log.debug("Fetching activity logs for activity type: {}", activityType);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "performedAt"));
        Page<UserActivityLog> logs = activityLogRepository.findByActivityTypeOrderByPerformedAtDesc(activityType, pageable);
        return logs.map(activityLogMapper::toDTO);
    }

    /**
     * Get activity logs within a date range (based on when the activity was performed).
     *
     * @param startDate start date
     * @param endDate   end date
     * @param page      page number
     * @param size      page size
     * @return page of activity logs
     */
    @Transactional(readOnly = true)
    public Page<ActivityLogDTO> getActivityLogsByDateRange(
            LocalDateTime startDate,
            LocalDateTime endDate,
            int page,
            int size
    ) {
        log.debug("Fetching activity logs between {} and {}", startDate, endDate);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "performedAt"));
        Page<UserActivityLog> logs = activityLogRepository.findByPerformedAtBetween(startDate, endDate, pageable);
        return logs.map(activityLogMapper::toDTO);
    }

    /**
     * Get recent activity logs.
     *
     * @param limit number of logs to return
     * @return list of recent activity logs
     */
    @Transactional(readOnly = true)
    public List<ActivityLogDTO> getRecentActivityLogs(int limit) {
        log.debug("Fetching {} recent activity logs", limit);
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "performedAt"));
        return activityLogRepository.findAll(pageable).stream()
                .map(activityLogMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get activity logs for a specific business unit.
     * Note: The original method tried to query by entityType and entityId which don't exist in the entity.
     * Use this method to get activity logs for a specific business unit.
     *
     * @param businessUnitId the business unit ID
     * @param page           page number
     * @param size           page size
     * @return page of activity logs
     */
    @Transactional(readOnly = true)
    public Page<ActivityLogDTO> getActivityLogsForBusinessUnit(
            Long businessUnitId,
            int page,
            int size
    ) {
        log.debug("Fetching activity logs for business unit id: {}", businessUnitId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "performedAt"));
        Page<UserActivityLog> logs = activityLogRepository.findByBusinessUnitIdOrderByPerformedAtDesc(businessUnitId, pageable);
        return logs.map(activityLogMapper::toDTO);
    }

    /**
     * Get activity logs by performed by user.
     *
     * @param performedBy the user who performed the action
     * @param page        page number
     * @param size        page size
     * @return page of activity logs
     */
    @Transactional(readOnly = true)
    public Page<ActivityLogDTO> getActivityLogsByPerformedBy(String performedBy, int page, int size) {
        log.debug("Fetching activity logs performed by: {}", performedBy);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "performedAt"));
        Page<UserActivityLog> logs = activityLogRepository.findByPerformedByOrderByPerformedAtDesc(performedBy, pageable);
        return logs.map(activityLogMapper::toDTO);
    }

    /**
     * Count activity logs for a user.
     *
     * @param userId the user ID
     * @return count of activity logs
     */
    @Transactional(readOnly = true)
    public long countUserActivityLogs(String userId) {
        return activityLogRepository.countByUserId(Long.parseLong(userId));
    }

    /**
     * Count activity logs by activity type.
     *
     * @param activityType the activity type
     * @return count of activity logs
     */
    @Transactional(readOnly = true)
    public long countActivityLogsByActivityType(ActivityType activityType) {
        return activityLogRepository.findByActivityType(activityType).size();
    }
}
