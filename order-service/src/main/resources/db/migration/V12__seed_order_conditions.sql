-- Insert condition categories
INSERT INTO condition_category (code, label, description, is_active) VALUES
('VN', 'Véhicule Neuf', 'Conditions for new vehicle orders', true),
('VO', 'Véhicule Occasion', 'Conditions for used vehicle orders', true),
('VU', 'Véhicule Utilitaire', 'Conditions for utility vehicle orders', true);

-- Insert VN conditions
INSERT INTO order_condition (code, label, description, category_id, priority, is_active, color) VALUES
-- VN Basic Conditions
('REPRISE_VO_VN', 'REPRISE VO', 'Trade-in Used Vehicle', (SELECT id FROM condition_category WHERE code = 'VN'), 1, true, '#4CAF50'),
('DIAC_VN', 'DIAC', 'DIAC Financing', (SELECT id FROM condition_category WHERE code = 'VN'), 2, true, '#2196F3'),
('PARTICULIER_VN', 'PARTICULIER', 'Individual Customer', (SELECT id FROM condition_category WHERE code = 'VN'), 3, true, '#FF9800'),
('ENTREPRISE_VN', 'ENTREPRISE', 'Business Customer', (SELECT id FROM condition_category WHERE code = 'VN'), 4, true, '#9C27B0'),
('ACCESSOIRES_VN', 'ACCESSOIRES', 'Accessories', (SELECT id FROM condition_category WHERE code = 'VN'), 5, true, '#00BCD4'),
('CONTRAT_SERVICE_VN', 'Entretien', 'Service Contract / Maintenance', (SELECT id FROM condition_category WHERE code = 'VN'), 6, true, '#8BC34A'),
('VD_AGENT_VN', 'VD AGENT', 'Agent Seller', (SELECT id FROM condition_category WHERE code = 'VN'), 7, true, '#FFC107'),
('INTERMEDIAIRE_VN', 'INTERMEDIAIRE', 'Intermediary', (SELECT id FROM condition_category WHERE code = 'VN'), 8, true, '#795548'),
('LOUEUR_VN', 'LOUEUR', 'Renter', (SELECT id FROM condition_category WHERE code = 'VN'), 9, true, '#607D8B'),
('TRANSPORT_VN', 'TRANSPORT', 'Transport Charges', (SELECT id FROM condition_category WHERE code = 'VN'), 10, true, '#9E9E9E'),
('SEC_GRAV_VN', 'SEC GRAV', 'Security Engraving', (SELECT id FROM condition_category WHERE code = 'VN'), 11, true, '#FF5722'),
('DIAC_LOC_VN', 'DIAC LOC', 'DIAC Leasing', (SELECT id FROM condition_category WHERE code = 'VN'), 12, true, '#3F51B5'),
('ER_VN', 'E/R', 'Engagement/Remuneration', (SELECT id FROM condition_category WHERE code = 'VN'), 13, true, '#E91E63'),
('DACIA_VN', 'DACIA', 'Dacia Brand', (SELECT id FROM condition_category WHERE code = 'VN'), 14, true, '#673AB7'),
('RENAULT_VN', 'RENAULT', 'Renault Brand', (SELECT id FROM condition_category WHERE code = 'VN'), 15, true, '#FFEB3B'),
('ALPINE_VN', 'ALPINE', 'Alpine Brand', (SELECT id FROM condition_category WHERE code = 'VN'), 16, true, '#00BCD4'),
('ZE_VN', 'ZE', 'Zero Emission (Electric)', (SELECT id FROM condition_category WHERE code = 'VN'), 17, true, '#4CAF50'),
('HE_VN', 'HE', 'Hybrid Electric', (SELECT id FROM condition_category WHERE code = 'VN'), 18, true, '#8BC34A'),
('VS_VN', 'VS', 'Véhicule de Sortie', (SELECT id FROM condition_category WHERE code = 'VN'), 19, true, '#FF9800'),
('REPVOPRIMCONV_VN', 'REPRISE VO PRIME CONV', 'Trade-in with Conversion Prime', (SELECT id FROM condition_category WHERE code = 'VN'), 20, true, '#9C27B0'),
('BONUS_VN', 'BONUS', 'Super Bonus', (SELECT id FROM condition_category WHERE code = 'VN'), 21, true, '#FFEB3B'),
('AUTO_SEPHERE_VN', 'AUTO SEPHERE VN', 'CGI Financier', (SELECT id FROM condition_category WHERE code = 'VN'), 22, true, '#795548'),
('CEE_VN', 'CEE', 'Certificats d''Économies d''Énergie', (SELECT id FROM condition_category WHERE code = 'VN'), 23, true, '#009688');

