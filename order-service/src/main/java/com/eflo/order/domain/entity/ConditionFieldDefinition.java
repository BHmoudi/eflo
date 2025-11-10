package com.eflo.order.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "condition_field_definition")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConditionFieldDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "field_path", unique = true, nullable = false)
    private String fieldPath;

    @Column(name = "field_label", nullable = false, length = 100)
    private String fieldLabel;

    @Enumerated(EnumType.STRING)
    @Column(name = "field_type", nullable = false, length = 50)
    private FieldType fieldType;

    @Column(name = "entity", nullable = false, length = 100)
    private String entity;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "available_operators", nullable = false, columnDefinition = "jsonb")
    private List<String> availableOperators;

    @Enumerated(EnumType.STRING)
    @Column(name = "value_source", length = 50)
    private ValueSource valueSource = ValueSource.MANUAL;

    @Column(name = "lookup_table", length = 100)
    private String lookupTable;

    @Column(name = "lookup_field", length = 100)
    private String lookupField;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum FieldType {
        STRING, NUMBER, BOOLEAN, ARRAY, OBJECT, DATE
    }

    public enum ValueSource {
        MANUAL, LOOKUP, CALCULATED
    }
}
