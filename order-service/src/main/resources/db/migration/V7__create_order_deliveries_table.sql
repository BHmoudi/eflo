-- Create order_deliveries table
CREATE TABLE IF NOT EXISTS order_deliveries (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    delivery_type VARCHAR(50) NOT NULL CHECK (delivery_type IN ('PICKUP', 'HOME_DELIVERY', 'DEALER_DELIVERY')),
    scheduled_date DATE NOT NULL,
    scheduled_time_start TIME,
    scheduled_time_end TIME,
    actual_delivery_date TIMESTAMP,
    delivery_address_line1 VARCHAR(255),
    delivery_address_line2 VARCHAR(255),
    delivery_city VARCHAR(100),
    delivery_state VARCHAR(100),
    delivery_postal_code VARCHAR(20),
    delivery_country VARCHAR(100),
    delivery_contact_name VARCHAR(255),
    delivery_contact_phone VARCHAR(50),
    delivery_contact_email VARCHAR(255),
    delivery_instructions TEXT,
    delivery_status VARCHAR(50) DEFAULT 'SCHEDULED' CHECK (delivery_status IN (
        'SCHEDULED', 'CONFIRMED', 'IN_TRANSIT', 'DELIVERED', 'FAILED', 'CANCELLED'
    )),
    delivered_by_user_id BIGINT,
    delivery_notes TEXT,
    signature_captured BOOLEAN DEFAULT FALSE,
    signature_data TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by_user_id BIGINT NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_deliveries_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_order_deliveries_order_id ON order_deliveries(order_id);
CREATE INDEX IF NOT EXISTS idx_order_deliveries_status ON order_deliveries(delivery_status);
CREATE INDEX IF NOT EXISTS idx_order_deliveries_scheduled_date ON order_deliveries(scheduled_date);
CREATE INDEX IF NOT EXISTS idx_order_deliveries_type ON order_deliveries(delivery_type);

-- Comments
COMMENT ON TABLE order_deliveries IS 'Delivery tracking and scheduling for orders';
COMMENT ON COLUMN order_deliveries.signature_data IS 'Base64 encoded signature image or reference';
