-- Create order_supplements table
CREATE TABLE IF NOT EXISTS order_supplements (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    supplement_code VARCHAR(50) NOT NULL,
    supplement_name VARCHAR(255) NOT NULL,
    supplement_type VARCHAR(100),
    description TEXT,
    amount DECIMAL(15, 2) NOT NULL DEFAULT 0,
    reason TEXT,
    approved_by_user_id BIGINT,
    approved_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by_user_id BIGINT NOT NULL,
    CONSTRAINT fk_order_supplements_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_order_supplements_order_id ON order_supplements(order_id);
CREATE INDEX IF NOT EXISTS idx_order_supplements_code ON order_supplements(supplement_code);
CREATE INDEX IF NOT EXISTS idx_order_supplements_type ON order_supplements(supplement_type);

-- Comments
COMMENT ON TABLE order_supplements IS 'Additional charges, fees, taxes, or surcharges added to orders';
COMMENT ON COLUMN order_supplements.amount IS 'Supplement amount (positive value increases customer price)';
