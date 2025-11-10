import { NextRequest, NextResponse } from 'next/server';

// Call through internal gateway
const GATEWAY_URL = 'http://localhost:8080/api/v1';

export async function GET(request: NextRequest) {
  try {
    const token = request.headers.get('authorization');

    console.log('Fetching workflows from:', `${GATEWAY_URL}/workflow/processes`);

    const response = await fetch(`${GATEWAY_URL}/workflow/processes`, {
      headers: {
        'Authorization': token || '',
        'Content-Type': 'application/json',
      },
    });

    console.log('Workflows response status:', response.status);

    if (!response.ok) {
      const errorText = await response.text();
      console.error('Workflows error:', response.status, errorText);
      return NextResponse.json({ error: errorText || 'Failed to fetch workflows' }, { status: response.status });
    }

    const text = await response.text();
    if (!text) {
      return NextResponse.json([], { status: 200 });
    }

    const data = JSON.parse(text);
    return NextResponse.json(data, { status: 200 });
  } catch (error) {
    console.error('Workflow API error:', error);
    return NextResponse.json({ error: error instanceof Error ? error.message : 'Failed to fetch workflows' }, { status: 500 });
  }
}

export async function POST(request: NextRequest) {
  try {
    const token = request.headers.get('authorization');
    const body = await request.json();

    console.log('Creating workflow:', body);

    // Transform frontend format to backend format
    const backendRequest = {
      processCode: body.code,
      processName: body.name,
      description: body.description,
      orderType: body.orderType || 'VN',
      maxDurationDays: body.maxDurationDays,
      autoProgressEnabled: body.autoProgressEnabled || false,
      parallelExecutionAllowed: body.parallelExecutionAllowed || false,
      configuration: body.configuration,
    };

    console.log('Backend request:', backendRequest);

    const response = await fetch(`${GATEWAY_URL}/workflow/processes`, {
      method: 'POST',
      headers: {
        'Authorization': token || '',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(backendRequest),
    });

    console.log('Create workflow response status:', response.status);

    if (!response.ok) {
      const errorText = await response.text();
      console.error('Create workflow error:', response.status, errorText);
      return NextResponse.json({ error: errorText || 'Failed to create workflow' }, { status: response.status });
    }

    const data = await response.json();
    return NextResponse.json(data, { status: 201 });
  } catch (error) {
    console.error('Create workflow API error:', error);
    return NextResponse.json({ error: error instanceof Error ? error.message : 'Failed to create workflow' }, { status: 500 });
  }
}
