-- Create workflow_transitions table
-- Defines allowed transitions between states

CREATE TABLE workflow_transitions (
    id BIGSERIAL PRIMARY KEY,
    process_id BIGINT NOT NULL,
    from_state_id BIGINT NOT NULL,
    to_state_id BIGINT NOT NULL,
    transition_name VARCHAR(255) NOT NULL,
    description TEXT,
    transition_type VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    requires_approval BOOLEAN NOT NULL DEFAULT false,
    requires_tasks_completion BOOLEAN NOT NULL DEFAULT true,
    requires_documents BOOLEAN NOT NULL DEFAULT false,
    auto_transition BOOLEAN NOT NULL DEFAULT false,
    condition_expression TEXT,
    notification_enabled BOOLEAN NOT NULL DEFAULT true,
    configuration JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_workflow_transitions_process FOREIGN KEY (process_id)
        REFERENCES workflow_processes(id) ON DELETE CASCADE,
    CONSTRAINT fk_workflow_transitions_from_state FOREIGN KEY (from_state_id)
        REFERENCES workflow_states(id) ON DELETE CASCADE,
    CONSTRAINT fk_workflow_transitions_to_state FOREIGN KEY (to_state_id)
        REFERENCES workflow_states(id) ON DELETE CASCADE,
    CONSTRAINT check_transition_type CHECK (transition_type IN ('NORMAL', 'ESCALATION', 'ROLLBACK', 'CANCEL')),
    CONSTRAINT check_different_states CHECK (from_state_id != to_state_id)
);

-- Create indexes
CREATE INDEX idx_workflow_transitions_process ON workflow_transitions(process_id);
CREATE INDEX idx_workflow_transitions_from_state ON workflow_transitions(from_state_id);
CREATE INDEX idx_workflow_transitions_to_state ON workflow_transitions(to_state_id);
CREATE INDEX idx_workflow_transitions_type ON workflow_transitions(transition_type);
CREATE INDEX idx_workflow_transitions_auto ON workflow_transitions(auto_transition);
CREATE INDEX idx_workflow_transitions_config ON workflow_transitions USING gin(configuration);

-- Add comments
COMMENT ON TABLE workflow_transitions IS 'Defines allowed transitions between workflow states';
COMMENT ON COLUMN workflow_transitions.transition_type IS 'Type of transition (NORMAL, ESCALATION, ROLLBACK, CANCEL)';
COMMENT ON COLUMN workflow_transitions.condition_expression IS 'Optional SpEL expression for conditional transitions';
COMMENT ON COLUMN workflow_transitions.auto_transition IS 'Whether this transition happens automatically';
