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
        option_code,
        option_name,
        option_category,
        description,
        price,
        cost,
        is_mandatory,
        is_factory_option,
        created_at,
        created_by_user_id
      FROM order_options
      WHERE order_id = $1
      ORDER BY option_category, option_name
    `;

    const result = await client.query(query, [orderId]);

    const options = result.rows.map(row => ({
      id: parseInt(row.id),
      orderId: parseInt(row.order_id),
      optionCode: row.option_code,
      optionName: row.option_name,
      optionCategory: row.option_category,
      description: row.description,
      price: parseFloat(row.price),
      cost: parseFloat(row.cost || 0),
      isMandatory: row.is_mandatory,
      isFactoryOption: row.is_factory_option,
      createdAt: row.created_at,
      createdByUserId: parseInt(row.created_by_user_id),
    }));

    await client.end();
    return NextResponse.json(options);

  } catch (error) {
    console.error('Database error:', error);
    await client.end().catch(() => {});
    return NextResponse.json({ error: 'Failed to fetch options' }, { status: 500 });
  }
}
