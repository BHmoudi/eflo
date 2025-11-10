import { NextRequest, NextResponse } from 'next/server';

const ORDER_SERVICE_URL = 'http://localhost:8080/order-service/api/v1';

export async function GET(
  request: NextRequest,
  { params }: { params: { conditionId: string } }
) {
  try {
    const token = request.headers.get('authorization');
    const { conditionId } = params;

    const response = await fetch(`${ORDER_SERVICE_URL}/conditions/${conditionId}/rules`, {
      headers: {
        'Authorization': token || '',
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      const errorText = await response.text();
      console.error('Condition API error:', errorText);
      return NextResponse.json({ error: errorText || 'Failed to fetch condition rules' }, { status: response.status });
    }

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    console.error('Condition API error:', error);
    return NextResponse.json({
      error: error instanceof Error ? error.message : 'Failed to fetch condition rules'
    }, { status: 500 });
  }
}
