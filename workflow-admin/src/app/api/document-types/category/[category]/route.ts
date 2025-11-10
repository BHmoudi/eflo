import { NextRequest, NextResponse } from 'next/server';

const GATEWAY_URL = 'http://localhost:8080/api/v1';

export async function GET(
  request: NextRequest,
  { params }: { params: { category: string } }
) {
  try {
    const token = request.headers.get('authorization');

    const response = await fetch(
      `${GATEWAY_URL}/document-types/category/${params.category}`,
      {
        headers: {
          'Authorization': token || '',
          'Content-Type': 'application/json',
        },
      }
    );

    if (!response.ok) {
      return NextResponse.json(
        { error: 'Failed to fetch document types by category' },
        { status: response.status }
      );
    }

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    return NextResponse.json(
      { error: 'Failed to fetch document types by category' },
      { status: 500 }
    );
  }
}
