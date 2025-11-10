package com.eflo.workflow.domain.model.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * State Changed Event DTO
 *
 * Published when workflow instance transitions between states
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StateChangedEvent {

    private String eventId;
    private Long instanceId;
    private Long orderId;
    private String processCode;
    private Long fromStateId;
    private String fromStateName;
    private Long toStateId;
    private String toStateName;
    private Long transitionId;
    private String transitionName;
    private LocalDateTime timestamp;
    private Long userId;
    private String userName;
}
