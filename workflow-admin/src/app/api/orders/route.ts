import { NextRequest, NextResponse } from 'next/server';

const ORDER_SERVICE_URL = 'http://localhost:8080/order-service/api/v1';

export async function GET(request: NextRequest) {
  try {
    const token = request.headers.get('authorization');
    const searchParams = request.nextUrl.searchParams;

    // Build query parameters
    const params = new URLSearchParams();
    const page = searchParams.get('page') || '0';
    const limit = searchParams.get('limit') || '20';
    const search = searchParams.get('search') || '';
    const status = searchParams.get('status');
    const orderType = searchParams.get('orderType');
    const startDate = searchParams.get('startDate');
    const endDate = searchParams.get('endDate');

    params.append('page', page);
    params.append('size', limit);
    if (search) params.append('search', search);
    if (status) params.append('status', status);
    if (orderType) params.append('orderType', orderType);
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);

    const response = await fetch(`${ORDER_SERVICE_URL}/orders?${params.toString()}`, {
      headers: {
        'Authorization': token || '',
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      const errorText = await response.text();
      console.error('Order API error:', errorText);
      return NextResponse.json({ error: errorText || 'Failed to fetch orders' }, { status: response.status });
    }

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    console.error('Order API error:', error);
    return NextResponse.json({
      error: error instanceof Error ? error.message : 'Failed to fetch orders'
    }, { status: 500 });
  }
}

export async function POST(request: NextRequest) {
  try {
    const token = request.headers.get('authorization');
    const body = await request.json();

    const response = await fetch(`${ORDER_SERVICE_URL}/orders`, {
      method: 'POST',
      headers: {
        'Authorization': token || '',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
    });

    if (!response.ok) {
      const errorText = await response.text();
      return NextResponse.json({ error: errorText || 'Failed to create order' }, { status: response.status });
    }

    const data = await response.json();
    return NextResponse.json(data, { status: 201 });
  } catch (error) {
    return NextResponse.json({
      error: error instanceof Error ? error.message : 'Failed to create order'
    }, { status: 500 });
  }
}
