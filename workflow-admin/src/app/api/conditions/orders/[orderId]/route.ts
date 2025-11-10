import { NextRequest, NextResponse } from 'next/server';

const ORDER_SERVICE_URL = 'http://localhost:8080/order-service/api/v1';

export async function GET(
  request: NextRequest,
  { params }: { params: { orderId: string } }
) {
  try {
    const token = request.headers.get('authorization');
    const { orderId } = params;

    const response = await fetch(`${ORDER_SERVICE_URL}/conditions/orders/${orderId}`, {
      headers: {
        'Authorization': token || '',
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      const errorText = await response.text();
      console.error('Condition API error:', errorText);
      return NextResponse.json({ error: errorText || 'Failed to fetch order conditions' }, { status: response.status });
    }

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    console.error('Condition API error:', error);
    return NextResponse.json({
      error: error instanceof Error ? error.message : 'Failed to fetch order conditions'
    }, { status: 500 });
  }
}
