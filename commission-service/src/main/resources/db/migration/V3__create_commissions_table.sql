-- V3__create_commissions_table.sql
-- Commissions: Calculated commission records

CREATE TABLE commissions (
    id BIGSERIAL PRIMARY KEY,

    -- Reference to Order
    order_id BIGINT NOT NULL UNIQUE,
    order_number VARCHAR(50) NOT NULL,
    order_type VARCHAR(20) NOT NULL,

    -- Reference to Commission Scale Used
    commission_scale_id BIGINT NOT NULL,
    scale_code VARCHAR(50) NOT NULL,
    scale_name VARCHAR(255) NOT NULL,

    -- Business Context
    business_unit_id BIGINT NOT NULL,
    business_unit_name VARCHAR(255),

    -- People Involved
    salesperson_id BIGINT NOT NULL,
    salesperson_name VARCHAR(255) NOT NULL,
    manager_id BIGINT,
    manager_name VARCHAR(255),

    -- Order Financial Details
    order_total_revenue DECIMAL(12,2) NOT NULL,
    order_net_margin DECIMAL(12,2) NOT NULL,

    -- Vehicle Commission Calculation
    vehicle_commission_base DECIMAL(12,2),
    vehicle_commission_rate DECIMAL(5,2),
    vehicle_commission_excl_tax DECIMAL(12,2) NOT NULL DEFAULT 0,
    vehicle_commission_tax DECIMAL(12,2) DEFAULT 0,
    vehicle_commission_incl_tax DECIMAL(12,2) DEFAULT 0,

    -- Accessory Commission
    accessory_commission_excl_tax DECIMAL(12,2) DEFAULT 0,
    accessory_commission_tax DECIMAL(12,2) DEFAULT 0,
    accessory_commission_incl_tax DECIMAL(12,2) DEFAULT 0,

    -- Service Commission
    service_commission_excl_tax DECIMAL(12,2) DEFAULT 0,
    service_commission_tax DECIMAL(12,2) DEFAULT 0,
    service_commission_incl_tax DECIMAL(12,2) DEFAULT 0,

    -- Total Commission
    total_commission_excl_tax DECIMAL(12,2) NOT NULL,
    total_commission_tax DECIMAL(12,2) NOT NULL DEFAULT 0,
    total_commission_incl_tax DECIMAL(12,2) NOT NULL,

    -- Manager Split
    manager_split_enabled BOOLEAN DEFAULT false,
    manager_split_percentage DECIMAL(5,2),
    manager_commission_excl_tax DECIMAL(12,2) DEFAULT 0,
    salesperson_net_commission DECIMAL(12,2), -- After manager split

    -- Calculation Details
    calculation_method VARCHAR(30) NOT NULL,
    calculation_details JSONB,
    tier_breakdown JSONB,

    -- Status and Dates
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_CALCULATION',
    calculation_date TIMESTAMP,
    validation_date TIMESTAMP,
    payment_date TIMESTAMP,

    -- Payment Information
    payment_batch_id BIGINT,
    payment_reference VARCHAR(100),
    payment_due_date DATE,

    -- Adjustments
    adjustment_reason VARCHAR(500),
    adjustment_amount DECIMAL(12,2),
    adjusted_by VARCHAR(100),
    adjusted_at TIMESTAMP,

    -- Notes
    notes TEXT,

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    version INT DEFAULT 0,

    -- Foreign Key
    CONSTRAINT fk_commission_scale FOREIGN KEY (commission_scale_id)
        REFERENCES commission_scales(id),

    -- Constraints
    CONSTRAINT chk_commission_amounts CHECK (total_commission_excl_tax >= 0),
    CONSTRAINT chk_manager_split_pct CHECK (manager_split_percentage IS NULL OR (manager_split_percentage >= 0 AND manager_split_percentage <= 100))
);

-- Indexes
CREATE INDEX idx_commissions_order_id ON commissions(order_id);
CREATE INDEX idx_commissions_order_number ON commissions(order_number);
CREATE INDEX idx_commissions_salesperson ON commissions(salesperson_id);
CREATE INDEX idx_commissions_manager ON commissions(manager_id);
CREATE INDEX idx_commissions_business_unit ON commissions(business_unit_id);
CREATE INDEX idx_commissions_status ON commissions(status);
CREATE INDEX idx_commissions_payment_batch ON commissions(payment_batch_id);
CREATE INDEX idx_commissions_calculation_date ON commissions(calculation_date);
CREATE INDEX idx_commissions_payment_due_date ON commissions(payment_due_date);
CREATE INDEX idx_commissions_composite_search ON commissions(salesperson_id, status, calculation_date);

-- Comments
COMMENT ON TABLE commissions IS 'Calculated commission records for orders';
COMMENT ON COLUMN commissions.calculation_details IS 'Detailed breakdown of commission calculation';
COMMENT ON COLUMN commissions.tier_breakdown IS 'For tiered calculations, breakdown by tier';
COMMENT ON COLUMN commissions.salesperson_net_commission IS 'Net commission to salesperson after manager split';
