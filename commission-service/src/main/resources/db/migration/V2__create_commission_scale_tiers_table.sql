-- V2__create_commission_scale_tiers_table.sql
-- Commission Scale Tiers: For tiered commission calculations

CREATE TABLE commission_scale_tiers (
    id BIGSERIAL PRIMARY KEY,

    -- Reference to Commission Scale
    commission_scale_id BIGINT NOT NULL,

    -- Tier Configuration
    tier_order INT NOT NULL,
    tier_name VARCHAR(100),
    tier_description VARCHAR(255),

    -- Threshold Range
    threshold_min DECIMAL(12,2) NOT NULL,
    threshold_max DECIMAL(12,2),

    -- Commission Rate for this Tier
    commission_rate_percentage DECIMAL(5,2) NOT NULL,
    fixed_amount DECIMAL(12,2),

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),

    -- Foreign Key
    CONSTRAINT fk_tier_commission_scale FOREIGN KEY (commission_scale_id)
        REFERENCES commission_scales(id) ON DELETE CASCADE,

    -- Constraints
    CONSTRAINT chk_tier_rate CHECK (commission_rate_percentage >= 0 AND commission_rate_percentage <= 100),
    CONSTRAINT chk_tier_threshold CHECK (threshold_max IS NULL OR threshold_max > threshold_min),
    CONSTRAINT uq_scale_tier_order UNIQUE (commission_scale_id, tier_order)
);

-- Indexes
CREATE INDEX idx_scale_tiers_scale_id ON commission_scale_tiers(commission_scale_id);
CREATE INDEX idx_scale_tiers_order ON commission_scale_tiers(commission_scale_id, tier_order);

-- Comments
COMMENT ON TABLE commission_scale_tiers IS 'Tiered commission rates for progressive commission calculations';
COMMENT ON COLUMN commission_scale_tiers.tier_order IS 'Order of the tier (1, 2, 3, etc.)';
COMMENT ON COLUMN commission_scale_tiers.threshold_min IS 'Minimum value for this tier (inclusive)';
COMMENT ON COLUMN commission_scale_tiers.threshold_max IS 'Maximum value for this tier (exclusive), NULL for unlimited';
