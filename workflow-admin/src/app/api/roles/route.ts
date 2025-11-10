import { NextRequest, NextResponse } from 'next/server';

// Call workflow-service directly (bypass gateway)
const GATEWAY_URL = 'http://localhost:8080/api/v1';

export async function GET(request: NextRequest) {
  try {
    const token = request.headers.get('authorization');

    console.log('Fetching roles from:', `${GATEWAY_URL}/roles`);

    const response = await fetch(`${GATEWAY_URL}/roles`, {
      headers: {
        'Authorization': token || '',
        'Content-Type': 'application/json',
      },
    });

    console.log('Roles response status:', response.status);

    if (!response.ok) {
      const errorText = await response.text();
      console.error('Roles error:', response.status, errorText);
      return NextResponse.json({ error: errorText || 'Failed to fetch roles' }, { status: response.status });
    }

    const data = await response.json();
    return NextResponse.json(data, { status: 200 });
  } catch (error) {
    console.error('Roles API error:', error);
    return NextResponse.json({ error: error instanceof Error ? error.message : 'Failed to fetch roles' }, { status: 500 });
  }
}

export async function POST(request: NextRequest) {
  try {
    const token = request.headers.get('authorization');
    const body = await request.json();

    const response = await fetch(`${GATEWAY_URL}/roles`, {
      method: 'POST',
      headers: {
        'Authorization': token || '',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
    });

    if (!response.ok) {
      const errorText = await response.text();
      return NextResponse.json({ error: errorText || 'Failed to create role' }, { status: response.status });
    }

    const data = await response.json();
    return NextResponse.json(data, { status: 201 });
  } catch (error) {
    return NextResponse.json({ error: error instanceof Error ? error.message : 'Failed to create role' }, { status: 500 });
  }
}
