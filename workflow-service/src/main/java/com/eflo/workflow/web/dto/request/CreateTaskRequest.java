package com.eflo.workflow.web.dto.request;

import com.eflo.workflow.domain.enums.TaskType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Request to create a workflow task
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskRequest {

    @NotBlank(message = "Task code is required")
    @Size(max = 50, message = "Task code cannot exceed 50 characters")
    private String taskCode;

    @NotBlank(message = "Task name is required")
    @Size(max = 255, message = "Task name cannot exceed 255 characters")
    private String taskName;

    private String description;

    @NotNull(message = "Task type is required")
    private TaskType taskType;

    @NotNull(message = "Task order is required")
    @Min(value = 1, message = "Task order must be at least 1")
    private Integer taskOrder;

    private String assignedToRole;

    private Long assignedRoleId;

    private Long assignedToUserId;

    @Builder.Default
    private Boolean isMandatory = true;

    private Integer expectedDurationHours;

    @Builder.Default
    private Boolean autoAssign = true;

    @Builder.Default
    private Boolean requiresApproval = false;

    private Long taskTypeId;

    private Map<String, Object> formDefinition;

    private Map<String, Object> validationRules;

    private Map<String, Object> configuration;

    // Advanced features
    private List<String> requiredDocuments;

    private List<InputFieldDefinition> inputFields;

    private Integer slaHours;

    private Integer escalationHours;

    private Boolean allowDelegation;

    private List<Long> visibleToRoles;

    private Boolean editableAfterCompletion;

    private String notificationTemplate;

    // Approval chain
    private Long approvalChainId;

    // Dependencies
    private List<TaskDependencyRequest> dependencies;

    // Conditions
    private List<TaskConditionRequest> conditions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InputFieldDefinition {
        private String name;
        private String type; // text, number, date, datetime, select, textarea, checkbox, file
        private String label;
        private Boolean required;
        private String placeholder;
        private Object defaultValue;
        private Map<String, Object> validation;
        private List<String> options; // For select fields
        private Integer maxLength;
        private Integer min;
        private Integer max;
        private String pattern;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskDependencyRequest {
        private String requiredTaskCode;
        private String dependencyType; // COMPLETION, APPROVAL, STATUS, FIELD_VALUE
        private String requiredStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskConditionRequest {
        private String conditionType; // FIELD_VALUE, ROLE_BASED, DATE_BASED, CUSTOM, ORDER_CRITERIA
        private String conditionField;
        private String conditionOperator; // EQUALS, NOT_EQUALS, GREATER_THAN, LESS_THAN, CONTAINS
        private String conditionValue;
        private String conditionExpression;
    }
}
