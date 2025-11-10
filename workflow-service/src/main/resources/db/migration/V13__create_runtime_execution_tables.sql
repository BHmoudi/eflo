-- Migration V13: Create Runtime Execution and Tracking Tables
-- This migration creates tables for workflow execution, approvals, and notifications

-- ============================================
-- ENHANCE WORKFLOW INSTANCE TASKS
-- ============================================

-- Add runtime execution columns to workflow_instance_tasks
ALTER TABLE workflow_instance_tasks
    ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'EN_ATTENTE', -- EN_ATTENTE, RECU, REALISE, SKIPPED
    ADD COLUMN IF NOT EXISTS assigned_user_id BIGINT,
    ADD COLUMN IF NOT EXISTS assigned_role_id BIGINT REFERENCES workflow_roles(id),
    ADD COLUMN IF NOT EXISTS assigned_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS started_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS input_data JSONB, -- User input data for actions
    ADD COLUMN IF NOT EXISTS uploaded_documents JSONB, -- Document references [{id, name, url, uploadedAt, uploadedBy}]
    ADD COLUMN IF NOT EXISTS current_approval_level INTEGER DEFAULT 0,
    ADD COLUMN IF NOT EXISTS approval_status VARCHAR(20), -- PENDING, APPROVED, REJECTED
    ADD COLUMN IF NOT EXISTS sla_due_date TIMESTAMP,
    ADD COLUMN IF NOT EXISTS escalated BOOLEAN DEFAULT false,
    ADD COLUMN IF NOT EXISTS escalated_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS escalated_to_user_id BIGINT,
    ADD COLUMN IF NOT EXISTS completed_by_user_id BIGINT,
    ADD COLUMN IF NOT EXISTS completion_comments TEXT,
    ADD COLUMN IF NOT EXISTS metadata JSONB; -- Additional metadata

-- Add indexes for performance
CREATE INDEX IF NOT EXISTS idx_instance_tasks_status ON workflow_instance_tasks(status);
CREATE INDEX IF NOT EXISTS idx_instance_tasks_assigned_user ON workflow_instance_tasks(assigned_user_id);
CREATE INDEX IF NOT EXISTS idx_instance_tasks_assigned_role ON workflow_instance_tasks(assigned_role_id);
CREATE INDEX IF NOT EXISTS idx_instance_tasks_sla ON workflow_instance_tasks(sla_due_date) WHERE status NOT IN ('REALISE', 'SKIPPED');
CREATE INDEX IF NOT EXISTS idx_instance_tasks_escalated ON workflow_instance_tasks(escalated) WHERE escalated = true;

-- Add constraint for status values
ALTER TABLE workflow_instance_tasks
    ADD CONSTRAINT chk_instance_task_status CHECK (status IN ('EN_ATTENTE', 'RECU', 'REALISE', 'SKIPPED', 'CANCELLED'));

-- ============================================
-- APPROVAL TRACKING
-- ============================================

-- Table: workflow_task_approvals
-- Tracks individual approvals for tasks
CREATE TABLE workflow_task_approvals (
    id BIGSERIAL PRIMARY KEY,
    instance_task_id BIGINT NOT NULL REFERENCES workflow_instance_tasks(id) ON DELETE CASCADE,
    approval_chain_id BIGINT REFERENCES workflow_approval_chains(id),
    approval_level_id BIGINT REFERENCES workflow_approval_levels(id),
    level_order INTEGER NOT NULL,
    approver_user_id BIGINT NOT NULL,
    approver_role_id BIGINT REFERENCES workflow_roles(id),
    approval_status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED, DELEGATED
    comments TEXT,
    approved_at TIMESTAMP,
    rejected_at TIMESTAMP,
    delegated_to_user_id BIGINT,
    delegated_at TIMESTAMP,
    timeout_at TIMESTAMP,
    notified_at TIMESTAMP,
    reminder_sent_at TIMESTAMP,
    ip_address VARCHAR(50),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_approval_status CHECK (approval_status IN ('PENDING', 'APPROVED', 'REJECTED', 'DELEGATED', 'TIMEOUT'))
);

CREATE INDEX idx_task_approvals_instance_task ON workflow_task_approvals(instance_task_id);
CREATE INDEX idx_task_approvals_approver ON workflow_task_approvals(approver_user_id);
CREATE INDEX idx_task_approvals_status ON workflow_task_approvals(approval_status);
CREATE INDEX idx_task_approvals_level ON workflow_task_approvals(approval_level_id);
CREATE INDEX idx_task_approvals_pending ON workflow_task_approvals(approver_user_id, approval_status) WHERE approval_status = 'PENDING';

