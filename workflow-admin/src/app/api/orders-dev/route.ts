import { NextRequest, NextResponse } from 'next/server';
import { Client } from 'pg';

// Real database connection - fetches actual orders from backend database
const DB_CONFIG = {
  host: 'localhost',
  port: 5434,
  database: 'order_db',
  user: 'order_user',
  password: 'change_me_order_db_password_2025',
};

export async function GET(request: NextRequest) {
  const client = new Client(DB_CONFIG);

  try {
    await client.connect();

    const searchParams = request.nextUrl.searchParams;
    const page = parseInt(searchParams.get('page') || '0');
    const limit = parseInt(searchParams.get('limit') || '20');
    const offset = page * limit;

    // Get orders with pagination
    const ordersQuery = `
      SELECT
        id,
        order_number,
        order_type,
        status,
        customer_id,
        business_unit_id,
        salesperson_id,
        make,
        model,
        year,
        vin,
        color_exterior as "colorExterior",
        base_price,
        total_amount,
        customer_first_name,
        customer_last_name,
        customer_email,
        salesperson_name,
        created_at,
        updated_at
      FROM orders
      WHERE deleted_at IS NULL
      ORDER BY created_at DESC
      LIMIT $1 OFFSET $2
    `;

    const countQuery = `
      SELECT COUNT(*) as total
      FROM orders
      WHERE deleted_at IS NULL
    `;

    const ordersResult = await client.query(ordersQuery, [limit, offset]);
    const countResult = await client.query(countQuery);

    const total = parseInt(countResult.rows[0].total);
    const totalPages = Math.ceil(total / limit);

    // Transform to match expected format
    const orders = ordersResult.rows.map(order => ({
      id: order.id,
      orderNumber: order.order_number,
      type: order.order_type,
      status: order.status,
      customerId: order.customer_id,
      customerName: order.customer_first_name && order.customer_last_name
        ? `${order.customer_first_name} ${order.customer_last_name}`
        : 'Unknown Customer',
      vehicleName: order.make && order.model && order.year
        ? `${order.year} ${order.make} ${order.model}`
        : 'Vehicle Details Pending',
      vin: order.vin,
      totalPrice: parseFloat(order.base_price || 0),
      amountDue: parseFloat(order.base_price || 0), // Simplified
      paymentStatus: 'PENDING',
      businessUnitName: 'Business Unit ' + order.business_unit_id,
      salesPersonName: order.salesperson_name || 'Salesperson',
      createdAt: order.created_at,
      estimatedDeliveryDate: null,
    }));

    await client.end();

    return NextResponse.json({
      content: orders,
      totalElements: total,
      totalPages: totalPages,
      number: page,
      size: limit,
    });

  } catch (error) {
    console.error('Database error:', error);
    await client.end().catch(() => {});

    return NextResponse.json({
      error: error instanceof Error ? error.message : 'Database error'
    }, { status: 500 });
  }
}
