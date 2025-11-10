import { NextRequest, NextResponse } from 'next/server';

const ORDER_SERVICE_URL = 'http://localhost:8080/order-service/api/v1';

export async function GET(request: NextRequest) {
  try {
    const token = request.headers.get('authorization');
    const searchParams = request.nextUrl.searchParams;

    const params = new URLSearchParams();
    const page = searchParams.get('page') || '0';
    const size = searchParams.get('size') || '20';
    const search = searchParams.get('search');

    params.append('page', page);
    params.append('size', size);
    if (search) params.append('search', search);

    const response = await fetch(`${ORDER_SERVICE_URL}/conditions?${params.toString()}`, {
      headers: {
        'Authorization': token || '',
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      const errorText = await response.text();
      console.error('Condition API error:', errorText);
      return NextResponse.json({ error: errorText || 'Failed to fetch conditions' }, { status: response.status });
    }

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    console.error('Condition API error:', error);
    return NextResponse.json({
      error: error instanceof Error ? error.message : 'Failed to fetch conditions'
    }, { status: 500 });
  }
}

export async function POST(request: NextRequest) {
  try {
    const token = request.headers.get('authorization');
    const body = await request.json();

    const response = await fetch(`${ORDER_SERVICE_URL}/conditions`, {
      method: 'POST',
      headers: {
        'Authorization': token || '',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
    });

    if (!response.ok) {
      const errorText = await response.text();
      return NextResponse.json({ error: errorText || 'Failed to create condition' }, { status: response.status });
    }

    const data = await response.json();
    return NextResponse.json(data, { status: 201 });
  } catch (error) {
    return NextResponse.json({
      error: error instanceof Error ? error.message : 'Failed to create condition'
    }, { status: 500 });
  }
}
