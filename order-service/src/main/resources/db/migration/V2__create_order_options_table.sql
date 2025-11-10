-- Create order_options table
CREATE TABLE IF NOT EXISTS order_options (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    option_code VARCHAR(50) NOT NULL,
    option_name VARCHAR(255) NOT NULL,
    option_category VARCHAR(100),
    description TEXT,
    price DECIMAL(15, 2) NOT NULL DEFAULT 0,
    cost DECIMAL(15, 2) DEFAULT 0,
    is_mandatory BOOLEAN DEFAULT FALSE,
    is_factory_option BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by_user_id BIGINT NOT NULL,
    CONSTRAINT fk_order_options_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_order_options_order_id ON order_options(order_id);
CREATE INDEX IF NOT EXISTS idx_order_options_code ON order_options(option_code);
CREATE INDEX IF NOT EXISTS idx_order_options_category ON order_options(option_category);

-- Comments
COMMENT ON TABLE order_options IS 'Factory and dealer options for orders';
COMMENT ON COLUMN order_options.is_factory_option IS 'TRUE for factory options, FALSE for dealer-installed';
