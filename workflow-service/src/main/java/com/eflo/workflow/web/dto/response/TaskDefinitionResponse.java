package com.eflo.workflow.web.dto.response;

import com.eflo.workflow.domain.enums.TaskType;
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
public class TaskDefinitionResponse {
    private Long id;
    private String taskCode;
    private String taskName;
    private String description;
    private TaskType taskType;
    private Integer taskOrder;
    private String assignedToRole;
    private Long assignedRoleId;
    private Long assignedToUserId;
    private Boolean isMandatory;
    private Integer expectedDurationHours;
    private Boolean autoAssign;
    private Boolean requiresApproval;
    private Map<String, Object> formDefinition;
    private Map<String, Object> validationRules;
    private Map<String, Object> configuration;
    private List<String> requiredDocuments;
    private List<InputFieldDefinition> inputFields;
    private Integer slaHours;
    private Integer escalationHours;
    private Boolean allowDelegation;
    private List<Long> visibleToRoles;
    private Boolean editableAfterCompletion;
    private String notificationTemplate;
    private Long approvalChainId;
    private String approvalChainName;
    private List<TaskDependencyInfo> dependencies;
    private List<TaskConditionInfo> conditions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InputFieldDefinition {
        private String name;
        private String type;
        private String label;
        private Boolean required;
        private String placeholder;
        private Object defaultValue;
        private Map<String, Object> validation;
        private List<String> options;
        private Integer maxLength;
        private Integer min;
        private Integer max;
        private String pattern;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskDependencyInfo {
        private Long id;
        private String requiredTaskCode;
        private String dependencyType;
        private String requiredStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskConditionInfo {
        private Long id;
        private String conditionType;
        private String conditionField;
        private String conditionOperator;
        private String conditionValue;
        private String conditionExpression;
    }
}
