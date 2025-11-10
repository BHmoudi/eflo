-- Create workflow_state_tasks table
-- Defines task templates for each state

CREATE TABLE workflow_state_tasks (
    id BIGSERIAL PRIMARY KEY,
    state_id BIGINT NOT NULL,
    task_code VARCHAR(50) NOT NULL,
    task_name VARCHAR(255) NOT NULL,
    description TEXT,
    task_type VARCHAR(30) NOT NULL,
    task_order INTEGER NOT NULL,
    assigned_to_role VARCHAR(50),
    assigned_to_user_id BIGINT,
    is_mandatory BOOLEAN NOT NULL DEFAULT true,
    expected_duration_hours INTEGER,
    auto_assign BOOLEAN NOT NULL DEFAULT true,
    requires_approval BOOLEAN NOT NULL DEFAULT false,
    form_definition JSONB,
    validation_rules JSONB,
    configuration JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_workflow_state_tasks_state FOREIGN KEY (state_id)
        REFERENCES workflow_states(id) ON DELETE CASCADE,
    CONSTRAINT check_task_type CHECK (task_type IN (
        'DATA_INPUT', 'DOCUMENT_UPLOAD', 'APPROVAL', 'VALIDATION',
        'NOTIFICATION', 'INTEGRATION', 'MANUAL', 'AUTOMATED'
    )),
    CONSTRAINT unique_task_code_per_state UNIQUE (state_id, task_code)
);

-- Create indexes
CREATE INDEX idx_workflow_state_tasks_state ON workflow_state_tasks(state_id);
CREATE INDEX idx_workflow_state_tasks_code ON workflow_state_tasks(task_code);
CREATE INDEX idx_workflow_state_tasks_type ON workflow_state_tasks(task_type);
CREATE INDEX idx_workflow_state_tasks_role ON workflow_state_tasks(assigned_to_role);
CREATE INDEX idx_workflow_state_tasks_user ON workflow_state_tasks(assigned_to_user_id);
CREATE INDEX idx_workflow_state_tasks_order ON workflow_state_tasks(state_id, task_order);
CREATE INDEX idx_workflow_state_tasks_form ON workflow_state_tasks USING gin(form_definition);

-- Add comments
COMMENT ON TABLE workflow_state_tasks IS 'Defines task templates for each workflow state';
COMMENT ON COLUMN workflow_state_tasks.task_type IS 'Type of task to be performed';
COMMENT ON COLUMN workflow_state_tasks.assigned_to_role IS 'Role that should be assigned this task';
COMMENT ON COLUMN workflow_state_tasks.form_definition IS 'JSON definition of the form for this task';
COMMENT ON COLUMN workflow_state_tasks.validation_rules IS 'JSON validation rules for task completion';
