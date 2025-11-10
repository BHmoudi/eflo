import { NextRequest, NextResponse } from 'next/server';

const GATEWAY_URL = 'http://localhost:8080/api/v1';

export async function GET(
  request: NextRequest,
  { params }: { params: { managerId: string } }
) {
  try {
    const token = request.headers.get('authorization');
    const { managerId } = params;

    const response = await fetch(
      `${GATEWAY_URL}/hierarchies/manager/${managerId}/direct-reports`,
      {
        headers: {
          'Authorization': token || '',
          'Content-Type': 'application/json',
        },
      }
    );

    if (!response.ok) {
      const errorText = await response.text();
      return NextResponse.json(
        { error: errorText || 'Failed to fetch direct reports' },
        { status: response.status }
      );
    }

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    return NextResponse.json(
      { error: error instanceof Error ? error.message : 'Failed to fetch direct reports' },
      { status: 500 }
    );
  }
}
