import { NextRequest, NextResponse } from 'next/server';

const GATEWAY_URL = 'http://localhost:8080/api/v1';

export async function POST(
  request: NextRequest,
  { params }: { params: { id: string } }
) {
  try {
    const token = request.headers.get('authorization');
    const { searchParams } = new URL(request.url);
    const newTypeCode = searchParams.get('newTypeCode');
    const newTypeName = searchParams.get('newTypeName');

    if (!newTypeCode || !newTypeName) {
      return NextResponse.json(
        { error: 'newTypeCode and newTypeName are required' },
        { status: 400 }
      );
    }

    const response = await fetch(
      `${GATEWAY_URL}/document-types/${params.id}/duplicate?newTypeCode=${newTypeCode}&newTypeName=${newTypeName}`,
      {
        method: 'POST',
        headers: {
          'Authorization': token || '',
          'Content-Type': 'application/json',
        },
      }
    );

    if (!response.ok) {
      const error = await response.json();
      return NextResponse.json(
        { error: error.message || 'Failed to duplicate document type' },
        { status: response.status }
      );
    }

    const data = await response.json();
    return NextResponse.json(data, { status: 201 });
  } catch (error) {
    return NextResponse.json(
      { error: 'Failed to duplicate document type' },
      { status: 500 }
    );
  }
}
