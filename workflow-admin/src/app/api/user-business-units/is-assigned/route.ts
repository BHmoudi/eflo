import { NextRequest, NextResponse } from 'next/server';

const GATEWAY_URL = 'http://localhost:8080/api/v1';

export async function GET(request: NextRequest) {
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
      `${GATEWAY_URL}/user-business-units/is-assigned?userId=${userId}&businessUnitId=${businessUnitId}`,
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
        { error: errorText || 'Failed to check user assignment' },
        { status: response.status }
      );
    }

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    return NextResponse.json(
      { error: error instanceof Error ? error.message : 'Failed to check user assignment' },
      { status: 500 }
    );
  }
}
