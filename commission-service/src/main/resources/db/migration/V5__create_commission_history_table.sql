-- V5__create_commission_history_table.sql
-- Commission History: Audit trail for all commission changes

CREATE TABLE commission_history (
    id BIGSERIAL PRIMARY KEY,

    -- Reference to Commission
    commission_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    order_number VARCHAR(50) NOT NULL,

    -- Change Information
    change_type VARCHAR(50) NOT NULL, -- CALCULATED, RECALCULATED, VALIDATED, ADJUSTED, PAID, CANCELLED
    change_description TEXT,

    -- Previous and New Values
    previous_status VARCHAR(30),
    new_status VARCHAR(30),
    previous_amount DECIMAL(12,2),
    new_amount DECIMAL(12,2),
    amount_difference DECIMAL(12,2),

    -- Change Details
    change_details JSONB,
    change_reason VARCHAR(500),

    -- Audit Fields
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    changed_by VARCHAR(100) NOT NULL,

    -- Foreign Key
    CONSTRAINT fk_history_commission FOREIGN KEY (commission_id)
        REFERENCES commissions(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_commission_history_commission_id ON commission_history(commission_id);
CREATE INDEX idx_commission_history_order_id ON commission_history(order_id);
CREATE INDEX idx_commission_history_change_type ON commission_history(change_type);
CREATE INDEX idx_commission_history_changed_at ON commission_history(changed_at);
CREATE INDEX idx_commission_history_changed_by ON commission_history(changed_by);

-- Comments
COMMENT ON TABLE commission_history IS 'Complete audit trail of all commission changes';
COMMENT ON COLUMN commission_history.change_type IS 'Type of change (CALCULATED, RECALCULATED, VALIDATED, ADJUSTED, PAID, CANCELLED)';
COMMENT ON COLUMN commission_history.change_details IS 'Detailed JSON information about the change';
