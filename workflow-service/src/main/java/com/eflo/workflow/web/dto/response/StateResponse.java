package com.eflo.workflow.web.dto.response;

import com.eflo.workflow.domain.enums.StateType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StateResponse {
    private Long id;
    private String stateCode;
    private String stateName;
    private String description;
    private Integer stateOrder;
    private StateType stateType;
    private Integer expectedDurationHours;
    private Boolean isFinalState;
    private Boolean requiresApproval;
    private Boolean allowSkip;
    private Map<String, Object> configuration;
    private List<TaskDefinitionResponse> tasks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
