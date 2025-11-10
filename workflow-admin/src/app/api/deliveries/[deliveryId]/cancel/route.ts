import { NextRequest, NextResponse } from 'next/server';

const ORDER_SERVICE_URL = 'http://localhost:8080/order-service/api/v1';

export async function POST(
  request: NextRequest,
  { params }: { params: { deliveryId: string } }
) {
  try {
    const token = request.headers.get('authorization');
    const { deliveryId } = params;

    const response = await fetch(`${ORDER_SERVICE_URL}/deliveries/${deliveryId}/cancel`, {
      method: 'POST',
      headers: {
        'Authorization': token || '',
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      const errorText = await response.text();
      return NextResponse.json({ error: errorText || 'Failed to cancel delivery' }, { status: response.status });
    }

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    return NextResponse.json({
      error: error instanceof Error ? error.message : 'Failed to cancel delivery'
    }, { status: 500 });
  }
}
