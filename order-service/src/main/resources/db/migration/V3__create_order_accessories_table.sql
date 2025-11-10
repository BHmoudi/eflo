-- Create order_accessories table
CREATE TABLE IF NOT EXISTS order_accessories (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    accessory_code VARCHAR(50) NOT NULL,
    accessory_name VARCHAR(255) NOT NULL,
    accessory_category VARCHAR(100),
    description TEXT,
    quantity INT NOT NULL DEFAULT 1 CHECK (quantity > 0),
    unit_price DECIMAL(15, 2) NOT NULL DEFAULT 0,
    total_price DECIMAL(15, 2) NOT NULL DEFAULT 0,
    cost_per_unit DECIMAL(15, 2) DEFAULT 0,
    total_cost DECIMAL(15, 2) DEFAULT 0,
    supplier VARCHAR(255),
    installation_required BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by_user_id BIGINT NOT NULL,
    CONSTRAINT fk_order_accessories_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_order_accessories_order_id ON order_accessories(order_id);
CREATE INDEX IF NOT EXISTS idx_order_accessories_code ON order_accessories(accessory_code);
CREATE INDEX IF NOT EXISTS idx_order_accessories_category ON order_accessories(accessory_category);

-- Comments
COMMENT ON TABLE order_accessories IS 'Aftermarket accessories added to orders';
COMMENT ON COLUMN order_accessories.total_price IS 'Calculated as quantity * unit_price';
