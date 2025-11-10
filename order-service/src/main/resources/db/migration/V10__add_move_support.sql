-- V10__add_move_support.sql
-- Migration to add MOVE/DIAC import support
-- Creates customers, order_commercial_actions, order_trade_ins tables
-- Adds MOVE-specific fields to orders table

-- =====================================================
-- 1. CREATE CUSTOMERS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS customers (
    id BIGSERIAL PRIMARY KEY,

    -- Customer type
    customer_type VARCHAR(10) NOT NULL CHECK (customer_type IN ('PA', 'PRO')),

    -- Personal information (for PA and PRO)
    civility VARCHAR(10),
    first_name VARCHAR(255),
    last_name VARCHAR(255) NOT NULL,

    -- Company information (for PRO only)
    commercial_name VARCHAR(255),
    company_name VARCHAR(255),
    siret VARCHAR(14),

    -- Contact information
    email VARCHAR(255),
    phone_mobile VARCHAR(20),
    phone_landline VARCHAR(20),

    -- Address
    address TEXT,
    postal_code VARCHAR(10),
    city VARCHAR(100),
    country VARCHAR(2) DEFAULT 'FR',

    -- Additional metadata
    sa INT,

    -- External references
    external_customer_id VARCHAR(100),

    -- Status
    is_active BOOLEAN DEFAULT TRUE,

    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT uk_customer_email UNIQUE (email)
);

-- Indexes for customers
CREATE INDEX idx_customers_email ON customers(email);
CREATE INDEX idx_customers_type ON customers(customer_type);
CREATE INDEX idx_customers_last_name ON customers(last_name);
CREATE INDEX idx_customers_external_id ON customers(external_customer_id);