-- Insert VO conditions
INSERT INTO order_condition (code, label, description, category_id, priority, is_active, color) VALUES
('REPRISE_VO', 'REPRISE VO', 'Trade-in Used Vehicle', (SELECT id FROM condition_category WHERE code = 'VO'), 1, true, '#4CAF50'),
('DIAC_VO', 'DIAC VO', 'DIAC Financing for Used Vehicles', (SELECT id FROM condition_category WHERE code = 'VO'), 2, true, '#2196F3'),
('PARTICULIER_VO', 'PARTICULIER VO', 'Individual Customer', (SELECT id FROM condition_category WHERE code = 'VO'), 3, true, '#FF9800'),
('ENTREPRISE_VO', 'ENTREPRISE VO', 'Business Customer', (SELECT id FROM condition_category WHERE code = 'VO'), 4, true, '#9C27B0'),
('ZE_VO', 'ZE VO', 'Zero Emission (Electric)', (SELECT id FROM condition_category WHERE code = 'VO'), 5, true, '#4CAF50'),
('HE_VO', 'HE VO', 'Hybrid Electric', (SELECT id FROM condition_category WHERE code = 'VO'), 6, true, '#8BC34A'),
('EN_VO', 'EN VO', 'Entretien (Maintenance)', (SELECT id FROM condition_category WHERE code = 'VO'), 7, true, '#8BC34A'),
('EX_VO', 'Extension de Gtie', 'Warranty Extension', (SELECT id FROM condition_category WHERE code = 'VO'), 8, true, '#00BCD4'),
('LOUEUR_VO', 'LOUEUR VO', 'Renter', (SELECT id FROM condition_category WHERE code = 'VO'), 9, true, '#607D8B'),
('GRAVAGE_VO', 'GRAVAGE VO', 'Security Engraving', (SELECT id FROM condition_category WHERE code = 'VO'), 10, true, '#FF5722'),
('ER_VO', 'E/R VO', 'Engagement/Remuneration', (SELECT id FROM condition_category WHERE code = 'VO'), 11, true, '#E91E63'),
('SOLOVI_VO', 'SOLOVI', 'Specific Origin Stock', (SELECT id FROM condition_category WHERE code = 'VO'), 12, true, '#795548'),
('AUTO_SEPHERE_VO', 'AUTO SEPHERE VO', 'CGI Financier', (SELECT id FROM condition_category WHERE code = 'VO'), 13, true, '#795548'),
('REPVOPRIMCONV_VO', 'REPVOPRIMCONV VO', 'Trade-in with Conversion Prime', (SELECT id FROM condition_category WHERE code = 'VO'), 14, true, '#9C27B0'),
('BONUS_VO', 'BONUS VO', 'Super Bonus', (SELECT id FROM condition_category WHERE code = 'VO'), 15, true, '#FFEB3B');

-- VN Rules and Criteria

-- 1. REPRISE VO VN
INSERT INTO order_condition_rule (condition_id, name, description, logical_operator, priority, is_active)
VALUES (
    (SELECT id FROM order_condition WHERE code = 'REPRISE_VO_VN'),
    'Trade-in value exists',
    'Activated when there is a trade-in value for a used vehicle',
    'AND', 0, true
);

INSERT INTO order_condition_criteria (rule_id, field_path, operator, value, sequence)
VALUES (
    (SELECT id FROM order_condition_rule WHERE name = 'Trade-in value exists'),
    'order.tradeinValue', 'GREATER_THAN', '0', 0
);

-- 2. DIAC VN
INSERT INTO order_condition_rule (condition_id, name, description, logical_operator, priority, is_active)
VALUES (
    (SELECT id FROM order_condition WHERE code = 'DIAC_VN'),
    'DIAC Product Type Rule',
    'Activated for specific DIAC product types',
    'AND', 0, true
);

INSERT INTO order_condition_criteria (rule_id, field_path, operator, value, sequence)
VALUES (
    (SELECT id FROM order_condition_rule WHERE name = 'DIAC Product Type Rule'),
    'order.productType', 'IN', '["ND1", "ND2", "ND3", "CB", "CBE", "CD", "CC", "LPC", "LPD"]', 0
);

