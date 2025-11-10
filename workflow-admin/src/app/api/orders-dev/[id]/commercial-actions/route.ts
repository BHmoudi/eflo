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
        action_code,
        action_label,
        amount_or_percentage,
        is_percentage,
        vat_rate,
        vat_type,
        created_at
      FROM order_commercial_actions
      WHERE order_id = $1
      ORDER BY action_label
    `;

    const result = await client.query(query, [orderId]);

    const actions = result.rows.map(row => ({
      id: parseInt(row.id),
      orderId: parseInt(row.order_id),
      actionCode: row.action_code,
      actionLabel: row.action_label,
      amountOrPercentage: parseFloat(row.amount_or_percentage),
      isPercentage: row.is_percentage,
      vatRate: parseFloat(row.vat_rate || 0),
      vatType: row.vat_type,
      createdAt: row.created_at,
    }));

    await client.end();
    return NextResponse.json(actions);

  } catch (error) {
    console.error('Database error:', error);
    await client.end().catch(() => {});
    return NextResponse.json({ error: 'Failed to fetch commercial actions' }, { status: 500 });
  }
}
