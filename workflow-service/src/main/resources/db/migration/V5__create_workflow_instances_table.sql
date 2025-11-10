-- Create workflow_instances table
-- Stores running workflow instances

CREATE TABLE workflow_instances (
    id BIGSERIAL PRIMARY KEY,
    process_id BIGINT NOT NULL,
    current_state_id BIGINT,
    order_id BIGINT NOT NULL,
    instance_name VARCHAR(255),
    instance_status VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    expected_completion_date TIMESTAMP,
    actual_completion_date TIMESTAMP,
    is_overdue BOOLEAN NOT NULL DEFAULT false,
    overdue_since TIMESTAMP,
    priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    context_data JSONB,
    error_message TEXT,
    error_details JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT fk_workflow_instances_process FOREIGN KEY (process_id)
        REFERENCES workflow_processes(id),
    CONSTRAINT fk_workflow_instances_current_state FOREIGN KEY (current_state_id)
        REFERENCES workflow_states(id),
    CONSTRAINT check_instance_status CHECK (instance_status IN (
        'CREATED', 'RUNNING', 'PAUSED', 'COMPLETED', 'CANCELLED', 'ERROR'
    )),
    CONSTRAINT check_priority CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT'))
);

-- Create indexes
CREATE INDEX idx_workflow_instances_process ON workflow_instances(process_id);
CREATE INDEX idx_workflow_instances_current_state ON workflow_instances(current_state_id);
CREATE INDEX idx_workflow_instances_order ON workflow_instances(order_id);
CREATE INDEX idx_workflow_instances_status ON workflow_instances(instance_status);
CREATE INDEX idx_workflow_instances_overdue ON workflow_instances(is_overdue) WHERE is_overdue = true;
CREATE INDEX idx_workflow_instances_priority ON workflow_instances(priority);
CREATE INDEX idx_workflow_instances_dates ON workflow_instances(start_date, end_date);
CREATE INDEX idx_workflow_instances_context ON workflow_instances USING gin(context_data);
CREATE INDEX idx_workflow_instances_created_at ON workflow_instances(created_at DESC);

-- Add comments
COMMENT ON TABLE workflow_instances IS 'Stores running workflow instances';
COMMENT ON COLUMN workflow_instances.instance_status IS 'Current status of the workflow instance';
COMMENT ON COLUMN workflow_instances.context_data IS 'JSON data context for the workflow instance';
COMMENT ON COLUMN workflow_instances.is_overdue IS 'Whether the instance has exceeded its expected completion time';
