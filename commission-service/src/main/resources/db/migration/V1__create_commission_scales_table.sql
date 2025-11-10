-- V1__create_commission_scales_table.sql
-- Commission Scales: Configurable rules for calculating commissions

CREATE TABLE commission_scales (
    id BIGSERIAL PRIMARY KEY,

    -- Scale Identification
    scale_code VARCHAR(50) NOT NULL UNIQUE,
    scale_name VARCHAR(255) NOT NULL,
    scale_description TEXT,

    -- Business Context
    business_unit_id BIGINT NOT NULL,
    order_type VARCHAR(20) NOT NULL, -- VN, VO, EVO

    -- Commission Type and Calculation Method
    commission_type VARCHAR(20) NOT NULL, -- VEHICLE, ACCESSORY, SERVICE, COMBINED
    calculation_method VARCHAR(30) NOT NULL, -- PERCENTAGE_MARGIN, PERCENTAGE_REVENUE, TIERED, FIXED_AMOUNT, HYBRID

    -- Basic Calculation Parameters
    commission_rate_percentage DECIMAL(5,2),
    fixed_amount DECIMAL(12,2),

    -- Minimum Requirements
    minimum_margin_required DECIMAL(12,2),
    minimum_revenue_required DECIMAL(12,2),
    maximum_commission_amount DECIMAL(12,2),

    -- Manager Split Configuration
    manager_split_enabled BOOLEAN DEFAULT false,
    manager_split_percentage DECIMAL(5,2),

    -- Advanced Configuration (JSONB for flexibility)
    configuration_json JSONB,

    -- Validity Period
    valid_from DATE NOT NULL,
    valid_to DATE,

    -- Status
    is_active BOOLEAN DEFAULT true,
    is_default BOOLEAN DEFAULT false,

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    version INT DEFAULT 0,

    -- Constraints
    CONSTRAINT chk_commission_rate CHECK (commission_rate_percentage IS NULL OR (commission_rate_percentage >= 0 AND commission_rate_percentage <= 100)),
    CONSTRAINT chk_manager_split CHECK (manager_split_percentage IS NULL OR (manager_split_percentage >= 0 AND manager_split_percentage <= 100)),
    CONSTRAINT chk_valid_period CHECK (valid_to IS NULL OR valid_to >= valid_from)
);

-- Indexes
CREATE INDEX idx_commission_scales_business_unit ON commission_scales(business_unit_id);
CREATE INDEX idx_commission_scales_order_type ON commission_scales(order_type);
CREATE INDEX idx_commission_scales_active ON commission_scales(is_active);
CREATE INDEX idx_commission_scales_validity ON commission_scales(valid_from, valid_to);
CREATE INDEX idx_commission_scales_lookup ON commission_scales(business_unit_id, order_type, is_active);

-- Comments
COMMENT ON TABLE commission_scales IS 'Configurable commission calculation rules and scales';
COMMENT ON COLUMN commission_scales.scale_code IS 'Unique code identifying the commission scale';
COMMENT ON COLUMN commission_scales.calculation_method IS 'Method used to calculate commission (PERCENTAGE_MARGIN, TIERED, FIXED_AMOUNT, HYBRID)';
COMMENT ON COLUMN commission_scales.configuration_json IS 'Flexible JSON configuration for complex calculation rules';
COMMENT ON COLUMN commission_scales.manager_split_enabled IS 'Whether to split commission between salesperson and manager';