-- ============================================
-- NOTIFICATIONS
-- ============================================

-- Table: workflow_notifications
-- Notification queue for email, SMS, in-app notifications
CREATE TABLE workflow_notifications (
    id BIGSERIAL PRIMARY KEY,
    instance_id BIGINT REFERENCES workflow_instances(id) ON DELETE CASCADE,
    instance_task_id BIGINT REFERENCES workflow_instance_tasks(id) ON DELETE CASCADE,
    recipient_user_id BIGINT NOT NULL,
    recipient_email VARCHAR(255),
    recipient_phone VARCHAR(50),
    notification_type VARCHAR(50) NOT NULL, -- TASK_ASSIGNED, APPROVAL_REQUIRED, TASK_COMPLETED, SLA_WARNING, ESCALATION, etc.
    notification_channel VARCHAR(20) NOT NULL, -- EMAIL, SMS, IN_APP, PUSH
    priority VARCHAR(20) DEFAULT 'NORMAL', -- LOW, NORMAL, HIGH, URGENT
    subject VARCHAR(500),
    message TEXT NOT NULL,
    template_code VARCHAR(100),
    template_data JSONB,
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, SENT, FAILED, CANCELLED
    sent_at TIMESTAMP,
    read_at TIMESTAMP,
    error_message TEXT,
    retry_count INTEGER DEFAULT 0,
    max_retries INTEGER DEFAULT 3,
    scheduled_for TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_notification_status CHECK (status IN ('PENDING', 'SENT', 'FAILED', 'CANCELLED')),
    CONSTRAINT chk_notification_channel CHECK (notification_channel IN ('EMAIL', 'SMS', 'IN_APP', 'PUSH')),
    CONSTRAINT chk_notification_priority CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT'))
);

CREATE INDEX idx_notifications_recipient ON workflow_notifications(recipient_user_id);
CREATE INDEX idx_notifications_status ON workflow_notifications(status);
CREATE INDEX idx_notifications_scheduled ON workflow_notifications(scheduled_for) WHERE status = 'PENDING';
CREATE INDEX idx_notifications_task ON workflow_notifications(instance_task_id);
CREATE INDEX idx_notifications_unread ON workflow_notifications(recipient_user_id, read_at) WHERE read_at IS NULL AND notification_channel = 'IN_APP';

-- ============================================
-- TASK HISTORY AND AUDIT
-- ============================================

-- Table: workflow_task_history
-- Detailed audit trail for task actions
CREATE TABLE workflow_task_history (
    id BIGSERIAL PRIMARY KEY,
    instance_task_id BIGINT NOT NULL REFERENCES workflow_instance_tasks(id) ON DELETE CASCADE,
    action_type VARCHAR(50) NOT NULL, -- CREATED, ASSIGNED, STARTED, UPDATED, COMPLETED, APPROVED, REJECTED, DELEGATED, ESCALATED
    action_by_user_id BIGINT NOT NULL,
    action_by_role_id BIGINT REFERENCES workflow_roles(id),
    old_status VARCHAR(50),
    new_status VARCHAR(50),
    old_assignee_user_id BIGINT,
    new_assignee_user_id BIGINT,
    comments TEXT,
    changes JSONB, -- Detailed changes in JSON format
    ip_address VARCHAR(50),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_task_action_type CHECK (action_type IN (
        'CREATED', 'ASSIGNED', 'STARTED', 'UPDATED', 'COMPLETED',
        'APPROVED', 'REJECTED', 'DELEGATED', 'ESCALATED', 'SKIPPED',
        'DOCUMENT_UPLOADED', 'DATA_ENTERED', 'CANCELLED'
    ))
);

CREATE INDEX idx_task_history_instance_task ON workflow_task_history(instance_task_id);
CREATE INDEX idx_task_history_action_by ON workflow_task_history(action_by_user_id);
CREATE INDEX idx_task_history_created ON workflow_task_history(created_at);
CREATE INDEX idx_task_history_action_type ON workflow_task_history(action_type);

-- ============================================
-- DELEGATION TRACKING
-- ============================================

-- Table: workflow_task_delegations
-- Tracks task delegations
CREATE TABLE workflow_task_delegations (
    id BIGSERIAL PRIMARY KEY,
    instance_task_id BIGINT NOT NULL REFERENCES workflow_instance_tasks(id) ON DELETE CASCADE,
    delegated_from_user_id BIGINT NOT NULL,
    delegated_to_user_id BIGINT NOT NULL,
    delegation_reason TEXT,
    delegated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    accepted_at TIMESTAMP,
    rejected_at TIMESTAMP,
    delegation_status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, ACCEPTED, REJECTED, COMPLETED
    completed_at TIMESTAMP,
    created_by_user_id BIGINT NOT NULL,
    CONSTRAINT chk_delegation_status CHECK (delegation_status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'COMPLETED', 'CANCELLED'))
);

