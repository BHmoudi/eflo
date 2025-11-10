package com.eflo.workflow.web.dto.response;

import com.eflo.workflow.domain.enums.TransitionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransitionResponse {
    private Long id;
    private String transitionName;
    private String fromStateCode;
    private String fromStateName;
    private String toStateCode;
    private String toStateName;
    private TransitionType transitionType;
    private Boolean requiresApproval;
    private Boolean autoTransition;
    private String conditionExpression;
    private String requiredRoleCode;
    private Map<String, Object> configuration;
    private LocalDateTime createdAt;
}
