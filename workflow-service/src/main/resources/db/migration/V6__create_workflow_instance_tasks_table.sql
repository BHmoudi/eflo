-- Create workflow_instance_tasks table
-- Stores actual task instances for workflow execution

CREATE TABLE workflow_instance_tasks (
    id BIGSERIAL PRIMARY KEY,
    instance_id BIGINT NOT NULL,
    state_task_id BIGINT NOT NULL,
    task_code VARCHAR(50) NOT NULL,
    task_name VARCHAR(255) NOT NULL,
    task_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    assigned_to_user_id BIGINT,
    assigned_to_role VARCHAR(50),
    assigned_at TIMESTAMP,
    assigned_by VARCHAR(100),
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    expected_completion_at TIMESTAMP,
    is_overdue BOOLEAN NOT NULL DEFAULT false,
    overdue_since TIMESTAMP,
    escalation_level INTEGER NOT NULL DEFAULT 0,
    escalated_to_user_id BIGINT,
    escalated_at TIMESTAMP,
    task_data JSONB,
    completion_data JSONB,
    comments TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_workflow_instance_tasks_instance FOREIGN KEY (instance_id)
        REFERENCES workflow_instances(id) ON DELETE CASCADE,
    CONSTRAINT fk_workflow_instance_tasks_state_task FOREIGN KEY (state_task_id)
        REFERENCES workflow_state_tasks(id),
    CONSTRAINT check_task_status CHECK (task_status IN (
        'PENDING', 'ASSIGNED', 'IN_PROGRESS', 'COMPLETED',
        'SKIPPED', 'FAILED', 'CANCELLED'
    ))
);

-- Create indexes
CREATE INDEX idx_workflow_instance_tasks_instance ON workflow_instance_tasks(instance_id);
CREATE INDEX idx_workflow_instance_tasks_state_task ON workflow_instance_tasks(state_task_id);
CREATE INDEX idx_workflow_instance_tasks_status ON workflow_instance_tasks(task_status);
CREATE INDEX idx_workflow_instance_tasks_assigned_user ON workflow_instance_tasks(assigned_to_user_id);
CREATE INDEX idx_workflow_instance_tasks_assigned_role ON workflow_instance_tasks(assigned_to_role);
CREATE INDEX idx_workflow_instance_tasks_overdue ON workflow_instance_tasks(is_overdue) WHERE is_overdue = true;
CREATE INDEX idx_workflow_instance_tasks_escalation ON workflow_instance_tasks(escalation_level) WHERE escalation_level > 0;
CREATE INDEX idx_workflow_instance_tasks_dates ON workflow_instance_tasks(assigned_at, completed_at);
CREATE INDEX idx_workflow_instance_tasks_data ON workflow_instance_tasks USING gin(task_data);
CREATE INDEX idx_workflow_instance_tasks_completion ON workflow_instance_tasks USING gin(completion_data);

-- Add comments
COMMENT ON TABLE workflow_instance_tasks IS 'Stores actual task instances for workflow execution';
COMMENT ON COLUMN workflow_instance_tasks.task_status IS 'Current status of the task';
COMMENT ON COLUMN workflow_instance_tasks.escalation_level IS 'Number of times this task has been escalated';
COMMENT ON COLUMN workflow_instance_tasks.task_data IS 'JSON data associated with the task';
COMMENT ON COLUMN workflow_instance_tasks.completion_data IS 'JSON data provided upon task completion';