-- =====================================================
-- 2. CREATE ORDER_COMMERCIAL_ACTIONS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS order_commercial_actions (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,

    -- Action details
    action_code VARCHAR(50) NOT NULL,
    action_label VARCHAR(255) NOT NULL,

    -- Amount
    amount_or_percentage DECIMAL(10,2) NOT NULL,
    is_percentage BOOLEAN DEFAULT FALSE,

    -- VAT
    vat_rate DECIMAL(5,2) DEFAULT 20.00,
    vat_type VARCHAR(10),

    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for commercial actions
CREATE INDEX idx_commercial_actions_order ON order_commercial_actions(order_id);
CREATE INDEX idx_commercial_actions_code ON order_commercial_actions(action_code);

-- =====================================================
-- 3. CREATE ORDER_TRADE_INS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS order_trade_ins (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,

    -- Owner information
    owner_name VARCHAR(255),
    owner_customer_id BIGINT,

    -- Vehicle type
    vehicle_type VARCHAR(10) CHECK (vehicle_type IN ('VP', 'VU')),

    -- Vehicle information
    brand VARCHAR(100),
    model VARCHAR(100),
    version VARCHAR(255),
    body_type VARCHAR(50),

    -- Identification
    vin VARCHAR(17),
    registration_number VARCHAR(20),
    registration_date DATE,
    first_registration_date DATE,

    -- Technical details
    fuel_type VARCHAR(50),
    engine_power INT,
    transmission VARCHAR(50),
    mileage INT,
    origin VARCHAR(10),
    number_of_doors INT,

    -- Colors
    exterior_color VARCHAR(100),
    interior_color VARCHAR(100),

    -- Pricing (all in EUR, excl tax)
    estimated_value DECIMAL(12,2) DEFAULT 0.00,
    overestimation DECIMAL(12,2) DEFAULT 0.00,
    final_value DECIMAL(12,2) DEFAULT 0.00,
    engagement_amount DECIMAL(12,2) DEFAULT 0.00,
    conversion_bonus DECIMAL(12,2) DEFAULT 0.00,
    trade_in_balance DECIMAL(12,2) DEFAULT 0.00,

    -- Status
    status VARCHAR(50) DEFAULT 'DRAFT' CHECK (status IN (
        'DRAFT', 'EVALUATED', 'ACCEPTED', 'COMPLETED', 'CANCELLED'
    )),

    -- Evaluation details
    evaluation_date DATE,
    evaluated_by_user_id BIGINT,
    evaluation_notes TEXT,

    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for trade-ins
CREATE INDEX idx_trade_ins_order ON order_trade_ins(order_id);
CREATE INDEX idx_trade_ins_vin ON order_trade_ins(vin);
CREATE INDEX idx_trade_ins_status ON order_trade_ins(status);
CREATE INDEX idx_trade_ins_owner ON order_trade_ins(owner_customer_id);

-- =====================================================
-- 4. ALTER ORDERS TABLE - ADD MOVE-SPECIFIC FIELDS
-- =====================================================

-- MOVE-specific vehicle fields
ALTER TABLE orders ADD COLUMN IF NOT EXISTS semi_clair_model VARCHAR(50);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS semi_clair_version VARCHAR(50);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS co2_level INT;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS body_type VARCHAR(50);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS fuel_type VARCHAR(50);

-- MOVE-specific business metadata
ALTER TABLE orders ADD COLUMN IF NOT EXISTS product_type VARCHAR(10);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS tariff_number INT;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS barcode VARCHAR(50);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS family_barcode VARCHAR(50);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS distrinet_code VARCHAR(50);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS distrinet_export_number VARCHAR(50);

-- Business organization fields
ALTER TABLE orders ADD COLUMN IF NOT EXISTS establishment_name VARCHAR(255);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS identifiant_rr VARCHAR(50);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS rattachement VARCHAR(50);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS seller_type VARCHAR(1);

-- Customer denormalized fields
ALTER TABLE orders ADD COLUMN IF NOT EXISTS customer_first_name VARCHAR(255);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS customer_last_name VARCHAR(255);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS customer_email VARCHAR(255);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS customer_phone VARCHAR(20);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS customer_address TEXT;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS customer_postal_code VARCHAR(10);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS customer_city VARCHAR(100);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS customer_civility VARCHAR(10);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS customer_type VARCHAR(10);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS customer_sa INT;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS customer_commercial_name VARCHAR(255);

-- Salesperson denormalized fields
ALTER TABLE orders ADD COLUMN IF NOT EXISTS salesperson_ipn VARCHAR(50);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS salesperson_name VARCHAR(255);

-- MOVE-specific financing fields
ALTER TABLE orders ADD COLUMN IF NOT EXISTS financing_contract_diac VARCHAR(100);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS financing_with_deposit BOOLEAN;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS financing_number_of_services INT;

-- Aids
ALTER TABLE orders ADD COLUMN IF NOT EXISTS aide_rpe DECIMAL(10,2) DEFAULT 0.00;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS aide_autres DECIMAL(10,2) DEFAULT 0.00;

-- Trade-in flag
ALTER TABLE orders ADD COLUMN IF NOT EXISTS has_trade_in BOOLEAN DEFAULT FALSE;

-- =====================================================
-- 5. ADD COMMENTS FOR DOCUMENTATION
-- =====================================================
COMMENT ON TABLE customers IS 'Customer records for order management';
COMMENT ON TABLE order_commercial_actions IS 'Commercial actions (actionCo) applied to orders';
COMMENT ON TABLE order_trade_ins IS 'Trade-in vehicle information (Reprise VO)';

COMMENT ON COLUMN orders.semi_clair_model IS 'Semi-clair code for vehicle model (MOVE)';
COMMENT ON COLUMN orders.semi_clair_version IS 'Semi-clair code for vehicle version (MOVE)';
COMMENT ON COLUMN orders.distrinet_code IS 'Distrinet code from MOVE system';
COMMENT ON COLUMN orders.financing_contract_diac IS 'DIAC financing contract number';
COMMENT ON COLUMN orders.aide_rpe IS 'RPE aid amount (government subsidy)';
COMMENT ON COLUMN orders.aide_autres IS 'Other aids amount';
