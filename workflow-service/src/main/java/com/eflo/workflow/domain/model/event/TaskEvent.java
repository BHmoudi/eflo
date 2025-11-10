package com.eflo.workflow.domain.model.event;

import com.eflo.workflow.domain.enums.EventType;
import com.eflo.workflow.domain.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Task Event DTO
 *
 * Published for all task-related events
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskEvent {

    private String eventId;
    private EventType eventType;
    private Long taskId;
    private Long instanceId;
    private Long orderId;
    private String taskCode;
    private String taskName;
    private TaskStatus taskStatus;
    private Long assignedToUserId;
    private String assignedToUserName;
    private LocalDateTime timestamp;
    private Long actionByUserId;
    private String actionByUserName;
    private String description;
}
