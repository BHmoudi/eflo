-- Create order_contract_services table
CREATE TABLE IF NOT EXISTS order_contract_services (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    service_code VARCHAR(50) NOT NULL,
    service_name VARCHAR(255) NOT NULL,
    service_type VARCHAR(100),
    description TEXT,
    price DECIMAL(15, 2) NOT NULL DEFAULT 0,
    cost DECIMAL(15, 2) DEFAULT 0,
    duration_months INT,
    coverage_details TEXT,
    provider VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by_user_id BIGINT NOT NULL,
    CONSTRAINT fk_order_services_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_order_services_order_id ON order_contract_services(order_id);
CREATE INDEX IF NOT EXISTS idx_order_services_code ON order_contract_services(service_code);
CREATE INDEX IF NOT EXISTS idx_order_services_type ON order_contract_services(service_type);

-- Comments
COMMENT ON TABLE order_contract_services IS 'Extended warranties, maintenance contracts, and other services';
COMMENT ON COLUMN order_contract_services.duration_months IS 'Duration of the service contract in months';
