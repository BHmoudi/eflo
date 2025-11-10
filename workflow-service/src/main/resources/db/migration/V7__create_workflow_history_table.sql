-- Create workflow_history table
-- Complete audit trail of all workflow changes

CREATE TABLE workflow_history (
    id BIGSERIAL PRIMARY KEY,
    instance_id BIGINT NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    event_description TEXT,
    from_state_id BIGINT,
    to_state_id BIGINT,
    transition_id BIGINT,
    task_id BIGINT,
    user_id BIGINT,
    user_name VARCHAR(100),
    user_role VARCHAR(50),
    event_data JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_workflow_history_instance FOREIGN KEY (instance_id)
        REFERENCES workflow_instances(id) ON DELETE CASCADE,
    CONSTRAINT fk_workflow_history_from_state FOREIGN KEY (from_state_id)
        REFERENCES workflow_states(id),
    CONSTRAINT fk_workflow_history_to_state FOREIGN KEY (to_state_id)
        REFERENCES workflow_states(id),
    CONSTRAINT fk_workflow_history_transition FOREIGN KEY (transition_id)
        REFERENCES workflow_transitions(id),
    CONSTRAINT fk_workflow_history_task FOREIGN KEY (task_id)
        REFERENCES workflow_instance_tasks(id) ON DELETE CASCADE,
    CONSTRAINT check_event_type CHECK (event_type IN (
        'INSTANCE_CREATED', 'INSTANCE_STARTED', 'INSTANCE_PAUSED', 'INSTANCE_RESUMED',
        'INSTANCE_COMPLETED', 'INSTANCE_CANCELLED', 'STATE_CHANGED', 'TRANSITION_EXECUTED',
        'TASK_CREATED', 'TASK_ASSIGNED', 'TASK_STARTED', 'TASK_COMPLETED',
        'TASK_REASSIGNED', 'TASK_SKIPPED', 'TASK_FAILED', 'ESCALATION_TRIGGERED',
        'DEADLINE_UPDATED', 'PRIORITY_CHANGED', 'DATA_UPDATED', 'ERROR_OCCURRED'
    ))
);

-- Create indexes
CREATE INDEX idx_workflow_history_instance ON workflow_history(instance_id);
CREATE INDEX idx_workflow_history_event_type ON workflow_history(event_type);
CREATE INDEX idx_workflow_history_created_at ON workflow_history(created_at DESC);
CREATE INDEX idx_workflow_history_user ON workflow_history(user_id);
CREATE INDEX idx_workflow_history_states ON workflow_history(from_state_id, to_state_id);
CREATE INDEX idx_workflow_history_task ON workflow_history(task_id);
CREATE INDEX idx_workflow_history_data ON workflow_history USING gin(event_data);

-- Add comments
COMMENT ON TABLE workflow_history IS 'Complete audit trail of all workflow changes';
COMMENT ON COLUMN workflow_history.event_type IS 'Type of event that occurred';
COMMENT ON COLUMN workflow_history.event_data IS 'JSON data associated with the event';
