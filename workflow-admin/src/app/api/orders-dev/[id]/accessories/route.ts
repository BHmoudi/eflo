import { NextRequest, NextResponse } from 'next/server';
import { Client } from 'pg';

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

    const query = `
      SELECT
        id,
        order_id,
        accessory_code,
        accessory_name,
        accessory_category,
        description,
        quantity,
        unit_price,
        total_price,
        cost_per_unit,
        total_cost,
        supplier,
        installation_required,
        created_at,
        created_by_user_id
      FROM order_accessories
      WHERE order_id = $1
      ORDER BY accessory_category, accessory_name
    `;

    const result = await client.query(query, [orderId]);

    const accessories = result.rows.map(row => ({
      id: parseInt(row.id),
      orderId: parseInt(row.order_id),
      accessoryCode: row.accessory_code,
      accessoryName: row.accessory_name,
      accessoryCategory: row.accessory_category,
      description: row.description,
      quantity: parseInt(row.quantity),
      unitPrice: parseFloat(row.unit_price),
      totalPrice: parseFloat(row.total_price),
      costPerUnit: parseFloat(row.cost_per_unit || 0),
      totalCost: parseFloat(row.total_cost || 0),
      supplier: row.supplier,
      installationRequired: row.installation_required,
      createdAt: row.created_at,
      createdByUserId: parseInt(row.created_by_user_id),
    }));

    await client.end();
    return NextResponse.json(accessories);

  } catch (error) {
    console.error('Database error:', error);
    await client.end().catch(() => {});
    return NextResponse.json({ error: 'Failed to fetch accessories' }, { status: 500 });
  }
}
