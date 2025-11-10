-- ═══════════════════════════════════════════════════════════════════
-- V1: Create Users Table
-- Description: Core user profiles with Keycloak integration
-- ═══════════════════════════════════════════════════════════════════

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,

    -- Keycloak integration
    keycloak_id UUID UNIQUE NOT NULL,
    keycloak_username VARCHAR(255) UNIQUE NOT NULL,

    -- Employee identification
    employee_number VARCHAR(50) UNIQUE NOT NULL,

    -- Personal information
    email VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    full_name VARCHAR(511) GENERATED ALWAYS AS (first_name || ' ' || last_name) STORED,

    -- Contact information
    phone_number VARCHAR(20),
    mobile_number VARCHAR(20),
    office_extension VARCHAR(10),

    -- Professional information
    job_title VARCHAR(255),
    department VARCHAR(100),

    -- Address
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    postal_code VARCHAR(10),
    city VARCHAR(100),
    country VARCHAR(2) DEFAULT 'FR',

    -- Employment
    hire_date DATE,
    termination_date DATE,

    -- Status
    is_active BOOLEAN DEFAULT TRUE,
    is_deleted BOOLEAN DEFAULT FALSE,

    -- Login tracking
    last_login_at TIMESTAMP,
    login_count INT DEFAULT 0,

    -- Profile
    profile_picture_url VARCHAR(500),
    bio TEXT,

    -- Preferences (JSON)
    preferences JSONB DEFAULT '{}',

    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    synced_from_keycloak_at TIMESTAMP,

    -- Constraints
    CONSTRAINT chk_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT chk_country_code CHECK (LENGTH(country) = 2)
);

-- Comments
COMMENT ON TABLE users IS 'User profiles synchronized with Keycloak';
COMMENT ON COLUMN users.keycloak_id IS 'UUID from Keycloak user ID';
COMMENT ON COLUMN users.employee_number IS 'Unique employee identifier';
COMMENT ON COLUMN users.full_name IS 'Generated column: first_name || last_name';
COMMENT ON COLUMN users.preferences IS 'JSON object for user preferences';
