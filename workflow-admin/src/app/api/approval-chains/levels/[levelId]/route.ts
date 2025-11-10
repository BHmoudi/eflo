import { NextRequest, NextResponse } from 'next/server';

const GATEWAY_URL = 'http://localhost:8080/api/v1';

export async function DELETE(
  request: NextRequest,
  { params }: { params: { levelId: string } }
) {
  try {
    const token = request.headers.get('authorization');
    const { levelId } = params;

    const response = await fetch(`${GATEWAY_URL}/approval-chains/levels/${levelId}`, {
      method: 'DELETE',
      headers: {
        'Authorization': token || '',
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      const errorText = await response.text();
      return NextResponse.json({ error: errorText || 'Failed to delete approval level' }, { status: response.status });
    }

    return NextResponse.json({ success: true });
  } catch (error) {
    return NextResponse.json({ error: error instanceof Error ? error.message : 'Failed to delete approval level' }, { status: 500 });
  }
}
