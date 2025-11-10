import { NextRequest, NextResponse } from 'next/server';

const ORDER_SERVICE_URL = 'http://localhost:8080/order-service/api/v1';

export async function DELETE(
  request: NextRequest,
  { params }: { params: { orderId: string; conditionId: string } }
) {
  try {
    const token = request.headers.get('authorization');
    const { orderId, conditionId } = params;

    const response = await fetch(
      `${ORDER_SERVICE_URL}/conditions/orders/${orderId}/conditions/${conditionId}`,
      {
        method: 'DELETE',
        headers: {
          'Authorization': token || '',
          'Content-Type': 'application/json',
        },
      }
    );

    if (!response.ok) {
      const errorText = await response.text();
      return NextResponse.json({ error: errorText || 'Failed to remove condition from order' }, { status: response.status });
    }

    return NextResponse.json({ message: 'Condition removed from order successfully' }, { status: 200 });
  } catch (error) {
    return NextResponse.json({
      error: error instanceof Error ? error.message : 'Failed to remove condition from order'
    }, { status: 500 });
  }
}