-- 3. PARTICULIER VN
INSERT INTO order_condition_rule (condition_id, name, description, logical_operator, priority, is_active)
VALUES (
    (SELECT id FROM order_condition WHERE code = 'PARTICULIER_VN'),
    'Individual Client Rule',
    'Activated when client type is Particulier',
    'AND', 0, true
);

INSERT INTO order_condition_criteria (rule_id, field_path, operator, value, sequence)
VALUES (
    (SELECT id FROM order_condition_rule WHERE name = 'Individual Client Rule'),
    'order.customerType', 'EQUALS', '"PA"', 0
);

-- 4. ENTREPRISE VN
INSERT INTO order_condition_rule (condition_id, name, description, logical_operator, priority, is_active)
VALUES (
    (SELECT id FROM order_condition WHERE code = 'ENTREPRISE_VN'),
    'Business Client Rule',
    'Activated when client type is not Particulier',
    'AND', 0, true
);

INSERT INTO order_condition_criteria (rule_id, field_path, operator, value, sequence)
VALUES (
    (SELECT id FROM order_condition_rule WHERE name = 'Business Client Rule'),
    'order.customerType', 'NOT_EQUALS', '"PA"', 0
);

-- 5. ACCESSOIRES VN
INSERT INTO order_condition_rule (condition_id, name, description, logical_operator, priority, is_active)
VALUES (
    (SELECT id FROM order_condition WHERE code = 'ACCESSOIRES_VN'),
    'Accessories Amount Rule',
    'Activated when accessories amount > 0',
    'OR', 0, true
);

INSERT INTO order_condition_criteria (rule_id, field_path, operator, value, sequence, criteria_group)
VALUES
    ((SELECT id FROM order_condition_rule WHERE name = 'Accessories Amount Rule'),
     'order.accessoriesTotal', 'GREATER_THAN', '0', 0, 1),
    ((SELECT id FROM order_condition_rule WHERE name = 'Accessories Amount Rule'),
     'order.accessories', 'EXISTS', 'null', 1, 2);

-- 6. CONTRAT SERVICE VN (Entretien)
INSERT INTO order_condition_rule (condition_id, name, description, logical_operator, priority, is_active)
VALUES (
    (SELECT id FROM order_condition WHERE code = 'CONTRAT_SERVICE_VN'),
    'Service Contract Rule',
    'Activated when service contracts exist or product type is DLA/DLO',
    'OR', 0, true
);

INSERT INTO order_condition_criteria (rule_id, field_path, operator, value, sequence, criteria_group)
VALUES
    ((SELECT id FROM order_condition_rule WHERE name = 'Service Contract Rule'),
     'order.contractServices', 'EXISTS', 'null', 0, 1),
    ((SELECT id FROM order_condition_rule WHERE name = 'Service Contract Rule'),
     'order.productType', 'IN', '["DLA", "DLO"]', 1, 2);

-- 7. LOUEUR VN
INSERT INTO order_condition_rule (condition_id, name, description, logical_operator, priority, is_active)
VALUES (
    (SELECT id FROM order_condition WHERE code = 'LOUEUR_VN'),
    'Renter Rule',
    'Activated when there is a renter (loueur ID != 1)',
    'AND', 0, true
);
-- Note: This would need a custom field for loueur ID

-- 8. TRANSPORT VN
INSERT INTO order_condition_rule (condition_id, name, description, logical_operator, priority, is_active)
VALUES (
    (SELECT id FROM order_condition WHERE code = 'TRANSPORT_VN'),
    'Transport Amount Rule',
    'Activated when transport amount > 0',
    'AND', 0, true
);
-- Note: This would need a custom field for transport amount

-- 9. SEC GRAV VN
INSERT INTO order_condition_rule (condition_id, name, description, logical_operator, priority, is_active)
VALUES (
    (SELECT id FROM order_condition WHERE code = 'SEC_GRAV_VN'),
    'Engraving Amount Rule',
    'Activated when engraving amount > 0',
    'AND', 0, true
);
-- Note: This would need a custom field for engraving amount

-- 10. DIAC LOC VN
INSERT INTO order_condition_rule (condition_id, name, description, logical_operator, priority, is_active)
VALUES (
    (SELECT id FROM order_condition WHERE code = 'DIAC_LOC_VN'),
    'DIAC Leasing Product Type Rule',
    'Activated for AE, DLA, DLO product types',
    'AND', 0, true
);

INSERT INTO order_condition_criteria (rule_id, field_path, operator, value, sequence)
VALUES (
    (SELECT id FROM order_condition_rule WHERE name = 'DIAC Leasing Product Type Rule'),
    'order.productType', 'IN', '["AE", "DLA", "DLO"]', 0
);

