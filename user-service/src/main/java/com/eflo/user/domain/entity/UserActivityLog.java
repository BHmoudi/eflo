package com.eflo.user.domain.entity;

import com.eflo.user.domain.enums.ActivityType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_activity_logs")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = "user")
public class UserActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 50)
    private ActivityType activityType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "performed_at", nullable = false)
    private LocalDateTime performedAt;

    @Column(name = "performed_by")
    private String performedBy;

    @Column(name = "business_unit_id")
    private Long businessUnitId;

    @Column(name = "business_unit_name")
    private String businessUnitName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata")
    private String metadata;

    // Metadata
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (performedAt == null) {
            performedAt = LocalDateTime.now();
        }
    }

    // Helper factory methods
    public static UserActivityLog createLoginLog(User user, String ipAddress, String userAgent) {
        return UserActivityLog.builder()
                .user(user)
                .activityType(ActivityType.LOGIN)
                .description("User logged in")
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .performedAt(LocalDateTime.now())
                .performedBy(user.getEmail())
                .build();
    }

    public static UserActivityLog createLogoutLog(User user, String ipAddress) {
        return UserActivityLog.builder()
                .user(user)
                .activityType(ActivityType.LOGOUT)
                .description("User logged out")
                .ipAddress(ipAddress)
                .performedAt(LocalDateTime.now())
                .performedBy(user.getEmail())
                .build();
    }

    public static UserActivityLog createProfileUpdateLog(User user, String performedBy) {
        return UserActivityLog.builder()
                .user(user)
                .activityType(ActivityType.PROFILE_UPDATE)
                .description("User profile updated")
                .performedAt(LocalDateTime.now())
                .performedBy(performedBy)
                .build();
    }
}
