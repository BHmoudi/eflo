import { NextRequest, NextResponse } from 'next/server';

const GATEWAY_URL = 'http://localhost:8080/api/v1';

export async function GET(request: NextRequest) {
  try {
    const token = request.headers.get('authorization');

    const response = await fetch(`${GATEWAY_URL}/workflow/dashboard`, {
      headers: {
        'Authorization': token || '',
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      const errorText = await response.text();
      console.error('Dashboard error:', response.status, errorText);
      return NextResponse.json({ error: errorText || 'Failed to fetch dashboard data' }, { status: response.status });
    }

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    console.error('Dashboard API error:', error);
    return NextResponse.json({ error: error instanceof Error ? error.message : 'Failed to fetch dashboard data' }, { status: 500 });
  }
}
