import { NextRequest, NextResponse } from 'next/server';

export async function GET(
  request: NextRequest,
  { params }: { params: { id: string } }
) {
  try {
    const token = request.headers.get('authorization');
    const { id } = params;
    const baseUrl = `${request.nextUrl.protocol}//${request.nextUrl.host}`;

    // Fetch the hierarchy data to extract manager information
    const response = await fetch(`${baseUrl}/api/hierarchies/employee/${id}`, {
      headers: {
        'Authorization': token || '',
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      return NextResponse.json(
        { error: 'Failed to fetch manager information' },
        { status: response.status }
      );
    }

    const data = await response.json();

    // Extract manager from hierarchy data
    const manager = data?.manager || null;

    return NextResponse.json(manager);
  } catch (error) {
    console.error('Error fetching manager information:', error);
    return NextResponse.json(
      { error: error instanceof Error ? error.message : 'Internal server error' },
      { status: 500 }
    );
  }
}
