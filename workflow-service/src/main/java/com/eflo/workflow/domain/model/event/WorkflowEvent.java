package com.eflo.workflow.domain.model.event;

import com.eflo.workflow.domain.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Base Workflow Event DTO
 *
 * Published to Kafka for all workflow events
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowEvent {

    private String eventId;
    private EventType eventType;
    private Long instanceId;
    private Long orderId;
    private String processCode;
    private LocalDateTime timestamp;
    private Long userId;
    private String userName;
    private Map<String, Object> eventData;
    private String description;
}
