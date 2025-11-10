package com.eflo.workflow.domain.enums;

/**
 * Workflow Event Type
 *
 * Defines all event types that can be published by the workflow engine.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
public enum EventType {

    // Instance Events
    INSTANCE_CREATED("Workflow instance created"),
    INSTANCE_STARTED("Workflow instance started"),
    INSTANCE_PAUSED("Workflow instance paused"),
    INSTANCE_RESUMED("Workflow instance resumed"),
    INSTANCE_COMPLETED("Workflow instance completed"),
    INSTANCE_CANCELLED("Workflow instance cancelled"),
    INSTANCE_FAILED("Workflow instance failed"),

    // State Events
    STATE_ENTERED("Entered workflow state"),
    STATE_EXITED("Exited workflow state"),
    STATE_CHANGED("Workflow state changed"),

    // Task Events
    TASK_CREATED("Task created"),
    TASK_ASSIGNED("Task assigned to user"),
    TASK_STARTED("Task started"),
    TASK_COMPLETED("Task completed"),
    TASK_FAILED("Task failed"),
    TASK_SKIPPED("Task skipped"),
    TASK_CANCELLED("Task cancelled"),
    TASK_REASSIGNED("Task reassigned"),

    // Deadline Events
    TASK_OVERDUE("Task is overdue"),
    INSTANCE_OVERDUE("Instance is overdue"),
    DEADLINE_WARNING("Deadline warning issued"),

    // Escalation Events
    ESCALATION_TRIGGERED("Escalation triggered"),
    ESCALATION_LEVEL_1("First level escalation"),
    ESCALATION_LEVEL_2("Second level escalation"),
    ESCALATION_LEVEL_3("Third level escalation"),

    // Transition Events
    TRANSITION_EXECUTED("State transition executed"),
    TRANSITION_FAILED("State transition failed"),

    // Error Events
    VALIDATION_ERROR("Validation error occurred"),
    BUSINESS_RULE_VIOLATION("Business rule violated"),
    SYSTEM_ERROR("System error occurred");

    private final String description;

    EventType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Get Kafka topic for this event type
     */
    public String getKafkaTopic() {
        if (name().startsWith("INSTANCE_")) {
            return "workflow.instance.events";
        } else if (name().startsWith("TASK_")) {
            return "workflow.task.events";
        } else if (name().startsWith("STATE_")) {
            return "workflow.state.events";
        } else if (name().contains("OVERDUE") || name().contains("DEADLINE")) {
            return "workflow.deadline.events";
        } else if (name().startsWith("ESCALATION_")) {
            return "workflow.escalation.events";
        } else {
            return "workflow.general.events";
        }
    }
}
