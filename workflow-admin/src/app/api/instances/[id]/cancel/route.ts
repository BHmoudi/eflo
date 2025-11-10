import { NextRequest, NextResponse } from 'next/server';

const GATEWAY_URL = 'http://localhost:8080/api/v1';

export async function POST(
  request: NextRequest,
  { params }: { params: { id: string } }
) {
  try {
    const token = request.headers.get('authorization');
    const { id } = params;
    const { searchParams } = new URL(request.url);
    const reason = searchParams.get('reason');

    let url = `${GATEWAY_URL}/workflow/instances/${id}/cancel`;
    if (reason) {
      url += `?reason=${encodeURIComponent(reason)}`;
    }

    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Authorization': token || '',
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      const errorText = await response.text();
      return NextResponse.json({ error: errorText || 'Failed to cancel instance' }, { status: response.status });
    }

    return NextResponse.json({ success: true });
  } catch (error) {
    return NextResponse.json({ error: error instanceof Error ? error.message : 'Failed to cancel instance' }, { status: 500 });
  }
}
