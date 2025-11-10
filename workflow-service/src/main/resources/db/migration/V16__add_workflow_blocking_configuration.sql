-- Add blocking configuration to workflow states
-- Allows configuring how strict the workflow progression rules are

-- Add blocking behavior enum
CREATE TYPE blocking_behavior AS ENUM (
    'HARD_BLOCK',           -- Cannot progress until all requirements met
    'WARNING',              -- Show warning but allow progression
    'ADMIN_OVERRIDE_ONLY'   -- Only admins can override the block
);

-- Add blocking configuration columns to workflow_states
ALTER TABLE workflow_states
ADD COLUMN IF NOT EXISTS block_on_pending_documents BOOLEAN DEFAULT true,
ADD COLUMN IF NOT EXISTS block_on_incomplete_tasks BOOLEAN DEFAULT true,
ADD COLUMN IF NOT EXISTS block_on_pending_approvals BOOLEAN DEFAULT true,
ADD COLUMN IF NOT EXISTS blocking_behavior VARCHAR(30) DEFAULT 'HARD_BLOCK',
ADD COLUMN IF NOT EXISTS allow_manual_override BOOLEAN DEFAULT false,
ADD COLUMN IF NOT EXISTS override_roles TEXT; -- Comma-separated list of roles that can override

-- Add blocking configuration to workflow_processes (global defaults)
ALTER TABLE workflow_processes
ADD COLUMN IF NOT EXISTS strict_validation_mode BOOLEAN DEFAULT true,
ADD COLUMN IF NOT EXISTS allow_state_skip BOOLEAN DEFAULT false;

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_states_blocking_behavior ON workflow_states(blocking_behavior);
CREATE INDEX IF NOT EXISTS idx_states_block_documents ON workflow_states(block_on_pending_documents);

-- Add comments
COMMENT ON COLUMN workflow_states.block_on_pending_documents IS 'Block state transition if required documents are not validated';
COMMENT ON COLUMN workflow_states.block_on_incomplete_tasks IS 'Block state transition if mandatory tasks are not completed';
COMMENT ON COLUMN workflow_states.blocking_behavior IS 'How to enforce blocking: HARD_BLOCK, WARNING, or ADMIN_OVERRIDE_ONLY';
COMMENT ON COLUMN workflow_states.allow_manual_override IS 'Allow users to manually override blocking with justification';
COMMENT ON COLUMN workflow_states.override_roles IS 'Roles allowed to override blocks (comma-separated)';
