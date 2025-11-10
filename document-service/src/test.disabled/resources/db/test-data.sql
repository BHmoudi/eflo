-- Test Data SQL Script
-- This script inserts sample data for integration tests

-- Clean up existing test data
DELETE FROM document_access_logs WHERE document_id IN (SELECT id FROM documents WHERE order_number LIKE 'TEST-%');
DELETE FROM document_metadata WHERE document_id IN (SELECT id FROM documents WHERE order_number LIKE 'TEST-%');
DELETE FROM document_validation_rules WHERE document_type_id IN (SELECT id FROM document_types WHERE type_code LIKE 'TEST_%');
DELETE FROM documents WHERE order_number LIKE 'TEST-%';
DELETE FROM document_types WHERE type_code LIKE 'TEST_%';

-- Insert Test Document Types
INSERT INTO document_types (
    id, type_code, type_name, description, category,
    is_mandatory, min_documents, max_documents,
    allowed_formats, max_file_size_mb,
    requires_validation, validator_roles,
    has_expiration, expiration_warning_days, default_validity_days,
    is_active, display_order,
    created_by, created_at
) VALUES
-- Mandatory document type
(1001, 'TEST_INVOICE', 'Test Invoice', 'Test invoice document type', 'ORDER',
 true, 1, 5,
 ARRAY['pdf', 'jpg', 'png'], 10.00,
 true, ARRAY['ROLE_VALIDATOR', 'ROLE_ADMIN'],
 false, 30, NULL,
 true, 1,
 'test-system', NOW()),

-- Optional document type with expiration
(1002, 'TEST_INSURANCE', 'Test Insurance Certificate', 'Test insurance certificate with expiration', 'COMPLIANCE',
 false, 0, 3,
 ARRAY['pdf'], 5.00,
 true, ARRAY['ROLE_VALIDATOR'],
 true, 30, 365,
 true, 2,
 'test-system', NOW()),

-- Simple document type
(1003, 'TEST_RECEIPT', 'Test Receipt', 'Test receipt document', 'ORDER',
 false, 0, 10,
 ARRAY['pdf', 'jpg', 'jpeg', 'png'], 2.00,
 false, ARRAY['ROLE_ADMIN'],
 false, NULL, NULL,
 true, 3,
 'test-system', NOW()),

-- Contract document type
(1004, 'TEST_CONTRACT', 'Test Contract', 'Test contract document with validation', 'CONTRACT',
 true, 1, 1,
 ARRAY['pdf', 'docx'], 20.00,
 true, ARRAY['ROLE_VALIDATOR', 'ROLE_LEGAL'],
 true, 60, 730,
 true, 4,
 'test-system', NOW()),

-- Photo document type
(1005, 'TEST_PHOTO', 'Test Photo', 'Test photo documentation', 'ORDER',
 false, 0, 50,
 ARRAY['jpg', 'jpeg', 'png', 'gif'], 5.00,
 false, NULL,
 false, NULL, NULL,
 true, 5,
 'test-system', NOW());

-- Insert Test Documents
INSERT INTO documents (
    id, document_uuid, document_type_id, type_code,
    order_id, order_number,
    original_filename, stored_filename, file_extension, mime_type, file_size_bytes, file_hash,
    storage_bucket, storage_path, storage_region,
    version, is_latest_version,
    status, virus_scan_status,
    is_confidential, access_level, encryption_enabled,
    business_unit_id, uploaded_by, uploaded_at,
    created_by, created_at
) VALUES
-- Validated document
(2001, gen_random_uuid(), 1001, 'TEST_INVOICE',
 10001, 'TEST-ORD-10001',
 'invoice-001.pdf', 'stored-invoice-001.pdf', 'pdf', 'application/pdf', 102400, md5('test-file-1')::text,
 'test-documents', 'documents/2024/invoice-001.pdf', 'us-east-1',
 1, true,
 'VALIDATED', 'CLEAN',
 false, 'STANDARD', false,
 100, 'test-user-1', NOW() - INTERVAL '2 days',
 'test-user-1', NOW() - INTERVAL '2 days'),

