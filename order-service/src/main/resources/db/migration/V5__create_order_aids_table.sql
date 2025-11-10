-- Create order_aids table
CREATE TABLE IF NOT EXISTS order_aids (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    aid_code VARCHAR(50) NOT NULL,
    aid_name VARCHAR(255) NOT NULL,
    aid_type VARCHAR(100),
    description TEXT,
    amount DECIMAL(15, 2) NOT NULL DEFAULT 0,
    provider VARCHAR(255),
    eligibility_criteria TEXT,
    approval_status VARCHAR(50) DEFAULT 'PENDING' CHECK (approval_status IN ('PENDING', 'APPROVED', 'REJECTED')),
    approved_by_user_id BIGINT,
    approved_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by_user_id BIGINT NOT NULL,
    CONSTRAINT fk_order_aids_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_order_aids_order_id ON order_aids(order_id);
CREATE INDEX IF NOT EXISTS idx_order_aids_code ON order_aids(aid_code);
CREATE INDEX IF NOT EXISTS idx_order_aids_type ON order_aids(aid_type);
CREATE INDEX IF NOT EXISTS idx_order_aids_approval ON order_aids(approval_status);

-- Comments
COMMENT ON TABLE order_aids IS 'Financial aids, subsidies, manufacturer rebates, and incentives';
COMMENT ON COLUMN order_aids.amount IS 'Aid amount (positive value reduces customer price)';
