import { NextRequest, NextResponse } from 'next/server';

const GATEWAY_URL = 'http://localhost:8080/api/v1';

export async function POST(request: NextRequest) {
  try {
    const token = request.headers.get('authorization');
    const { searchParams } = new URL(request.url);
    const page = searchParams.get('page') || '0';
    const size = searchParams.get('size') || '20';
    const metadata = await request.json();

    const response = await fetch(
      `${GATEWAY_URL}/documents/search/metadata?page=${page}&size=${size}`,
      {
        method: 'POST',
        headers: {
          'Authorization': token || '',
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(metadata),
      }
    );

    if (!response.ok) {
      return NextResponse.json(
        { error: 'Failed to search by metadata' },
        { status: response.status }
      );
    }

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    return NextResponse.json(
      { error: 'Failed to search by metadata' },
      { status: 500 }
    );
  }
}
