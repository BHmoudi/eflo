package com.eflo.user.event.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Event published when a hierarchy relationship is changed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HierarchyChangedEvent {

    private String eventId;

    private String hierarchyId;

    private String childBusinessUnitId;

    private String parentBusinessUnitId;

    private String changeType; // CREATED, UPDATED, DELETED

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private String changedBy;

    public static HierarchyChangedEvent of(String hierarchyId, String childBusinessUnitId,
                                            String parentBusinessUnitId, String changeType, String changedBy) {
        return HierarchyChangedEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .hierarchyId(hierarchyId)
                .childBusinessUnitId(childBusinessUnitId)
                .parentBusinessUnitId(parentBusinessUnitId)
                .changeType(changeType)
                .timestamp(LocalDateTime.now())
                .changedBy(changedBy)
                .build();
    }
}