-- 11. E/R VN
INSERT INTO order_condition_rule (condition_id, name, description, logical_operator, priority, is_active)
VALUES (
    (SELECT id FROM order_condition WHERE code = 'ER_VN'),
    'E/R Product Type Rule',
    'Activated for ND1, ND2, ND3, DLA, CBE product types',
    'AND', 0, true
);

INSERT INTO order_condition_criteria (rule_id, field_path, operator, value, sequence)
VALUES (
    (SELECT id FROM order_condition_rule WHERE name = 'E/R Product Type Rule'),
    'order.productType', 'IN', '["ND1", "ND2", "ND3", "DLA", "CBE"]', 0
);

-- 12. DACIA VN
INSERT INTO order_condition_rule (condition_id, name, description, logical_operator, priority, is_active)
VALUES (
    (SELECT id FROM order_condition WHERE code = 'DACIA_VN'),
    'Dacia Brand Rule',
    'Activated when brand is DACIA',
    'AND', 0, true
);

INSERT INTO order_condition_criteria (rule_id, field_path, operator, value, sequence)
VALUES (
    (SELECT id FROM order_condition_rule WHERE name = 'Dacia Brand Rule'),
    'order.make', 'EQUALS', '"DACIA"', 0
);

-- 13. RENAULT VN
INSERT INTO order_condition_rule (condition_id, name, description, logical_operator, priority, is_active)
VALUES (
    (SELECT id FROM order_condition WHERE code = 'RENAULT_VN'),
    'Renault Brand Rule',
    'Activated when brand is RENAULT',
    'AND', 0, true
);

INSERT INTO order_condition_criteria (rule_id, field_path, operator, value, sequence)
VALUES (
    (SELECT id FROM order_condition_rule WHERE name = 'Renault Brand Rule'),
    'order.make', 'EQUALS', '"RENAULT"', 0
);

-- 14. ALPINE VN
INSERT INTO order_condition_rule (condition_id, name, description, logical_operator, priority, is_active)
VALUES (
    (SELECT id FROM order_condition WHERE code = 'ALPINE_VN'),
    'Alpine Brand Rule',
    'Activated when brand is ALPINE',
    'AND', 0, true
);

INSERT INTO order_condition_criteria (rule_id, field_path, operator, value, sequence)
VALUES (
    (SELECT id FROM order_condition_rule WHERE name = 'Alpine Brand Rule'),
    'order.make', 'EQUALS', '"ALPINE"', 0
);

-- 15. ZE VN
INSERT INTO order_condition_rule (condition_id, name, description, logical_operator, priority, is_active)
VALUES (
    (SELECT id FROM order_condition WHERE code = 'ZE_VN'),
    'Electric Vehicle Rule',
    'Activated when energy type contains Electrique',
    'AND', 0, true
);

INSERT INTO order_condition_criteria (rule_id, field_path, operator, value, sequence)
VALUES (
    (SELECT id FROM order_condition_rule WHERE name = 'Electric Vehicle Rule'),
    'order.fuelType', 'CONTAINS', '"Electrique"', 0
);

-- 16. HE VN
INSERT INTO order_condition_rule (condition_id, name, description, logical_operator, priority, is_active)
VALUES (
    (SELECT id FROM order_condition WHERE code = 'HE_VN'),
    'Hybrid Electric Rule',
    'Activated when energy type contains Hybride',
    'AND', 0, true
);

INSERT INTO order_condition_criteria (rule_id, field_path, operator, value, sequence)
VALUES (
    (SELECT id FROM order_condition_rule WHERE name = 'Hybrid Electric Rule'),
    'order.fuelType', 'CONTAINS', '"Hybride"', 0
);

-- 17. AUTO SEPHERE VN
INSERT INTO order_condition_rule (condition_id, name, description, logical_operator, priority, is_active)
VALUES (
    (SELECT id FROM order_condition WHERE code = 'AUTO_SEPHERE_VN'),
    'CGI Financier Rule',
    'Activated when financier is CGI',
    'AND', 0, true
);

INSERT INTO order_condition_criteria (rule_id, field_path, operator, value, sequence)
VALUES (
    (SELECT id FROM order_condition_rule WHERE name = 'CGI Financier Rule'),
    'order.financingInstitution', 'EQUALS', '"CGI"', 0
);

