import { NextRequest, NextResponse } from 'next/server';

const GATEWAY_URL = 'http://localhost:8080/api/v1';

export async function GET(
  request: NextRequest,
  { params }: { params: { id: string } }
) {
  try {
    const token = request.headers.get('authorization');
    const { id } = params;
    const { searchParams } = new URL(request.url);

    // Build query string with pagination params
    const queryParams = new URLSearchParams();
    const page = searchParams.get('page');
    const size = searchParams.get('size');
    const sort = searchParams.get('sort');

    if (page) queryParams.append('page', page);
    if (size) queryParams.append('size', size);
    if (sort) queryParams.append('sort', sort);

    const queryString = queryParams.toString();
    const url = `${GATEWAY_URL}/users/${id}/activity-logs${queryString ? `?${queryString}` : ''}`;

    const response = await fetch(url, {
      headers: {
        'Authorization': token || '',
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      return NextResponse.json(
        { error: 'Failed to fetch user activity logs' },
        { status: response.status }
      );
    }

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    console.error('Error fetching user activity logs:', error);
    return NextResponse.json(
      { error: error instanceof Error ? error.message : 'Internal server error' },
      { status: 500 }
    );
  }
}
