-- Add new enum values to document_status
-- PostgreSQL requires adding enum values one at a time

-- Add PENDING_UPLOAD status (for document placeholders)
ALTER TYPE document_status ADD VALUE IF NOT EXISTS 'PENDING_UPLOAD';

-- Add UPLOADED status (for uploaded but not yet validated)
ALTER TYPE document_status ADD VALUE IF NOT EXISTS 'UPLOADED';

-- Add comment explaining the new statuses
COMMENT ON TYPE document_status IS 'Document lifecycle status: PENDING_UPLOAD (placeholder created), UPLOADED (file uploaded), PENDING (awaiting validation), VALIDATED, REJECTED, EXPIRED, ARCHIVED, DELETED';