-- Pending document
(2002, gen_random_uuid(), 1001, 'TEST_INVOICE',
 10002, 'TEST-ORD-10002',
 'invoice-002.pdf', 'stored-invoice-002.pdf', 'pdf', 'application/pdf', 204800, md5('test-file-2')::text,
 'test-documents', 'documents/2024/invoice-002.pdf', 'us-east-1',
 1, true,
 'PENDING', 'PENDING',
 false, 'STANDARD', false,
 100, 'test-user-2', NOW() - INTERVAL '1 day',
 'test-user-2', NOW() - INTERVAL '1 day'),

-- Document with expiration
(2003, gen_random_uuid(), 1002, 'TEST_INSURANCE',
 10003, 'TEST-ORD-10003',
 'insurance-001.pdf', 'stored-insurance-001.pdf', 'pdf', 'application/pdf', 512000, md5('test-file-3')::text,
 'test-documents', 'documents/2024/insurance-001.pdf', 'us-east-1',
 1, true,
 'VALIDATED', 'CLEAN',
 false, 'STANDARD', false,
 100, 'test-user-1', NOW() - INTERVAL '30 days',
 'test-user-1', NOW() - INTERVAL '30 days'),

-- Rejected document
(2004, gen_random_uuid(), 1003, 'TEST_RECEIPT',
 10004, 'TEST-ORD-10004',
 'receipt-001.jpg', 'stored-receipt-001.jpg', 'jpg', 'image/jpeg', 153600, md5('test-file-4')::text,
 'test-documents', 'documents/2024/receipt-001.jpg', 'us-east-1',
 1, true,
 'REJECTED', 'CLEAN',
 false, 'STANDARD', false,
 101, 'test-user-3', NOW() - INTERVAL '5 days',
 'test-user-3', NOW() - INTERVAL '5 days'),

-- Confidential document
(2005, gen_random_uuid(), 1004, 'TEST_CONTRACT',
 10005, 'TEST-ORD-10005',
 'contract-001.pdf', 'stored-contract-001.pdf', 'pdf', 'application/pdf', 1024000, md5('test-file-5')::text,
 'test-documents', 'documents/2024/contract-001.pdf', 'us-east-1',
 1, true,
 'VALIDATED', 'CLEAN',
 true, 'CONFIDENTIAL', true,
 102, 'test-user-admin', NOW() - INTERVAL '10 days',
 'test-user-admin', NOW() - INTERVAL '10 days'),

-- Multiple versions - Version 1 (old)
(2006, '550e8400-e29b-41d4-a716-446655440001'::uuid, 1001, 'TEST_INVOICE',
 10006, 'TEST-ORD-10006',
 'invoice-003-v1.pdf', 'stored-invoice-003-v1.pdf', 'pdf', 'application/pdf', 256000, md5('test-file-6')::text,
 'test-documents', 'documents/2024/invoice-003-v1.pdf', 'us-east-1',
 1, false,
 'VALIDATED', 'CLEAN',
 false, 'STANDARD', false,
 100, 'test-user-1', NOW() - INTERVAL '15 days',
 'test-user-1', NOW() - INTERVAL '15 days'),

-- Multiple versions - Version 2 (current)
(2007, '550e8400-e29b-41d4-a716-446655440002'::uuid, 1001, 'TEST_INVOICE',
 10006, 'TEST-ORD-10006',
 'invoice-003-v2.pdf', 'stored-invoice-003-v2.pdf', 'pdf', 'application/pdf', 262144, md5('test-file-7')::text,
 'test-documents', 'documents/2024/invoice-003-v2.pdf', 'us-east-1',
 2, true,
 'VALIDATED', 'CLEAN',
 false, 'STANDARD', false,
 100, 'test-user-1', NOW() - INTERVAL '7 days',
 'test-user-1', NOW() - INTERVAL '7 days'),

