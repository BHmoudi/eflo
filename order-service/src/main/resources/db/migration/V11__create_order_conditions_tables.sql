-- Create condition_category table
CREATE TABLE IF NOT EXISTS condition_category (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) UNIQUE NOT NULL,
    label VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create order_condition table
CREATE TABLE IF NOT EXISTS order_condition (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    label VARCHAR(100) NOT NULL,
    description TEXT,
    category_id BIGINT REFERENCES condition_category(id),
    priority INT DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    color VARCHAR(50),
    icon VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create order_condition_rule table
CREATE TABLE IF NOT EXISTS order_condition_rule (
    id BIGSERIAL PRIMARY KEY,
    condition_id BIGINT NOT NULL REFERENCES order_condition(id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    logical_operator VARCHAR(10) DEFAULT 'AND' CHECK (logical_operator IN ('AND', 'OR')),
    priority INT DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create order_condition_criteria table
CREATE TABLE IF NOT EXISTS order_condition_criteria (
    id BIGSERIAL PRIMARY KEY,
    rule_id BIGINT NOT NULL REFERENCES order_condition_rule(id) ON DELETE CASCADE,
    field_path VARCHAR(255) NOT NULL,
    operator VARCHAR(50) NOT NULL CHECK (operator IN (
        'EQUALS', 'NOT_EQUALS', 'IN', 'NOT_IN',
        'GREATER_THAN', 'LESS_THAN', 'GREATER_THAN_OR_EQUAL', 'LESS_THAN_OR_EQUAL',
        'CONTAINS', 'STARTS_WITH', 'ENDS_WITH',
        'EXISTS', 'NOT_EXISTS',
        'COUNT_EQUALS', 'COUNT_GREATER_THAN', 'COUNT_LESS_THAN',
        'IS_NULL', 'IS_NOT_NULL'
    )),
    value JSONB,
    criteria_group INT DEFAULT 1,
    sequence INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create order_assigned_condition table
CREATE TABLE IF NOT EXISTS order_assigned_condition (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    condition_id BIGINT NOT NULL REFERENCES order_condition(id),
    rule_id BIGINT REFERENCES order_condition_rule(id),
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by VARCHAR(100) DEFAULT 'SYSTEM',
    is_manual BOOLEAN DEFAULT false,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(order_id, condition_id)
);

-- Create condition_field_definition table (for UI support)
CREATE TABLE IF NOT EXISTS condition_field_definition (
    id BIGSERIAL PRIMARY KEY,
    field_path VARCHAR(255) UNIQUE NOT NULL,
    field_label VARCHAR(100) NOT NULL,
    field_type VARCHAR(50) NOT NULL CHECK (field_type IN ('STRING', 'NUMBER', 'BOOLEAN', 'ARRAY', 'OBJECT', 'DATE')),
    entity VARCHAR(100) NOT NULL,
    available_operators JSONB NOT NULL,
    value_source VARCHAR(50) DEFAULT 'MANUAL' CHECK (value_source IN ('MANUAL', 'LOOKUP', 'CALCULATED')),
    lookup_table VARCHAR(100),
    lookup_field VARCHAR(100),
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create condition_dependency table
CREATE TABLE IF NOT EXISTS condition_dependency (
    id BIGSERIAL PRIMARY KEY,
    condition_id BIGINT NOT NULL REFERENCES order_condition(id) ON DELETE CASCADE,
    depends_on_condition_id BIGINT NOT NULL REFERENCES order_condition(id) ON DELETE CASCADE,
    dependency_type VARCHAR(50) NOT NULL CHECK (dependency_type IN ('REQUIRES', 'EXCLUDES', 'IMPLIES')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(condition_id, depends_on_condition_id)
);

-- Indexes for condition_category
CREATE INDEX IF NOT EXISTS idx_condition_category_code ON condition_category(code);
CREATE INDEX IF NOT EXISTS idx_condition_category_active ON condition_category(is_active);

-- Indexes for order_condition
CREATE INDEX IF NOT EXISTS idx_order_condition_code ON order_condition(code);
CREATE INDEX IF NOT EXISTS idx_order_condition_category ON order_condition(category_id);
CREATE INDEX IF NOT EXISTS idx_order_condition_active ON order_condition(is_active);
CREATE INDEX IF NOT EXISTS idx_order_condition_priority ON order_condition(priority);

-- Indexes for order_condition_rule
CREATE INDEX IF NOT EXISTS idx_order_condition_rule_condition ON order_condition_rule(condition_id);
CREATE INDEX IF NOT EXISTS idx_order_condition_rule_active ON order_condition_rule(is_active);
CREATE INDEX IF NOT EXISTS idx_order_condition_rule_priority ON order_condition_rule(priority);

-- Indexes for order_condition_criteria
CREATE INDEX IF NOT EXISTS idx_order_condition_criteria_rule ON order_condition_criteria(rule_id);
CREATE INDEX IF NOT EXISTS idx_order_condition_criteria_field ON order_condition_criteria(field_path);
CREATE INDEX IF NOT EXISTS idx_order_condition_criteria_sequence ON order_condition_criteria(sequence);

-- Indexes for order_assigned_condition
CREATE INDEX IF NOT EXISTS idx_order_assigned_condition_order ON order_assigned_condition(order_id);
CREATE INDEX IF NOT EXISTS idx_order_assigned_condition_condition ON order_assigned_condition(condition_id);
CREATE INDEX IF NOT EXISTS idx_order_assigned_condition_rule ON order_assigned_condition(rule_id);
CREATE INDEX IF NOT EXISTS idx_order_assigned_condition_manual ON order_assigned_condition(is_manual);

-- Indexes for condition_field_definition
CREATE INDEX IF NOT EXISTS idx_condition_field_definition_entity ON condition_field_definition(entity);
CREATE INDEX IF NOT EXISTS idx_condition_field_definition_active ON condition_field_definition(is_active);

-- Indexes for condition_dependency
CREATE INDEX IF NOT EXISTS idx_condition_dependency_condition ON condition_dependency(condition_id);
CREATE INDEX IF NOT EXISTS idx_condition_dependency_depends_on ON condition_dependency(depends_on_condition_id);

-- Comments
COMMENT ON TABLE condition_category IS 'Categories for conditions (VN, VO, VU, etc.)';
COMMENT ON TABLE order_condition IS 'Master table for all available conditions';
COMMENT ON TABLE order_condition_rule IS 'Rules that determine when a condition should be applied';
COMMENT ON TABLE order_condition_criteria IS 'Individual criteria within a rule';
COMMENT ON TABLE order_assigned_condition IS 'Links orders to their active conditions';
COMMENT ON TABLE condition_field_definition IS 'Defines available fields for rule building (helps UI)';
COMMENT ON TABLE condition_dependency IS 'Define dependencies between conditions';

COMMENT ON COLUMN order_condition_rule.logical_operator IS 'How to combine criteria: AND or OR';
COMMENT ON COLUMN order_condition_criteria.operator IS 'Comparison operator for the criteria';
COMMENT ON COLUMN order_condition_criteria.value IS 'Flexible JSON value storage (string, number, array)';
COMMENT ON COLUMN order_condition_criteria.criteria_group IS 'For grouping criteria with parentheses';
COMMENT ON COLUMN order_assigned_condition.is_manual IS 'True if manually assigned by user';
COMMENT ON COLUMN condition_dependency.dependency_type IS 'REQUIRES: needs other condition, EXCLUDES: cannot coexist, IMPLIES: automatically adds other condition';
