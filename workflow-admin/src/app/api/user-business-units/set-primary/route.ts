import { NextRequest, NextResponse } from 'next/server';

const GATEWAY_URL = 'http://localhost:8080/api/v1';

export async function PUT(request: NextRequest) {
  try {
    const token = request.headers.get('authorization');
    const searchParams = request.nextUrl.searchParams;
    const userId = searchParams.get('userId');
    const businessUnitId = searchParams.get('businessUnitId');

    if (!userId || !businessUnitId) {
      return NextResponse.json(
        { error: 'userId and businessUnitId are required' },
        { status: 400 }
      );
    }

    const response = await fetch(
      `${GATEWAY_URL}/user-business-units/set-primary?userId=${userId}&businessUnitId=${businessUnitId}`,
      {
        method: 'PUT',
        headers: {
          'Authorization': token || '',
          'Content-Type': 'application/json',
        },
      }
    );

    if (!response.ok) {
      const errorText = await response.text();
      return NextResponse.json(
        { error: errorText || 'Failed to set primary business unit' },
        { status: response.status }
      );
    }

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    return NextResponse.json(
      { error: error instanceof Error ? error.message : 'Failed to set primary business unit' },
      { status: 500 }
    );
  }
}
