-- Create order_documents table for managing document uploads/downloads

CREATE TABLE IF NOT EXISTS order_documents (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(100),
    description TEXT,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    uploaded_by_user_id BIGINT NOT NULL,
    deleted_at TIMESTAMP,
    deleted_by_user_id BIGINT,
    version INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT fk_order_documents_order
        FOREIGN KEY (order_id)
        REFERENCES orders(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_document_type
        CHECK (document_type IN ('INVOICE', 'CONTRACT', 'INSURANCE', 'REGISTRATION',
                                 'PHOTO', 'ID_CARD', 'PROOF_OF_ADDRESS', 'OTHER')),

    CONSTRAINT chk_file_size
        CHECK (file_size > 0 AND file_size <= 10485760) -- Max 10MB
);

-- Create indexes for better query performance
CREATE INDEX idx_order_documents_order_id ON order_documents(order_id);
CREATE INDEX idx_order_documents_type ON order_documents(document_type);
CREATE INDEX idx_order_documents_uploaded_at ON order_documents(uploaded_at);
CREATE INDEX idx_order_documents_deleted_at ON order_documents(deleted_at);

-- Create composite index for common query patterns
CREATE INDEX idx_order_documents_order_type ON order_documents(order_id, document_type)
    WHERE deleted_at IS NULL;

-- Add comment to table
COMMENT ON TABLE order_documents IS 'Stores metadata for documents attached to orders (invoices, contracts, photos, etc.)';

-- Add comments to columns
COMMENT ON COLUMN order_documents.order_id IS 'Reference to the parent order';
COMMENT ON COLUMN order_documents.document_type IS 'Type of document: INVOICE, CONTRACT, INSURANCE, REGISTRATION, PHOTO, ID_CARD, PROOF_OF_ADDRESS, OTHER';
COMMENT ON COLUMN order_documents.file_name IS 'Original filename uploaded by user';
COMMENT ON COLUMN order_documents.file_path IS 'Relative path to stored file (either local filesystem or MinIO)';
COMMENT ON COLUMN order_documents.file_size IS 'File size in bytes';
COMMENT ON COLUMN order_documents.content_type IS 'MIME type of the file';
COMMENT ON COLUMN order_documents.description IS 'Optional description or notes about the document';
COMMENT ON COLUMN order_documents.uploaded_at IS 'Timestamp when document was uploaded';
COMMENT ON COLUMN order_documents.uploaded_by_user_id IS 'ID of user who uploaded the document';
COMMENT ON COLUMN order_documents.deleted_at IS 'Soft delete timestamp';
COMMENT ON COLUMN order_documents.deleted_by_user_id IS 'ID of user who deleted the document';
