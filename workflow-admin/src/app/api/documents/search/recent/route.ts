import { NextRequest, NextResponse } from 'next/server';

const GATEWAY_URL = 'http://localhost:8080/api/v1';

export async function GET(request: NextRequest) {
  try {
    const token = request.headers.get('authorization');
    const { searchParams } = new URL(request.url);
    const days = searchParams.get('days') || '7';
    const page = searchParams.get('page') || '0';
    const size = searchParams.get('size') || '20';

    const response = await fetch(
      `${GATEWAY_URL}/documents/search/recent?days=${days}&page=${page}&size=${size}`,
      {
        headers: {
          'Authorization': token || '',
          'Content-Type': 'application/json',
        },
      }
    );

    if (!response.ok) {
      return NextResponse.json(
        { error: 'Failed to fetch recent documents' },
        { status: response.status }
      );
    }

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    return NextResponse.json(
      { error: 'Failed to fetch recent documents' },
      { status: 500 }
    );
  }
}
