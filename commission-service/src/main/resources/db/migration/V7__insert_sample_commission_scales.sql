-- V7__insert_sample_commission_scales.sql
-- Sample commission scales for testing and initial setup

-- VN Standard 10% Scale
INSERT INTO commission_scales (
    scale_code, scale_name, scale_description,
    business_unit_id, order_type,
    commission_type, calculation_method,
    commission_rate_percentage,
    minimum_margin_required, minimum_revenue_required,
    manager_split_enabled, manager_split_percentage,
    valid_from, valid_to,
    is_active, is_default,
    created_by
) VALUES (
    'VN_STANDARD_10PCT',
    'New Vehicle Standard 10%',
    'Standard 10% commission on net margin for new vehicles',
    1, -- Business Unit ID
    'VN',
    'VEHICLE',
    'PERCENTAGE_MARGIN',
    10.00,
    1000.00, -- Minimum 1000 EUR margin
    15000.00, -- Minimum 15000 EUR revenue
    true,
    20.00, -- 20% to manager
    '2024-01-01',
    '2024-12-31',
    true,
    true,
    'SYSTEM'
);

-- VN Tiered Premium Scale
INSERT INTO commission_scales (
    scale_code, scale_name, scale_description,
    business_unit_id, order_type,
    commission_type, calculation_method,
    minimum_margin_required,
    manager_split_enabled, manager_split_percentage,
    valid_from, valid_to,
    is_active, is_default,
    created_by
) VALUES (
    'VN_TIERED_PREMIUM',
    'New Vehicle Tiered Premium',
    'Progressive tiered commission for new vehicles',
    1,
    'VN',
    'VEHICLE',
    'TIERED',
    500.00,
    true,
    25.00,
    '2024-01-01',
    '2024-12-31',
    true,
    false,
    'SYSTEM'
);

-- Tiers for VN_TIERED_PREMIUM
INSERT INTO commission_scale_tiers (
    commission_scale_id, tier_order, tier_description,
    threshold_min, threshold_max, commission_rate_percentage,
    created_by
) VALUES
    ((SELECT id FROM commission_scales WHERE scale_code = 'VN_TIERED_PREMIUM'), 1, 'Basic tier', 0.00, 2000.00, 5.00, 'SYSTEM'),
    ((SELECT id FROM commission_scales WHERE scale_code = 'VN_TIERED_PREMIUM'), 2, 'Standard tier', 2000.01, 5000.00, 8.00, 'SYSTEM'),
    ((SELECT id FROM commission_scales WHERE scale_code = 'VN_TIERED_PREMIUM'), 3, 'Premium tier', 5000.01, NULL, 12.00, 'SYSTEM');

-- VO Fixed Amount Scale
INSERT INTO commission_scales (
    scale_code, scale_name, scale_description,
    business_unit_id, order_type,
    commission_type, calculation_method,
    fixed_amount,
    minimum_margin_required, minimum_revenue_required,
    manager_split_enabled,
    valid_from, valid_to,
    is_active, is_default,
    created_by
) VALUES (
    'VO_FIXED_150',
    'Used Vehicle Fixed 150 EUR',
    'Fixed 150 EUR commission per used vehicle sale',
    1,
    'VO',
    'VEHICLE',
    'FIXED_AMOUNT',
    150.00,
    800.00,
    10000.00,
    false,
    '2024-01-01',
    '2024-12-31',
    true,
    true,
    'SYSTEM'
);

-- EVO Hybrid Scale
INSERT INTO commission_scales (
    scale_code, scale_name, scale_description,
    business_unit_id, order_type,
    commission_type, calculation_method,
    minimum_margin_required,
    manager_split_enabled, manager_split_percentage,
    configuration_json,
    valid_from, valid_to,
    is_active, is_default,
    created_by
) VALUES (
    'EVO_HYBRID',
    'Evolution Hybrid Scale',
    'Combined calculation for vehicle, accessories, and services',
    1,
    'EVO',
    'COMBINED',
    'HYBRID',
    1200.00,
    true,
    30.00,
    '{
        "vehicleCommission": {
            "method": "PERCENTAGE_MARGIN",
            "rate": 8.00
        },
        "accessoryCommission": {
            "method": "FIXED_AMOUNT",
            "amount": 50.00
        },
        "serviceCommission": {
            "method": "PERCENTAGE_MARGIN",
            "rate": 15.00
        }
    }'::jsonb,
    '2024-01-01',
    '2024-12-31',
    true,
    true,
    'SYSTEM'
);

-- Accessory Scale
INSERT INTO commission_scales (
    scale_code, scale_name, scale_description,
    business_unit_id, order_type,
    commission_type, calculation_method,
    commission_rate_percentage,
    minimum_margin_required,
    manager_split_enabled,
    valid_from, valid_to,
    is_active, is_default,
    created_by
) VALUES (
    'ACC_STANDARD_15PCT',
    'Accessory Standard 15%',
    'Standard 15% commission on accessory margin',
    1,
    'VN',
    'ACCESSORY',
    'PERCENTAGE_MARGIN',
    15.00,
    100.00,
    false,
    '2024-01-01',
    '2024-12-31',
    true,
    true,
    'SYSTEM'
);

-- Service Scale
INSERT INTO commission_scales (
    scale_code, scale_name, scale_description,
    business_unit_id, order_type,
    commission_type, calculation_method,
    commission_rate_percentage,
    minimum_margin_required,
    manager_split_enabled, manager_split_percentage,
    valid_from, valid_to,
    is_active, is_default,
    created_by
) VALUES (
    'SVC_STANDARD_20PCT',
    'Service Standard 20%',
    'Standard 20% commission on service margin',
    1,
    'VN',
    'SERVICE',
    'PERCENTAGE_MARGIN',
    20.00,
    50.00,
    true,
    10.00,
    '2024-01-01',
    '2024-12-31',
    true,
    true,
    'SYSTEM'
);
