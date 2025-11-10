-- Insert sample workflow processes and their states
-- VN (New Vehicle) Standard Workflow

INSERT INTO workflow_processes (process_code, process_name, description, order_type, max_duration_days, auto_progress_enabled, configuration)
VALUES
    ('VN_STANDARD', 'Standard New Vehicle Order Workflow', 'Standard workflow for new vehicle orders', 'VN', 90, true, '{"notifications": true, "auto_escalation": true}'::jsonb),
    ('VO_STANDARD', 'Standard Used Vehicle Order Workflow', 'Standard workflow for used vehicle orders', 'VO', 30, true, '{"notifications": true, "auto_escalation": true}'::jsonb),
    ('EVO_STANDARD', 'Standard Evolution Order Workflow', 'Standard workflow for evolution vehicle orders', 'EVO', 60, true, '{"notifications": true, "auto_escalation": true}'::jsonb);

-- VN Standard Workflow States
INSERT INTO workflow_states (process_id, state_code, state_name, description, state_order, state_type, expected_duration_hours, is_final_state, requires_approval)
SELECT
    p.id,
    s.state_code,
    s.state_name,
    s.description,
    s.state_order,
    s.state_type,
    s.expected_duration_hours,
    s.is_final_state,
    s.requires_approval
FROM workflow_processes p
CROSS JOIN (VALUES
    ('DRAFT', 'Draft Order', 'Initial draft state for order creation', 1, 'START', 24, false, false),
    ('VALIDATION_CDV', 'Validation by Sales Manager', 'Order validation by sales manager', 2, 'NORMAL', 48, false, true),
    ('VALIDATION_CDR', 'Validation by Regional Manager', 'Order validation by regional manager', 3, 'NORMAL', 48, false, true),
    ('PENDING_FINANCING', 'Pending Financing', 'Awaiting financing approval', 4, 'NORMAL', 120, false, false),
    ('FINANCING_APPROVED', 'Financing Approved', 'Financing has been approved', 5, 'NORMAL', 24, false, false),
    ('ORDER_CONFIRMED', 'Order Confirmed', 'Order confirmed and submitted to manufacturer', 6, 'NORMAL', 24, false, false),
    ('VEHICLE_IN_PRODUCTION', 'Vehicle In Production', 'Vehicle is being manufactured', 7, 'NORMAL', 1440, false, false),
    ('VEHICLE_DELIVERED_DEALER', 'Vehicle Delivered to Dealer', 'Vehicle delivered to dealership', 8, 'NORMAL', 72, false, false),
    ('READY_FOR_DELIVERY', 'Ready for Customer Delivery', 'Vehicle ready for customer pickup', 9, 'NORMAL', 48, false, false),
    ('DELIVERED', 'Delivered to Customer', 'Vehicle delivered to customer', 10, 'FINAL', 24, true, false),
    ('CANCELLED', 'Order Cancelled', 'Order has been cancelled', 11, 'FINAL', 0, true, false)
) AS s(state_code, state_name, description, state_order, state_type, expected_duration_hours, is_final_state, requires_approval)
WHERE p.process_code = 'VN_STANDARD';

-- VO Standard Workflow States
INSERT INTO workflow_states (process_id, state_code, state_name, description, state_order, state_type, expected_duration_hours, is_final_state, requires_approval)
SELECT
    p.id,
    s.state_code,
    s.state_name,
    s.description,
    s.state_order,
    s.state_type,
    s.expected_duration_hours,
    s.is_final_state,
    s.requires_approval
FROM workflow_processes p
CROSS JOIN (VALUES
    ('DRAFT', 'Draft Order', 'Initial draft state for used vehicle order', 1, 'START', 12, false, false),
    ('VEHICLE_INSPECTION', 'Vehicle Inspection', 'Technical inspection of used vehicle', 2, 'NORMAL', 48, false, false),
    ('VALUATION', 'Vehicle Valuation', 'Professional valuation of used vehicle', 3, 'NORMAL', 24, false, true),
    ('VALIDATION_CDV', 'Validation by Sales Manager', 'Order validation by sales manager', 4, 'NORMAL', 24, false, true),
    ('PENDING_FINANCING', 'Pending Financing', 'Awaiting financing approval', 5, 'NORMAL', 72, false, false),
    ('FINANCING_APPROVED', 'Financing Approved', 'Financing has been approved', 6, 'NORMAL', 12, false, false),
    ('READY_FOR_DELIVERY', 'Ready for Delivery', 'Vehicle ready for customer', 7, 'NORMAL', 24, false, false),
    ('DELIVERED', 'Delivered to Customer', 'Vehicle delivered to customer', 8, 'FINAL', 12, true, false),
    ('CANCELLED', 'Order Cancelled', 'Order has been cancelled', 9, 'FINAL', 0, true, false)
) AS s(state_code, state_name, description, state_order, state_type, expected_duration_hours, is_final_state, requires_approval)
WHERE p.process_code = 'VO_STANDARD';

