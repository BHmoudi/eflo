package com.eflo.order.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_condition_criteria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderConditionCriteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private OrderConditionRule rule;

    @Column(name = "field_path", nullable = false)
    private String fieldPath;

    @Enumerated(EnumType.STRING)
    @Column(name = "operator", nullable = false, length = 50)
    private CriteriaOperator operator;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "value", columnDefinition = "jsonb")
    private Object value;

    @Column(name = "criteria_group")
    private Integer criteriaGroup = 1;

    @Column(name = "sequence")
    private Integer sequence = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum CriteriaOperator {
        EQUALS,
        NOT_EQUALS,
        IN,
        NOT_IN,
        GREATER_THAN,
        LESS_THAN,
        GREATER_THAN_OR_EQUAL,
        LESS_THAN_OR_EQUAL,
        CONTAINS,
        STARTS_WITH,
        ENDS_WITH,
        EXISTS,
        NOT_EXISTS,
        COUNT_EQUALS,
        COUNT_GREATER_THAN,
        COUNT_LESS_THAN,
        IS_NULL,
        IS_NOT_NULL
    }
}
