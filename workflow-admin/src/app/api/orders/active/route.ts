import { NextRequest, NextResponse } from 'next/server';

const ORDER_SERVICE_URL = 'http://localhost:8080/order-service/api/v1';

export async function GET(request: NextRequest) {
  try {
    const token = request.headers.get('authorization');
    const searchParams = request.nextUrl.searchParams;

    const queryParams = new URLSearchParams();
    const page = searchParams.get('page') || '0';
    const size = searchParams.get('size') || '20';

    queryParams.append('page', page);
    queryParams.append('size', size);

    const response = await fetch(
      `${ORDER_SERVICE_URL}/orders/active?${queryParams.toString()}`,
      {
        headers: {
          'Authorization': token || '',
          'Content-Type': 'application/json',
        },
      }
    );

    if (!response.ok) {
      const errorText = await response.text();
      console.error('Order API error:', errorText);
      return NextResponse.json({ error: errorText || 'Failed to fetch active orders' }, { status: response.status });
    }

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    console.error('Order API error:', error);
    return NextResponse.json({
      error: error instanceof Error ? error.message : 'Failed to fetch active orders'
    }, { status: 500 });
  }
}
