-- V4__create_commission_payments_table.sql
-- Commission Payments: Payment batches for commissions

CREATE TABLE commission_payments (
    id BIGSERIAL PRIMARY KEY,

    -- Batch Identification
    batch_number VARCHAR(50) NOT NULL UNIQUE,
    batch_name VARCHAR(255) NOT NULL,

    -- Payment Period
    payment_year INT NOT NULL,
    payment_month INT NOT NULL,
    payment_period VARCHAR(20) NOT NULL, -- Format: YYYY-MM

    -- Business Context
    business_unit_id BIGINT,
    business_unit_name VARCHAR(255),

    -- Payment Summary
    total_commissions INT NOT NULL DEFAULT 0,
    total_amount_excl_tax DECIMAL(15,2) NOT NULL DEFAULT 0,
    total_amount_tax DECIMAL(15,2) NOT NULL DEFAULT 0,
    total_amount_incl_tax DECIMAL(15,2) NOT NULL DEFAULT 0,

    -- Status
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    -- Payment Details
    payment_method VARCHAR(50),
    payment_reference VARCHAR(100),
    payment_file_path VARCHAR(500),

    -- Dates
    scheduled_payment_date DATE,
    processed_date TIMESTAMP,
    completed_date TIMESTAMP,

    -- Processing Details
    processing_started_at TIMESTAMP,
    processing_completed_at TIMESTAMP,
    processing_error TEXT,

    -- Notes
    notes TEXT,

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),

    -- Constraints
    CONSTRAINT chk_payment_year CHECK (payment_year >= 2020 AND payment_year <= 2100),
    CONSTRAINT chk_payment_month CHECK (payment_month >= 1 AND payment_month <= 12),
    CONSTRAINT chk_total_commissions CHECK (total_commissions >= 0),
    CONSTRAINT chk_total_amount CHECK (total_amount_excl_tax >= 0)
);

-- Indexes
CREATE INDEX idx_commission_payments_batch_number ON commission_payments(batch_number);
CREATE INDEX idx_commission_payments_period ON commission_payments(payment_year, payment_month);
CREATE INDEX idx_commission_payments_business_unit ON commission_payments(business_unit_id);
CREATE INDEX idx_commission_payments_status ON commission_payments(status);
CREATE INDEX idx_commission_payments_scheduled_date ON commission_payments(scheduled_payment_date);

-- Comments
COMMENT ON TABLE commission_payments IS 'Payment batches for processing commission payments';
COMMENT ON COLUMN commission_payments.batch_number IS 'Unique batch identifier (e.g., PAY-2024-01-001)';
COMMENT ON COLUMN commission_payments.payment_period IS 'Payment period in YYYY-MM format';
COMMENT ON COLUMN commission_payments.total_commissions IS 'Number of commission records in this batch';
