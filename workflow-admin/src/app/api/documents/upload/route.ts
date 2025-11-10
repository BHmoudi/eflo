import { NextRequest, NextResponse } from 'next/server';

const GATEWAY_URL = 'http://localhost:8080/api/v1';

export async function POST(request: NextRequest) {
  try {
    const token = request.headers.get('authorization');
    const formData = await request.formData();

    const response = await fetch(`${GATEWAY_URL}/documents/upload`, {
      method: 'POST',
      headers: {
        'Authorization': token || '',
      },
      body: formData,
    });

    if (!response.ok) {
      const error = await response.json();
      return NextResponse.json(
        { error: error.message || 'Failed to upload document' },
        { status: response.status }
      );
    }

    const data = await response.json();
    return NextResponse.json(data, { status: 201 });
  } catch (error) {
    console.error('Upload error:', error);
    return NextResponse.json(
      { error: 'Failed to upload document' },
      { status: 500 }
    );
  }
}
