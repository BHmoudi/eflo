-- ═══════════════════════════════════════════════════════════════════
-- V2: Create Business Units Table
-- Description: Dealerships, service centers, and organizational units
-- ═══════════════════════════════════════════════════════════════════

CREATE TABLE business_units (
    id BIGSERIAL PRIMARY KEY,

    -- Business unit identification
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    legal_name VARCHAR(255),

    -- Type
    type VARCHAR(50) CHECK (type IN (
        'DEALERSHIP', 'SERVICE_CENTER', 'PARTS_DEPOT',
        'REGIONAL_OFFICE', 'HEADQUARTERS'
    )),

    -- Regional organization
    region_code VARCHAR(10),
    region_name VARCHAR(100),
    rrf_code VARCHAR(50),

    -- Contact information
    phone_number VARCHAR(20),
    fax_number VARCHAR(20),
    email VARCHAR(255),
    website VARCHAR(255),

    -- Address
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    postal_code VARCHAR(10),
    city VARCHAR(100),
    country VARCHAR(2) DEFAULT 'FR',

    -- GPS coordinates
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),

    -- Operating hours (JSON)
    opening_hours JSONB,

    -- Manager
    manager_id BIGINT,
    manager_name VARCHAR(255),

    -- Financial
    siret VARCHAR(14),
    vat_number VARCHAR(20),

    -- Brand affiliations
    brands VARCHAR[] DEFAULT '{}',

    -- Status
    is_active BOOLEAN DEFAULT TRUE,
    opening_date DATE,
    closing_date DATE,

    -- Settings (JSON)
    settings JSONB DEFAULT '{}',

    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign keys (added after users table exists)
    CONSTRAINT fk_business_unit_manager FOREIGN KEY (manager_id)
        REFERENCES users(id) ON DELETE SET NULL,

    -- Constraints
    CONSTRAINT chk_bu_country_code CHECK (LENGTH(country) = 2),
    CONSTRAINT chk_latitude CHECK (latitude BETWEEN -90 AND 90),
    CONSTRAINT chk_longitude CHECK (longitude BETWEEN -180 AND 180)
);

-- Comments
COMMENT ON TABLE business_units IS 'Organizational units (dealerships, service centers, etc.)';
COMMENT ON COLUMN business_units.code IS 'Unique business unit code';
COMMENT ON COLUMN business_units.type IS 'Type of business unit';
COMMENT ON COLUMN business_units.opening_hours IS 'JSON object for weekly opening hours';
COMMENT ON COLUMN business_units.brands IS 'Array of brand codes this unit handles';
COMMENT ON COLUMN business_units.settings IS 'JSON object for unit-specific settings';
