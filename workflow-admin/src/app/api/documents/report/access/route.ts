import { NextRequest, NextResponse } from 'next/server';

const GATEWAY_URL = 'http://localhost:8080/api/v1';

export async function GET(request: NextRequest) {
  try {
    const token = request.headers.get('authorization');
    const { searchParams } = new URL(request.url);

    const params = new URLSearchParams();
    if (searchParams.get('fromDate')) params.append('fromDate', searchParams.get('fromDate')!);
    if (searchParams.get('toDate')) params.append('toDate', searchParams.get('toDate')!);
    if (searchParams.get('documentId')) params.append('documentId', searchParams.get('documentId')!);

    const queryString = params.toString();
    const url = `${GATEWAY_URL}/documents/report/access${queryString ? `?${queryString}` : ''}`;

    const response = await fetch(url, {
      headers: {
        'Authorization': token || '',
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      return NextResponse.json(
        { error: 'Failed to fetch access report' },
        { status: response.status }
      );
    }

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    return NextResponse.json(
      { error: 'Failed to fetch access report' },
      { status: 500 }
    );
  }
}
