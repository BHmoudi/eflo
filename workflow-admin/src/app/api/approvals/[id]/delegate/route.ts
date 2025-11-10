import { NextRequest, NextResponse } from 'next/server';

const GATEWAY_URL = 'http://localhost:8080/api/v1';

export async function POST(
  request: NextRequest,
  { params }: { params: { id: string } }
) {
  try {
    const token = request.headers.get('authorization');
    const userId = request.headers.get('x-user-id');
    const { id } = params;
    const body = await request.json();

    const response = await fetch(`${GATEWAY_URL}/approvals/${id}/delegate`, {
      method: 'POST',
      headers: {
        'Authorization': token || '',
        'X-User-Id': userId || '1',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
    });

    if (!response.ok) {
      const errorText = await response.text();
      return NextResponse.json({ error: errorText || 'Failed to delegate' }, { status: response.status });
    }

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    return NextResponse.json({ error: error instanceof Error ? error.message : 'Failed to delegate' }, { status: 500 });
  }
}
