import { NextRequest, NextResponse } from 'next/server';

const GATEWAY_URL = 'http://localhost:8080/api/v1';

export async function PUT(
  request: NextRequest,
  { params }: { params: { code: string; stateCode: string } }
) {
  try {
    const token = request.headers.get('authorization');
    const { code, stateCode } = params;
    const body = await request.json();

    // Transform frontend field names to backend field names
    const transformedBody = {
      stateCode: body.code || stateCode,
      stateName: body.name,
      description: body.description,
      stateOrder: body.order,
      stateType: body.isInitial ? 'START' : (body.isFinal ? 'FINAL' : 'NORMAL'),
      isFinalState: body.isFinal || false,
      requiresApproval: body.requiresApproval || false,
      allowSkip: body.allowSkip || false,
      expectedDurationHours: body.expectedDurationHours,
      configuration: body.configuration,
    };

    const response = await fetch(`${GATEWAY_URL}/workflow/processes/${code}/states/${stateCode}`, {
      method: 'PUT',
      headers: {
        'Authorization': token || '',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(transformedBody),
    });

    if (!response.ok) {
      const errorText = await response.text();
      return NextResponse.json({ error: errorText || 'Failed to update state' }, { status: response.status });
    }

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    return NextResponse.json({ error: error instanceof Error ? error.message : 'Failed to update state' }, { status: 500 });
  }
}

export async function DELETE(
  request: NextRequest,
  { params }: { params: { code: string; stateCode: string } }
) {
  try {
    const token = request.headers.get('authorization');
    const { code, stateCode } = params;

    const response = await fetch(`${GATEWAY_URL}/workflow/processes/${code}/states/${stateCode}`, {
      method: 'DELETE',
      headers: {
        'Authorization': token || '',
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      const errorText = await response.text();
      return NextResponse.json({ error: errorText || 'Failed to delete state' }, { status: response.status });
    }

    return NextResponse.json({ success: true });
  } catch (error) {
    return NextResponse.json({ error: error instanceof Error ? error.message : 'Failed to delete state' }, { status: 500 });
  }
}
