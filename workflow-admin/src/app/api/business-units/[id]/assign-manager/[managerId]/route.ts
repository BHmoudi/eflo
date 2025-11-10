import { NextRequest, NextResponse } from 'next/server';

const GATEWAY_URL = 'http://localhost:8080/api/v1';

export async function POST(
  request: NextRequest,
  { params }: { params: { id: string; managerId: string } }
) {
  try {
    const token = request.headers.get('authorization');
    const { id, managerId } = params;

    const response = await fetch(`${GATEWAY_URL}/business-units/${id}/assign-manager/${managerId}`, {
      method: 'POST',
      headers: {
        'Authorization': token || '',
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      const errorText = await response.text();
      return NextResponse.json({ error: errorText || 'Failed to assign manager' }, { status: response.status });
    }

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    return NextResponse.json({ error: error instanceof Error ? error.message : 'Failed to assign manager' }, { status: 500 });
  }
}
