import { NextRequest, NextResponse } from 'next/server';
import { Client } from 'pg';

// Real database connection - fetches a single order by ID
const DB_CONFIG = {
  host: 'localhost',
  port: 5434,
  database: 'order_db',
  user: 'order_user',
  password: 'change_me_order_db_password_2025',
};

export async function GET(
  request: NextRequest,
  { params }: { params: { id: string } }
) {
  const client = new Client(DB_CONFIG);

  try {
    await client.connect();

    const orderId = parseInt(params.id);

    // Get order details
    const orderQuery = `
      SELECT *
      FROM orders
      WHERE id = $1 AND deleted_at IS NULL
    `;

    const result = await client.query(orderQuery, [orderId]);

    if (result.rows.length === 0) {
      await client.end();
      return NextResponse.json({ error: 'Order not found' }, { status: 404 });
    }

    const row = result.rows[0];

    // Fetch all child entities
    const optionsQuery = `SELECT * FROM order_options WHERE order_id = $1 ORDER BY option_category, option_name`;
    const accessoriesQuery = `SELECT * FROM order_accessories WHERE order_id = $1 ORDER BY accessory_category, accessory_name`;
    const servicesQuery = `SELECT * FROM order_contract_services WHERE order_id = $1 ORDER BY service_name`;
    const aidsQuery = `SELECT * FROM order_aids WHERE order_id = $1 ORDER BY aid_name`;
    const supplementsQuery = `SELECT * FROM order_supplements WHERE order_id = $1 ORDER BY supplement_name`;
    const commercialActionsQuery = `SELECT * FROM order_commercial_actions WHERE order_id = $1 ORDER BY action_label`;
    const historyQuery = `SELECT * FROM order_history WHERE order_id = $1 ORDER BY changed_at DESC`;
    const conditionsQuery = `SELECT * FROM order_assigned_condition WHERE order_id = $1 ORDER BY assigned_at DESC`;

    const [optionsRes, accessoriesRes, servicesRes, aidsRes, supplementsRes, actionsRes, historyRes, conditionsRes] = await Promise.all([
      client.query(optionsQuery, [orderId]),
      client.query(accessoriesQuery, [orderId]),
      client.query(servicesQuery, [orderId]),
      client.query(aidsQuery, [orderId]),
      client.query(supplementsQuery, [orderId]),
      client.query(commercialActionsQuery, [orderId]),
      client.query(historyQuery, [orderId]),
      client.query(conditionsQuery, [orderId]),
    ]);

    // Transform to match expected format
    const order = {
      id: parseInt(row.id),
      orderNumber: row.order_number,
      type: row.order_type,
      status: row.status,
      customerId: row.customer_id,
      businessUnitId: row.business_unit_id,
      salesPersonId: row.salesperson_id,

      // Customer info
      customer: {
        id: row.customer_id,
        firstName: row.customer_first_name,
        lastName: row.customer_last_name,
        email: row.customer_email,
        phone: row.customer_phone,
        address: row.customer_address,
        city: row.customer_city,
      },

      // Vehicle info
      vehicle: {
        id: null,
        make: row.make,
        model: row.model,
        year: row.year,
        vin: row.vin,
        trim: row.trim,
        color: row.color_exterior,
      },

      // Pricing
      basePrice: parseFloat(row.base_price || 0),
      optionsTotal: parseFloat(row.options_total || 0),
      accessoriesTotal: parseFloat(row.accessories_total || 0),
      servicesTotal: parseFloat(row.services_total || 0),
      aidsTotal: parseFloat(row.aids_total || 0),
      supplementsTotal: parseFloat(row.supplements_total || 0),
      subtotal: parseFloat(row.subtotal || 0),
      discount: parseFloat(row.discount_amount || 0),
      discountPercentage: parseFloat(row.discount_percentage || 0),
      totalBeforeTax: parseFloat(row.total_before_tax || 0),
      vatRate: parseFloat(row.vat_rate || 0),
      taxAmount: parseFloat(row.vat_amount || 0),
      totalPrice: parseFloat(row.total_amount || row.base_price || 0),
      tradeInValue: parseFloat(row.tradein_value || 0),
      fees: 0,

      // Payment
      depositAmount: 0,
      amountPaid: 0,
      amountDue: parseFloat(row.total_amount || row.base_price || 0),
      paymentStatus: 'PENDING',

      // Business info
      businessUnitName: 'Business Unit ' + row.business_unit_id,
      salesPersonName: row.salesperson_name || row.salesperson_ipn || 'Unknown',

      // Dates
      estimatedDeliveryDate: row.expected_delivery_date,
      actualDeliveryDate: row.actual_delivery_date,

      // Notes
      notes: row.notes,
      internalComments: row.internal_comments,

      // Workflow
      workflowInstanceId: row.workflow_instance_id,
      workflowStatus: row.workflow_current_state,

      // Metadata
      createdAt: row.created_at,
      updatedAt: row.updated_at,
      createdBy: 'User ' + row.created_by_user_id,
      updatedBy: 'User ' + row.updated_by_user_id,

      // Child collections with real data
      options: optionsRes.rows.map(opt => ({
        id: parseInt(opt.id),
        orderId: parseInt(opt.order_id),
        optionCode: opt.option_code,
        optionName: opt.option_name,
        optionCategory: opt.option_category,
        description: opt.description,
        price: parseFloat(opt.price),
        cost: parseFloat(opt.cost || 0),
        isMandatory: opt.is_mandatory,
        isFactoryOption: opt.is_factory_option,
      })),
      accessories: accessoriesRes.rows.map(acc => ({
        id: parseInt(acc.id),
        orderId: parseInt(acc.order_id),
        accessoryCode: acc.accessory_code,
        accessoryName: acc.accessory_name,
        accessoryCategory: acc.accessory_category,
        description: acc.description,
        quantity: parseInt(acc.quantity),
        unitPrice: parseFloat(acc.unit_price),
        totalPrice: parseFloat(acc.total_price),
        supplier: acc.supplier,
        installationRequired: acc.installation_required,
      })),
      contractServices: servicesRes.rows.map(srv => ({
        id: parseInt(srv.id),
        orderId: parseInt(srv.order_id),
        serviceCode: srv.service_code,
        serviceName: srv.service_name,
        serviceType: srv.service_type,
        description: srv.description,
        price: parseFloat(srv.price),
        durationMonths: srv.duration_months,
        provider: srv.provider,
      })),
      aids: aidsRes.rows.map(aid => ({
        id: parseInt(aid.id),
        orderId: parseInt(aid.order_id),
        aidCode: aid.aid_code,
        aidName: aid.aid_name,
        aidType: aid.aid_type,
        description: aid.description,
        amount: parseFloat(aid.amount),
        provider: aid.provider,
      })),
      supplements: supplementsRes.rows.map(sup => ({
        id: parseInt(sup.id),
        orderId: parseInt(sup.order_id),
        supplementCode: sup.supplement_code,
        supplementName: sup.supplement_name,
        supplementType: sup.supplement_type,
        description: sup.description,
        amount: parseFloat(sup.amount),
        reason: sup.reason,
      })),
      commercialActions: actionsRes.rows.map(act => ({
        id: parseInt(act.id),
        orderId: parseInt(act.order_id),
        actionCode: act.action_code,
        actionName: act.action_label,
        amount: parseFloat(act.amount_or_percentage),
        isPercentage: act.is_percentage,
      })),
      history: historyRes.rows.map(hist => ({
        id: parseInt(hist.id),
        orderId: parseInt(hist.order_id),
        eventType: hist.event_type,
        eventDescription: hist.event_description,
        previousStatus: hist.previous_status,
        newStatus: hist.new_status,
        changedByUserId: parseInt(hist.changed_by_user_id),
        changedAt: hist.changed_at,
      })),
      assignedConditions: conditionsRes.rows.map(cond => ({
        id: parseInt(cond.id),
        orderId: parseInt(cond.order_id),
        conditionId: parseInt(cond.condition_id),
        conditionCode: cond.condition_code,
        conditionLabel: cond.condition_label,
        isManual: cond.is_manual,
        assignedAt: cond.assigned_at,
      })),
      documents: [],
      payments: [],
      commissions: [],
    };

    await client.end();

    return NextResponse.json(order);

  } catch (error) {
    console.error('Database error:', error);
    await client.end().catch(() => {});

    return NextResponse.json({
      error: error instanceof Error ? error.message : 'Database error'
    }, { status: 500 });
  }
}
