import { NextRequest, NextResponse } from 'next/server';

const GATEWAY_URL = 'http://localhost:8080/api/v1';

export async function POST(request: NextRequest) {
  try {
    const token = request.headers.get('authorization');
    const { searchParams } = new URL(request.url);
    const documentIds = searchParams.get('documentIds');
    const comments = searchParams.get('comments');

    const params = new URLSearchParams();
    if (documentIds) params.append('documentIds', documentIds);
    if (comments) params.append('comments', comments);

    const response = await fetch(
      `${GATEWAY_URL}/documents/validate-batch?${params.toString()}`,
      {
        method: 'POST',
        headers: {
          'Authorization': token || '',
          'Content-Type': 'application/json',
        },
      }
    );

    if (!response.ok) {
      return NextResponse.json(
        { error: 'Failed to batch validate documents' },
        { status: response.status }
      );
    }

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    return NextResponse.json(
      { error: 'Failed to batch validate documents' },
      { status: 500 }
    );
  }
}