-- Document with tags and metadata
(2008, gen_random_uuid(), 1005, 'TEST_PHOTO',
 10007, 'TEST-ORD-10007',
 'photo-001.jpg', 'stored-photo-001.jpg', 'jpg', 'image/jpeg', 307200, md5('test-file-8')::text,
 'test-documents', 'documents/2024/photo-001.jpg', 'us-east-1',
 1, true,
 'VALIDATED', 'CLEAN',
 false, 'STANDARD', false,
 100, 'test-user-2', NOW() - INTERVAL '3 days',
 'test-user-2', NOW() - INTERVAL '3 days');

-- Update document relationships for versions
UPDATE documents SET parent_document_id = 2006, replaced_by_document_id = NULL WHERE id = 2006;
UPDATE documents SET parent_document_id = 2006, replaced_by_document_id = NULL WHERE id = 2007;
UPDATE documents SET replaced_by_document_id = 2007 WHERE id = 2006;

-- Update expiration dates
UPDATE documents SET expiration_date = CURRENT_DATE + INTERVAL '90 days' WHERE id = 2003;

-- Update status reason for rejected document
UPDATE documents SET status_reason = 'Document is illegible', validated_by = 'test-validator', validated_at = NOW() - INTERVAL '4 days' WHERE id = 2004;

-- Update validated documents
UPDATE documents SET validated_by = 'test-validator', validated_at = uploaded_at + INTERVAL '1 day' WHERE status = 'VALIDATED';

-- Add tags to document with metadata
UPDATE documents SET tags = ARRAY['test', 'sample', 'photo'] WHERE id = 2008;

-- Add custom metadata to document
UPDATE documents SET custom_metadata = '{"photographer": "Test User", "location": "Test Site", "weather": "sunny"}'::jsonb WHERE id = 2008;

-- Insert Document Access Logs
INSERT INTO document_access_logs (
    document_id, accessed_by, access_action, access_time,
    ip_address, user_agent, success
) VALUES
(2001, 'test-user-1', 'DOWNLOAD', NOW() - INTERVAL '1 hour', '127.0.0.1', 'Test Browser', true),
(2001, 'test-user-2', 'VIEW', NOW() - INTERVAL '2 hours', '127.0.0.1', 'Test Browser', true),
(2005, 'test-user-admin', 'DOWNLOAD', NOW() - INTERVAL '3 hours', '127.0.0.1', 'Test Browser', true),
(2003, 'test-user-1', 'VIEW', NOW() - INTERVAL '5 hours', '127.0.0.1', 'Test Browser', true);

-- Insert Document Validation Rules
INSERT INTO document_validation_rules (
    document_type_id, rule_type, rule_name, rule_config,
    is_active, execution_order,
    created_by, created_at
) VALUES
(1001, 'FILE_SIZE', 'Max File Size Check', '{"maxSizeMB": 10}'::jsonb, true, 1, 'test-system', NOW()),
(1001, 'FILE_FORMAT', 'Allowed Formats Check', '{"allowedFormats": ["pdf", "jpg", "png"]}'::jsonb, true, 2, 'test-system', NOW()),
(1002, 'FILE_SIZE', 'Max File Size Check', '{"maxSizeMB": 5}'::jsonb, true, 1, 'test-system', NOW()),
(1004, 'CONTENT', 'Contract Validation', '{"requireSignature": true}'::jsonb, true, 3, 'test-system', NOW());

-- Reset sequences to avoid conflicts
SELECT setval('document_types_id_seq', 2000, true);
SELECT setval('documents_id_seq', 3000, true);

-- Verify test data
SELECT 'Document Types:', COUNT(*) FROM document_types WHERE type_code LIKE 'TEST_%';
SELECT 'Documents:', COUNT(*) FROM documents WHERE order_number LIKE 'TEST-%';
SELECT 'Access Logs:', COUNT(*) FROM document_access_logs WHERE document_id IN (SELECT id FROM documents WHERE order_number LIKE 'TEST-%');
SELECT 'Validation Rules:', COUNT(*) FROM document_validation_rules WHERE document_type_id IN (SELECT id FROM document_types WHERE type_code LIKE 'TEST_%');
