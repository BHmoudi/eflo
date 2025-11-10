-- Create order_history table
CREATE TABLE IF NOT EXISTS order_history (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_description TEXT NOT NULL,
    previous_status VARCHAR(50),
    new_status VARCHAR(50),
    previous_data JSONB,
    new_data JSONB,
    changed_by_user_id BIGINT NOT NULL,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(50),
    user_agent TEXT,
    CONSTRAINT fk_order_history_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_order_history_order_id ON order_history(order_id);
CREATE INDEX IF NOT EXISTS idx_order_history_event_type ON order_history(event_type);
CREATE INDEX IF NOT EXISTS idx_order_history_changed_at ON order_history(changed_at);
CREATE INDEX IF NOT EXISTS idx_order_history_changed_by ON order_history(changed_by_user_id);

-- Comments
COMMENT ON TABLE order_history IS 'Complete audit trail for all order changes';
COMMENT ON COLUMN order_history.previous_data IS 'JSONB snapshot of data before change';
COMMENT ON COLUMN order_history.new_data IS 'JSONB snapshot of data after change';