CREATE INDEX idx_delegations_instance_task ON workflow_task_delegations(instance_task_id);
CREATE INDEX idx_delegations_from_user ON workflow_task_delegations(delegated_from_user_id);
CREATE INDEX idx_delegations_to_user ON workflow_task_delegations(delegated_to_user_id);
CREATE INDEX idx_delegations_status ON workflow_task_delegations(delegation_status);

-- ============================================
-- COMMENTS AND COLLABORATION
-- ============================================

-- Table: workflow_task_comments
-- Task comments and discussions
CREATE TABLE workflow_task_comments (
    id BIGSERIAL PRIMARY KEY,
    instance_task_id BIGINT NOT NULL REFERENCES workflow_instance_tasks(id) ON DELETE CASCADE,
    parent_comment_id BIGINT REFERENCES workflow_task_comments(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL,
    comment_text TEXT NOT NULL,
    is_internal BOOLEAN DEFAULT false, -- Internal comment not visible to external users
    mentions BIGINT[], -- Array of user IDs mentioned in comment
    attachments JSONB, -- Optional file attachments
    edited_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_task_comments_instance_task ON workflow_task_comments(instance_task_id);
CREATE INDEX idx_task_comments_user ON workflow_task_comments(user_id);
CREATE INDEX idx_task_comments_parent ON workflow_task_comments(parent_comment_id);
CREATE INDEX idx_task_comments_created ON workflow_task_comments(created_at);

-- ============================================
-- SLA AND METRICS
-- ============================================

-- Table: workflow_task_metrics
-- Performance metrics for tasks
CREATE TABLE workflow_task_metrics (
    id BIGSERIAL PRIMARY KEY,
    instance_task_id BIGINT NOT NULL REFERENCES workflow_instance_tasks(id) ON DELETE CASCADE,
    assigned_duration_seconds INTEGER, -- Time from assignment to start
    execution_duration_seconds INTEGER, -- Time from start to completion
    total_duration_seconds INTEGER, -- Total time from creation to completion
    approval_duration_seconds INTEGER, -- Time spent in approval
    number_of_reassignments INTEGER DEFAULT 0,
    number_of_escalations INTEGER DEFAULT 0,
    number_of_delegations INTEGER DEFAULT 0,
    sla_met BOOLEAN,
    sla_breach_hours INTEGER,
    first_response_time_seconds INTEGER,
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_task_metrics UNIQUE(instance_task_id)
);

CREATE INDEX idx_task_metrics_instance_task ON workflow_task_metrics(instance_task_id);
CREATE INDEX idx_task_metrics_sla ON workflow_task_metrics(sla_met);

-- ============================================
-- UPDATE EXISTING HISTORY TABLE
-- ============================================

-- Enhance workflow_history table with more detail
ALTER TABLE workflow_history
    ADD COLUMN IF NOT EXISTS action_by_user_id BIGINT,
    ADD COLUMN IF NOT EXISTS action_by_role_id BIGINT REFERENCES workflow_roles(id),
    ADD COLUMN IF NOT EXISTS task_id BIGINT REFERENCES workflow_instance_tasks(id),
    ADD COLUMN IF NOT EXISTS metadata JSONB;

CREATE INDEX IF NOT EXISTS idx_workflow_history_user ON workflow_history(action_by_user_id);
CREATE INDEX IF NOT EXISTS idx_workflow_history_task ON workflow_history(task_id);

-- Comments
COMMENT ON TABLE workflow_task_approvals IS 'Tracks individual approval actions for tasks';
COMMENT ON TABLE workflow_notifications IS 'Notification queue for all workflow events';
COMMENT ON TABLE workflow_task_history IS 'Complete audit trail of task actions';
COMMENT ON TABLE workflow_task_delegations IS 'Tracks task delegation between users';
COMMENT ON TABLE workflow_task_comments IS 'Comments and collaboration on tasks';
COMMENT ON TABLE workflow_task_metrics IS 'Performance and SLA metrics for tasks';
COMMENT ON COLUMN workflow_instance_tasks.status IS 'EN_ATTENTE: waiting, RECU: received/in-progress, REALISE: completed';
COMMENT ON COLUMN workflow_task_approvals.approval_status IS 'Current status of this approval action';
COMMENT ON COLUMN workflow_notifications.notification_channel IS 'Delivery channel: EMAIL, SMS, IN_APP, or PUSH';
