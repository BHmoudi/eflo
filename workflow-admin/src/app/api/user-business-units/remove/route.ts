import { NextRequest, NextResponse } from 'next/server';

const GATEWAY_URL = 'http://localhost:8080/api/v1';

export async function DELETE(request: NextRequest) {
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
      `${GATEWAY_URL}/user-business-units/remove?userId=${userId}&businessUnitId=${businessUnitId}`,
      {
        method: 'DELETE',
        headers: {
          'Authorization': token || '',
          'Content-Type': 'application/json',
        },
      }
    );

    if (!response.ok) {
      const errorText = await response.text();
      return NextResponse.json(
        { error: errorText || 'Failed to remove user from business unit' },
        { status: response.status }
      );
    }

    return NextResponse.json({ success: true });
  } catch (error) {
    return NextResponse.json(
      { error: error instanceof Error ? error.message : 'Failed to remove user from business unit' },
      { status: 500 }
    );
  }
}
