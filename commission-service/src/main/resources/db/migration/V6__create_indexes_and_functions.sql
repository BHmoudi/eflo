-- V6__create_indexes_and_functions.sql
-- Additional indexes and database functions

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Triggers for updated_at
CREATE TRIGGER update_commission_scales_updated_at BEFORE UPDATE ON commission_scales
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_commission_scale_tiers_updated_at BEFORE UPDATE ON commission_scale_tiers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_commissions_updated_at BEFORE UPDATE ON commissions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_commission_payments_updated_at BEFORE UPDATE ON commission_payments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Function to add commission history entry
CREATE OR REPLACE FUNCTION add_commission_history_entry()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'UPDATE' AND OLD.status != NEW.status) THEN
        INSERT INTO commission_history (
            commission_id,
            order_id,
            order_number,
            change_type,
            change_description,
            previous_status,
            new_status,
            previous_amount,
            new_amount,
            amount_difference,
            changed_by
        ) VALUES (
            NEW.id,
            NEW.order_id,
            NEW.order_number,
            'STATUS_CHANGE',
            'Status changed from ' || OLD.status || ' to ' || NEW.status,
            OLD.status,
            NEW.status,
            OLD.total_commission_excl_tax,
            NEW.total_commission_excl_tax,
            NEW.total_commission_excl_tax - OLD.total_commission_excl_tax,
            COALESCE(NEW.updated_by, 'SYSTEM')
        );
    END IF;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger for commission history
CREATE TRIGGER commission_status_change_history AFTER UPDATE ON commissions
    FOR EACH ROW EXECUTE FUNCTION add_commission_history_entry();

-- Function to extract year from timestamp
CREATE OR REPLACE FUNCTION payment_year(ts TIMESTAMP)
RETURNS INT AS $$
BEGIN
    RETURN EXTRACT(YEAR FROM ts);
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- Function to extract month from timestamp
CREATE OR REPLACE FUNCTION payment_month(ts TIMESTAMP)
RETURNS INT AS $$
BEGIN
    RETURN EXTRACT(MONTH FROM ts);
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- Additional composite indexes for reporting
CREATE INDEX idx_commissions_reporting ON commissions(business_unit_id, payment_year(calculation_date), payment_month(calculation_date))
    WHERE status IN ('VALIDATED', 'PENDING_PAYMENT', 'PAID');

CREATE INDEX idx_commissions_payment_processing ON commissions(business_unit_id, status, payment_due_date)
    WHERE status IN ('VALIDATED', 'PENDING_PAYMENT');

-- Comments
COMMENT ON FUNCTION update_updated_at_column() IS 'Automatically updates the updated_at timestamp on record modification';
COMMENT ON FUNCTION add_commission_history_entry() IS 'Automatically creates history entry when commission status changes';