-- EVO Standard Workflow States
INSERT INTO workflow_states (process_id, state_code, state_name, description, state_order, state_type, expected_duration_hours, is_final_state, requires_approval)
SELECT
    p.id,
    s.state_code,
    s.state_name,
    s.description,
    s.state_order,
    s.state_type,
    s.expected_duration_hours,
    s.is_final_state,
    s.requires_approval
FROM workflow_processes p
CROSS JOIN (VALUES
    ('DRAFT', 'Draft Order', 'Initial draft state for evolution order', 1, 'START', 24, false, false),
    ('CONVERSION_PLANNING', 'Conversion Planning', 'Planning the vehicle conversion', 2, 'NORMAL', 72, false, true),
    ('VALIDATION_CDV', 'Validation by Sales Manager', 'Order validation by sales manager', 3, 'NORMAL', 48, false, true),
    ('PENDING_FINANCING', 'Pending Financing', 'Awaiting financing approval', 4, 'NORMAL', 96, false, false),
    ('FINANCING_APPROVED', 'Financing Approved', 'Financing has been approved', 5, 'NORMAL', 24, false, false),
    ('CONVERSION_IN_PROGRESS', 'Conversion in Progress', 'Vehicle conversion work in progress', 6, 'NORMAL', 720, false, false),
    ('QUALITY_INSPECTION', 'Quality Inspection', 'Post-conversion quality check', 7, 'NORMAL', 48, false, true),
    ('READY_FOR_DELIVERY', 'Ready for Delivery', 'Vehicle ready for customer', 8, 'NORMAL', 48, false, false),
    ('DELIVERED', 'Delivered to Customer', 'Vehicle delivered to customer', 9, 'FINAL', 24, true, false),
    ('CANCELLED', 'Order Cancelled', 'Order has been cancelled', 10, 'FINAL', 0, true, false)
) AS s(state_code, state_name, description, state_order, state_type, expected_duration_hours, is_final_state, requires_approval)
WHERE p.process_code = 'EVO_STANDARD';

-- Create standard transitions for VN workflow
INSERT INTO workflow_transitions (process_id, from_state_id, to_state_id, transition_name, transition_type, requires_approval, auto_transition)
SELECT
    p.id,
    s1.id,
    s2.id,
    t.transition_name,
    t.transition_type,
    t.requires_approval,
    t.auto_transition
FROM workflow_processes p
JOIN workflow_states s1 ON s1.process_id = p.id
JOIN workflow_states s2 ON s2.process_id = p.id
CROSS JOIN (VALUES
    ('DRAFT', 'VALIDATION_CDV', 'Submit for Validation', 'NORMAL', false, false),
    ('VALIDATION_CDV', 'VALIDATION_CDR', 'Approve and Forward', 'NORMAL', true, false),
    ('VALIDATION_CDV', 'DRAFT', 'Reject and Return', 'ROLLBACK', false, false),
    ('VALIDATION_CDR', 'PENDING_FINANCING', 'Approve and Submit', 'NORMAL', true, false),
    ('VALIDATION_CDR', 'DRAFT', 'Reject and Return', 'ROLLBACK', false, false),
    ('PENDING_FINANCING', 'FINANCING_APPROVED', 'Financing Approved', 'NORMAL', false, true),
    ('FINANCING_APPROVED', 'ORDER_CONFIRMED', 'Confirm Order', 'NORMAL', false, false),
    ('ORDER_CONFIRMED', 'VEHICLE_IN_PRODUCTION', 'Production Started', 'NORMAL', false, true),
    ('VEHICLE_IN_PRODUCTION', 'VEHICLE_DELIVERED_DEALER', 'Vehicle Arrived', 'NORMAL', false, false),
    ('VEHICLE_DELIVERED_DEALER', 'READY_FOR_DELIVERY', 'Prepare for Delivery', 'NORMAL', false, false),
    ('READY_FOR_DELIVERY', 'DELIVERED', 'Deliver to Customer', 'NORMAL', false, false),
    ('DRAFT', 'CANCELLED', 'Cancel Order', 'CANCEL', false, false),
    ('VALIDATION_CDV', 'CANCELLED', 'Cancel Order', 'CANCEL', true, false),
    ('VALIDATION_CDR', 'CANCELLED', 'Cancel Order', 'CANCEL', true, false),
    ('PENDING_FINANCING', 'CANCELLED', 'Cancel Order', 'CANCEL', false, false)
) AS t(from_state_code, to_state_code, transition_name, transition_type, requires_approval, auto_transition)
WHERE p.process_code = 'VN_STANDARD'
  AND s1.state_code = t.from_state_code
  AND s2.state_code = t.to_state_code;

COMMENT ON TABLE workflow_processes IS 'Sample workflow processes are pre-populated for VN, VO, and EVO order types';
