-- Seed Default Document Types
-- Pre-configured document types for common use cases

-- IDENTITY DOCUMENTS
INSERT INTO document_types (
    type_code, type_name, description, category,
    is_mandatory, min_documents, max_documents,
    allowed_formats, max_file_size_mb,
    requires_validation, validator_roles,
    has_expiration, expiration_warning_days, default_validity_days,
    auto_validate_conditions,
    is_active, display_order, created_by
) VALUES
(
    'IDENTITY_CARD',
    'Identity Card',
    'National identity card or passport',
    'IDENTITY',
    true, 1, 2,
    ARRAY['PDF', 'JPG', 'JPEG', 'PNG'], 5.0,
    true, ARRAY['DOCUMENT_MANAGER', 'SALES_MANAGER'],
    true, 30, 1825,
    '{"fileSizeMax": 5242880, "allowedMimeTypes": ["application/pdf", "image/jpeg", "image/png"]}'::jsonb,
    true, 1, 'SYSTEM'
),
(
    'DRIVERS_LICENSE',
    'Driver''s License',
    'Valid driver''s license for vehicle purchase',
    'IDENTITY',
    true, 1, 1,
    ARRAY['PDF', 'JPG', 'JPEG', 'PNG'], 5.0,
    true, ARRAY['DOCUMENT_MANAGER', 'SALES_MANAGER'],
    true, 30, 3650,
    '{"fileSizeMax": 5242880, "requiresValidation": true}'::jsonb,
    true, 2, 'SYSTEM'
);

-- FINANCIAL DOCUMENTS
INSERT INTO document_types (
    type_code, type_name, description, category,
    is_mandatory, min_documents, max_documents,
    allowed_formats, max_file_size_mb,
    requires_validation, validator_roles,
    has_expiration, expiration_warning_days,
    auto_validate_conditions,
    is_active, display_order, created_by
) VALUES
(
    'PROOF_INCOME',
    'Proof of Income',
    'Salary slips or income tax returns',
    'FINANCIAL',
    true, 1, 3,
    ARRAY['PDF'], 10.0,
    true, ARRAY['FINANCIAL_MANAGER', 'DOCUMENT_MANAGER'],
    true, 90,
    '{"fileSizeMax": 10485760, "requiredFields": ["employer", "salary", "date"]}'::jsonb,
    true, 10, 'SYSTEM'
),
(
    'BANK_STATEMENT',
    'Bank Statement',
    'Recent bank account statements',
    'FINANCIAL',
    true, 1, 6,
    ARRAY['PDF'], 10.0,
    true, ARRAY['FINANCIAL_MANAGER', 'DOCUMENT_MANAGER'],
    true, 90,
    '{"fileSizeMax": 10485760, "minMonths": 3}'::jsonb,
    true, 11, 'SYSTEM'
),
(
    'TAX_RETURN',
    'Tax Return',
    'Annual tax return documentation',
    'FINANCIAL',
    false, 0, 3,
    ARRAY['PDF'], 15.0,
    true, ARRAY['FINANCIAL_MANAGER'],
    false, null,
    '{"fileSizeMax": 15728640}'::jsonb,
    true, 12, 'SYSTEM'
);

-- LEGAL DOCUMENTS
INSERT INTO document_types (
    type_code, type_name, description, category,
    is_mandatory, min_documents, max_documents,
    allowed_formats, max_file_size_mb,
    requires_validation, validator_roles,
    has_expiration, expiration_warning_days,
    auto_validate_conditions,
    is_active, display_order, created_by
) VALUES
(
    'PURCHASE_CONTRACT',
    'Purchase Contract',
    'Signed vehicle purchase contract',
    'CONTRACT',
    true, 1, 1,
    ARRAY['PDF'], 20.0,
    true, ARRAY['LEGAL_MANAGER', 'CONTRACT_MANAGER'],
    false, null,
    '{"digitalSignature": true, "requiredClauses": ["payment", "delivery", "warranty"]}'::jsonb,
    true, 20, 'SYSTEM'
),
(
    'INSURANCE_CERTIFICATE',
    'Insurance Certificate',
    'Vehicle insurance certificate',
    'LEGAL',
    true, 1, 1,
    ARRAY['PDF'], 10.0,
    true, ARRAY['DOCUMENT_MANAGER'],
    true, 30,
    '{"fileSizeMax": 10485760}'::jsonb,
    true, 21, 'SYSTEM'
),
(
    'POWER_OF_ATTORNEY',
    'Power of Attorney',
    'Legal authorization for representation',
    'LEGAL',
    false, 0, 1,
    ARRAY['PDF'], 10.0,
    true, ARRAY['LEGAL_MANAGER'],
    true, 180,
    '{"notarized": true}'::jsonb,
    true, 22, 'SYSTEM'
);

