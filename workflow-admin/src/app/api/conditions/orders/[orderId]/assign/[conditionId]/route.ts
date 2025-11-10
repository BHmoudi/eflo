import { NextRequest, NextResponse } from 'next/server';

const ORDER_SERVICE_URL = 'http://localhost:8080/order-service/api/v1';

export async function POST(
  request: NextRequest,
  { params }: { params: { orderId: string; conditionId: string } }
) {
  try {
    const token = request.headers.get('authorization');
    const { orderId, conditionId } = params;

    const response = await fetch(
      `${ORDER_SERVICE_URL}/conditions/orders/${orderId}/assign/${conditionId}`,
      {
        method: 'POST',
        headers: {
          'Authorization': token || '',
          'Content-Type': 'application/json',
        },
      }
    );

    if (!response.ok) {
      const errorText = await response.text();
      return NextResponse.json({ error: errorText || 'Failed to assign condition to order' }, { status: response.status });
    }

    const data = await response.json();
    return NextResponse.json(data, { status: 201 });
  } catch (error) {
    return NextResponse.json({
      error: error instanceof Error ? error.message : 'Failed to assign condition to order'
    }, { status: 500 });
  }
}