-- 18. CEE VN
INSERT INTO order_condition_rule (condition_id, name, description, logical_operator, priority, is_active)
VALUES (
    (SELECT id FROM order_condition WHERE code = 'CEE_VN'),
    'CEE Commercial Action Rule',
    'Activated when CEE commercial action exists',
    'AND', 0, true
);

INSERT INTO order_condition_criteria (rule_id, field_path, operator, value, sequence)
VALUES (
    (SELECT id FROM order_condition_rule WHERE name = 'CEE Commercial Action Rule'),
    'order.commercialActions', 'EXISTS', 'null', 0
);

-- VO Rules (similar pattern, abbreviated for brevity)

-- 1. REPRISE VO
INSERT INTO order_condition_rule (condition_id, name, description, logical_operator, priority, is_active)
VALUES (
    (SELECT id FROM order_condition WHERE code = 'REPRISE_VO'),
    'Trade-in value exists (VO)',
    'Activated when there is a trade-in value',
    'AND', 0, true
);

INSERT INTO order_condition_criteria (rule_id, field_path, operator, value, sequence)
VALUES (
    (SELECT id FROM order_condition_rule WHERE name = 'Trade-in value exists (VO)'),
    'order.tradeinValue', 'GREATER_THAN', '0', 0
);

-- 2. DIAC VO
INSERT INTO order_condition_rule (condition_id, name, description, logical_operator, priority, is_active)
VALUES (
    (SELECT id FROM order_condition WHERE code = 'DIAC_VO'),
    'DIAC Product Type Rule (VO)',
    'Activated for specific DIAC product types',
    'AND', 0, true
);

INSERT INTO order_condition_criteria (rule_id, field_path, operator, value, sequence)
VALUES (
    (SELECT id FROM order_condition_rule WHERE name = 'DIAC Product Type Rule (VO)'),
    'order.productType', 'IN', '["ND1", "ND2", "ND3", "CB", "CBE", "CD", "CC", "LPC", "LPD"]', 0
);

-- 3. PARTICULIER VO
INSERT INTO order_condition_rule (condition_id, name, description, logical_operator, priority, is_active)
VALUES (
    (SELECT id FROM order_condition WHERE code = 'PARTICULIER_VO'),
    'Individual Client Rule (VO)',
    'Activated when client type is Particulier',
    'AND', 0, true
);

INSERT INTO order_condition_criteria (rule_id, field_path, operator, value, sequence)
VALUES (
    (SELECT id FROM order_condition_rule WHERE name = 'Individual Client Rule (VO)'),
    'order.customerType', 'EQUALS', '"PA"', 0
);

-- 4. ZE VO
INSERT INTO order_condition_rule (condition_id, name, description, logical_operator, priority, is_active)
VALUES (
    (SELECT id FROM order_condition WHERE code = 'ZE_VO'),
    'Electric Vehicle Rule (VO)',
    'Activated when energy type contains Electrique',
    'AND', 0, true
);

INSERT INTO order_condition_criteria (rule_id, field_path, operator, value, sequence)
VALUES (
    (SELECT id FROM order_condition_rule WHERE name = 'Electric Vehicle Rule (VO)'),
    'order.fuelType', 'CONTAINS', '"Electrique"', 0
);

-- 5. HE VO
INSERT INTO order_condition_rule (condition_id, name, description, logical_operator, priority, is_active)
VALUES (
    (SELECT id FROM order_condition WHERE code = 'HE_VO'),
    'Hybrid Electric Rule (VO)',
    'Activated when energy type contains Hybride',
    'AND', 0, true
);

INSERT INTO order_condition_criteria (rule_id, field_path, operator, value, sequence)
VALUES (
    (SELECT id FROM order_condition_rule WHERE name = 'Hybrid Electric Rule (VO)'),
    'order.fuelType', 'CONTAINS', '"Hybride"', 0
);

-- 6. EN VO (Entretien)
INSERT INTO order_condition_rule (condition_id, name, description, logical_operator, priority, is_active)
VALUES (
    (SELECT id FROM order_condition WHERE code = 'EN_VO'),
    'Maintenance Service Rule (VO)',
    'Activated when maintenance service exists',
    'AND', 0, true
);

INSERT INTO order_condition_criteria (rule_id, field_path, operator, value, sequence)
VALUES (
    (SELECT id FROM order_condition_rule WHERE name = 'Maintenance Service Rule (VO)'),
    'order.contractServices', 'EXISTS', 'null', 0
);