-- TECHNICAL DOCUMENTS
INSERT INTO document_types (
    type_code, type_name, description, category,
    is_mandatory, min_documents, max_documents,
    allowed_formats, max_file_size_mb,
    requires_validation, validator_roles,
    has_expiration, expiration_warning_days,
    auto_validate_conditions,
    is_active, display_order, created_by
) VALUES
(
    'VEHICLE_REGISTRATION',
    'Vehicle Registration',
    'Official vehicle registration document',
    'TECHNICAL',
    true, 1, 1,
    ARRAY['PDF', 'JPG', 'JPEG', 'PNG'], 10.0,
    true, ARRAY['TECHNICAL_MANAGER', 'DOCUMENT_MANAGER'],
    false, null,
    '{"fileSizeMax": 10485760}'::jsonb,
    true, 30, 'SYSTEM'
),
(
    'INSPECTION_REPORT',
    'Technical Inspection Report',
    'Vehicle technical inspection and certification',
    'TECHNICAL',
    true, 1, 1,
    ARRAY['PDF'], 15.0,
    true, ARRAY['TECHNICAL_MANAGER'],
    true, 180,
    '{"fileSizeMax": 15728640}'::jsonb,
    true, 31, 'SYSTEM'
),
(
    'MAINTENANCE_RECORD',
    'Maintenance Records',
    'Vehicle service and maintenance history',
    'TECHNICAL',
    false, 0, 10,
    ARRAY['PDF', 'JPG', 'JPEG'], 10.0,
    false, ARRAY[]::text[],
    false, null,
    '{"fileSizeMax": 10485760}'::jsonb,
    true, 32, 'SYSTEM'
);

-- ADMINISTRATIVE DOCUMENTS
INSERT INTO document_types (
    type_code, type_name, description, category,
    is_mandatory, min_documents, max_documents,
    allowed_formats, max_file_size_mb,
    requires_validation, validator_roles,
    has_expiration, expiration_warning_days,
    auto_validate_conditions,
    is_active, display_order, created_by
) VALUES
(
    'PROOF_ADDRESS',
    'Proof of Address',
    'Utility bill or residence certificate',
    'ADMINISTRATIVE',
    true, 1, 2,
    ARRAY['PDF', 'JPG', 'JPEG', 'PNG'], 5.0,
    true, ARRAY['DOCUMENT_MANAGER'],
    true, 90,
    '{"fileSizeMax": 5242880, "maxAgeMonths": 3}'::jsonb,
    true, 40, 'SYSTEM'
),
(
    'EMPLOYMENT_LETTER',
    'Employment Letter',
    'Letter from employer confirming employment',
    'ADMINISTRATIVE',
    false, 0, 1,
    ARRAY['PDF'], 5.0,
    true, ARRAY['HR_MANAGER', 'DOCUMENT_MANAGER'],
    true, 180,
    '{"fileSizeMax": 5242880}'::jsonb,
    true, 41, 'SYSTEM'
),
(
    'TRADE_REGISTRY',
    'Trade Registry Extract',
    'Official trade registry document for businesses',
    'ADMINISTRATIVE',
    false, 0, 1,
    ARRAY['PDF'], 10.0,
    true, ARRAY['LEGAL_MANAGER', 'DOCUMENT_MANAGER'],
    true, 90,
    '{"fileSizeMax": 10485760}'::jsonb,
    true, 42, 'SYSTEM'
);

-- OTHER DOCUMENTS
INSERT INTO document_types (
    type_code, type_name, description, category,
    is_mandatory, min_documents, max_documents,
    allowed_formats, max_file_size_mb,
    requires_validation, validator_roles,
    has_expiration, expiration_warning_days,
    auto_validate_conditions,
    is_active, display_order, created_by
) VALUES
(
    'CUSTOM_DOCUMENT',
    'Custom Document',
    'Additional custom documentation',
    'OTHER',
    false, 0, 20,
    ARRAY['PDF', 'JPG', 'JPEG', 'PNG', 'DOC', 'DOCX', 'XLS', 'XLSX'], 50.0,
    false, ARRAY[]::text[],
    false, null,
    '{"fileSizeMax": 52428800}'::jsonb,
    true, 90, 'SYSTEM'
),
(
    'PHOTO',
    'Photograph',
    'General purpose photograph',
    'OTHER',
    false, 0, 50,
    ARRAY['JPG', 'JPEG', 'PNG'], 10.0,
    false, ARRAY[]::text[],
    false, null,
    '{"fileSizeMax": 10485760}'::jsonb,
    true, 91, 'SYSTEM'
);

-- Comments
COMMENT ON COLUMN document_types.type_code IS 'Standard document type codes for the Eflo platform';
