import { NextRequest, NextResponse } from 'next/server';

const GATEWAY_URL = 'http://localhost:8080/api/v1';

export async function GET(
  request: NextRequest,
  { params }: { params: { code: string } }
) {
  try {
    const token = request.headers.get('authorization');
    const { code } = params;

    const response = await fetch(`${GATEWAY_URL}/workflow/processes/${code}/transitions`, {
      method: 'GET',
      headers: {
        'Authorization': token || '',
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      const errorText = await response.text();
      return NextResponse.json({ error: errorText || 'Failed to fetch transitions' }, { status: response.status });
    }

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    return NextResponse.json({ error: error instanceof Error ? error.message : 'Failed to fetch transitions' }, { status: 500 });
  }
}

export async function POST(
  request: NextRequest,
  { params }: { params: { code: string } }
) {
  try {
    const token = request.headers.get('authorization');
    const { code } = params;
    const body = await request.json();

    // Transform frontend field names to backend field names
    const transformedBody = {
      transitionName: body.name || 'Transition',
      fromStateCode: body.fromStateCode,
      toStateCode: body.toStateCode,
      condition: body.condition,
      roleCode: body.roleCode,
      description: body.description,
    };

    const response = await fetch(`${GATEWAY_URL}/workflow/processes/${code}/transitions`, {
      method: 'POST',
      headers: {
        'Authorization': token || '',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(transformedBody),
    });

    if (!response.ok) {
      const errorText = await response.text();
      return NextResponse.json({ error: errorText || 'Failed to create transition' }, { status: response.status });
    }

    const data = await response.json();
    return NextResponse.json(data, { status: 201 });
  } catch (error) {
    return NextResponse.json({ error: error instanceof Error ? error.message : 'Failed to create transition' }, { status: 500 });
  }
}
