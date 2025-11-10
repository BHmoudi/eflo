package com.eflo.user.event.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Event published when a new business unit is created.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessUnitCreatedEvent {

    private String eventId;

    private String businessUnitId;

    private String name;

    private String code;

    private String type;

    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private String createdBy;

    public static BusinessUnitCreatedEvent of(String businessUnitId, String name, String code,
                                               String type, String description, String createdBy) {
        return BusinessUnitCreatedEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .businessUnitId(businessUnitId)
                .name(name)
                .code(code)
                .type(type)
                .description(description)
                .timestamp(LocalDateTime.now())
                .createdBy(createdBy)
                .build();
    }
}
