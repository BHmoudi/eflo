-- Additional performance indexes and constraints

-- Composite indexes for common queries
CREATE INDEX idx_instances_process_status ON workflow_instances(process_id, instance_status);
CREATE INDEX idx_instances_order_status ON workflow_instances(order_id, instance_status);
CREATE INDEX idx_tasks_instance_status ON workflow_instance_tasks(instance_id, task_status);
CREATE INDEX idx_tasks_user_status ON workflow_instance_tasks(assigned_to_user_id, task_status)
    WHERE assigned_to_user_id IS NOT NULL;
CREATE INDEX idx_tasks_role_status ON workflow_instance_tasks(assigned_to_role, task_status)
    WHERE assigned_to_role IS NOT NULL;

-- Indexes for deadline monitoring
CREATE INDEX idx_instances_expected_completion ON workflow_instances(expected_completion_date)
    WHERE instance_status IN ('RUNNING', 'PAUSED');
CREATE INDEX idx_tasks_expected_completion ON workflow_instance_tasks(expected_completion_at)
    WHERE task_status IN ('PENDING', 'ASSIGNED', 'IN_PROGRESS');

-- Partial indexes for active workflows
CREATE INDEX idx_instances_active ON workflow_instances(id, current_state_id)
    WHERE instance_status IN ('RUNNING', 'PAUSED');
CREATE INDEX idx_tasks_active ON workflow_instance_tasks(id, instance_id)
    WHERE task_status IN ('PENDING', 'ASSIGNED', 'IN_PROGRESS');

-- Covering index for dashboard queries
CREATE INDEX idx_instances_dashboard ON workflow_instances(
    instance_status, priority, is_overdue, created_at DESC
) INCLUDE (process_id, order_id, current_state_id);

-- Add trigger for updated_at timestamp on workflow_processes
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_workflow_processes_updated_at
    BEFORE UPDATE ON workflow_processes
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_workflow_states_updated_at
    BEFORE UPDATE ON workflow_states
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_workflow_transitions_updated_at
    BEFORE UPDATE ON workflow_transitions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_workflow_state_tasks_updated_at
    BEFORE UPDATE ON workflow_state_tasks
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_workflow_instances_updated_at
    BEFORE UPDATE ON workflow_instances
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_workflow_instance_tasks_updated_at
    BEFORE UPDATE ON workflow_instance_tasks
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Add check constraints for date logic
ALTER TABLE workflow_instances
    ADD CONSTRAINT check_start_before_end
    CHECK (start_date IS NULL OR end_date IS NULL OR start_date <= end_date);

ALTER TABLE workflow_instance_tasks
    ADD CONSTRAINT check_assigned_before_started
    CHECK (assigned_at IS NULL OR started_at IS NULL OR assigned_at <= started_at);

ALTER TABLE workflow_instance_tasks
    ADD CONSTRAINT check_started_before_completed
    CHECK (started_at IS NULL OR completed_at IS NULL OR started_at <= completed_at);

-- Comments
COMMENT ON INDEX idx_instances_process_status IS 'Optimize queries filtering by process and status';
COMMENT ON INDEX idx_instances_expected_completion IS 'Optimize deadline monitoring queries';
COMMENT ON INDEX idx_tasks_user_status IS 'Optimize user task list queries';