-- 7. EX VO (Extension Garantie)
INSERT INTO order_condition_rule (condition_id, name, description, logical_operator, priority, is_active)
VALUES (
    (SELECT id FROM order_condition WHERE code = 'EX_VO'),
    'Warranty Extension Rule (VO)',
    'Activated when warranty extension service exists',
    'AND', 0, true
);

INSERT INTO order_condition_criteria (rule_id, field_path, operator, value, sequence)
VALUES (
    (SELECT id FROM order_condition_rule WHERE name = 'Warranty Extension Rule (VO)'),
    'order.contractServices', 'EXISTS', 'null', 0
);

-- 8. E/R VO
INSERT INTO order_condition_rule (condition_id, name, description, logical_operator, priority, is_active)
VALUES (
    (SELECT id FROM order_condition WHERE code = 'ER_VO'),
    'E/R Product Type Rule (VO)',
    'Activated for ND1, ND2, ND3, DLA, CBE product types',
    'AND', 0, true
);

INSERT INTO order_condition_criteria (rule_id, field_path, operator, value, sequence)
VALUES (
    (SELECT id FROM order_condition_rule WHERE name = 'E/R Product Type Rule (VO)'),
    'order.productType', 'IN', '["ND1", "ND2", "ND3", "DLA", "CBE"]', 0
);

-- Insert field definitions for UI
INSERT INTO condition_field_definition (field_path, field_label, field_type, entity, available_operators, description, is_active)
VALUES
('order.tradeinValue', 'Trade-in Value', 'NUMBER', 'order', '["EQUALS", "NOT_EQUALS", "GREATER_THAN", "LESS_THAN", "GREATER_THAN_OR_EQUAL", "LESS_THAN_OR_EQUAL"]', 'Trade-in vehicle value', true),
('order.productType', 'Product Type', 'STRING', 'order', '["EQUALS", "NOT_EQUALS", "IN", "NOT_IN"]', 'Financial product type (ND1, ND2, etc.)', true),
('order.make', 'Make/Brand', 'STRING', 'order', '["EQUALS", "NOT_EQUALS", "IN", "NOT_IN", "CONTAINS"]', 'Vehicle make or brand', true),
('order.fuelType', 'Fuel Type', 'STRING', 'order', '["EQUALS", "NOT_EQUALS", "IN", "NOT_IN", "CONTAINS"]', 'Vehicle fuel/energy type', true),
('order.financingInstitution', 'Financing Institution', 'STRING', 'order', '["EQUALS", "NOT_EQUALS", "IN", "NOT_IN"]', 'Financial institution name', true),
('order.accessoriesTotal', 'Accessories Total', 'NUMBER', 'order', '["EQUALS", "NOT_EQUALS", "GREATER_THAN", "LESS_THAN"]', 'Total accessories amount', true),
('order.accessories', 'Accessories', 'ARRAY', 'order', '["EXISTS", "NOT_EXISTS", "COUNT_GREATER_THAN", "COUNT_EQUALS"]', 'Order accessories collection', true),
('order.contractServices', 'Contract Services', 'ARRAY', 'order', '["EXISTS", "NOT_EXISTS", "COUNT_GREATER_THAN", "COUNT_EQUALS"]', 'Contract services collection', true),
('order.commercialActions', 'Commercial Actions', 'ARRAY', 'order', '["EXISTS", "NOT_EXISTS", "COUNT_GREATER_THAN", "COUNT_EQUALS"]', 'Commercial actions collection', true),
('order.orderType', 'Order Type', 'STRING', 'order', '["EQUALS", "NOT_EQUALS", "IN", "NOT_IN"]', 'Order type (VN, VO, etc.)', true),
('order.customerType', 'Customer Type', 'STRING', 'order', '["EQUALS", "NOT_EQUALS", "IN", "NOT_IN"]', 'Customer type (PA=Particulier, PRO=Business)', true),
('order.options', 'Options', 'ARRAY', 'order', '["EXISTS", "NOT_EXISTS", "COUNT_GREATER_THAN", "COUNT_EQUALS"]', 'Order options collection', true),
('order.supplements', 'Supplements', 'ARRAY', 'order', '["EXISTS", "NOT_EXISTS", "COUNT_GREATER_THAN", "COUNT_EQUALS"]', 'Order supplements collection', true);

-- Comments
COMMENT ON TABLE order_condition IS 'Master table for all configurable order conditions';
COMMENT ON TABLE order_condition_rule IS 'Business rules that trigger conditions';
COMMENT ON TABLE order_condition_criteria IS 'Individual criteria within rules';
