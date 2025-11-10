-- Create orders table
CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    order_type VARCHAR(10) NOT NULL CHECK (order_type IN ('VN', 'VO', 'EVO')),

    -- Customer & Business Info
    customer_id BIGINT NOT NULL,
    business_unit_id BIGINT NOT NULL,
    salesperson_id BIGINT NOT NULL,

    -- Vehicle Info
    vehicle_id BIGINT,
    vin VARCHAR(17),
    make VARCHAR(100),
    model VARCHAR(100),
    year INT,
    trim VARCHAR(100),
    color_exterior VARCHAR(50),
    color_interior VARCHAR(50),

    -- Pricing Fields
    base_price DECIMAL(15, 2) NOT NULL DEFAULT 0,
    options_total DECIMAL(15, 2) DEFAULT 0,
    accessories_total DECIMAL(15, 2) DEFAULT 0,
    services_total DECIMAL(15, 2) DEFAULT 0,
    aids_total DECIMAL(15, 2) DEFAULT 0,
    supplements_total DECIMAL(15, 2) DEFAULT 0,

    subtotal DECIMAL(15, 2) DEFAULT 0,
    discount_amount DECIMAL(15, 2) DEFAULT 0,
    discount_percentage DECIMAL(5, 2) DEFAULT 0,
    total_before_tax DECIMAL(15, 2) DEFAULT 0,

    vat_rate DECIMAL(5, 2) DEFAULT 20.00,
    vat_amount DECIMAL(15, 2) DEFAULT 0,
    total_amount DECIMAL(15, 2) DEFAULT 0,

    -- Margin Fields
    cost_price DECIMAL(15, 2) DEFAULT 0,
    gross_margin DECIMAL(15, 2) DEFAULT 0,
    net_margin DECIMAL(15, 2) DEFAULT 0,
    margin_percentage DECIMAL(5, 2) DEFAULT 0,

    -- Trade-in
    tradein_vehicle_id BIGINT,
    tradein_value DECIMAL(15, 2) DEFAULT 0,

    -- Financing (metadata only)
    financing_type VARCHAR(50),
    financing_institution VARCHAR(100),
    financing_amount DECIMAL(15, 2),
    financing_term_months INT,
    financing_interest_rate DECIMAL(5, 2),

    -- Workflow
    workflow_instance_id BIGINT,
    workflow_current_state VARCHAR(100),

    -- Status & Lifecycle
    status VARCHAR(50) DEFAULT 'DRAFT' CHECK (status IN (
        'DRAFT', 'PENDING', 'CONFIRMED', 'IN_PRODUCTION',
        'READY_FOR_DELIVERY', 'DELIVERED', 'INVOICED',
        'CANCELLED', 'ON_HOLD'
    )),

    -- Delivery
    expected_delivery_date DATE,
    actual_delivery_date DATE,
    delivery_location VARCHAR(255),
    delivery_notes TEXT,

    -- Notes & Comments
    notes TEXT,
    internal_comments TEXT,

    -- Audit Fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by_user_id BIGINT NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by_user_id BIGINT,
    deleted_at TIMESTAMP,
    deleted_by_user_id BIGINT,
    version INT DEFAULT 0
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_orders_number ON orders(order_number);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_type ON orders(order_type);
CREATE INDEX IF NOT EXISTS idx_orders_customer ON orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_orders_salesperson ON orders(salesperson_id);
CREATE INDEX IF NOT EXISTS idx_orders_business_unit ON orders(business_unit_id);
CREATE INDEX IF NOT EXISTS idx_orders_vin ON orders(vin);
CREATE INDEX IF NOT EXISTS idx_orders_workflow ON orders(workflow_instance_id);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at);
CREATE INDEX IF NOT EXISTS idx_orders_expected_delivery ON orders(expected_delivery_date);

-- Comments
COMMENT ON TABLE orders IS 'Main orders table for VN (New), VO (Used), and EVO (Evolution) vehicle orders';
COMMENT ON COLUMN orders.order_type IS 'VN=New Vehicle, VO=Used Vehicle, EVO=Evolution';
COMMENT ON COLUMN orders.margin_percentage IS 'Calculated as (net_margin / total_amount) * 100';
