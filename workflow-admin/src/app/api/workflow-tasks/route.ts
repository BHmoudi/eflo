import { NextRequest, NextResponse } from 'next/server';

const GATEWAY_URL = 'http://localhost:8080/api/v1';

export async function GET(request: NextRequest) {
  try {
    const token = request.headers.get('authorization');
    const { searchParams } = new URL(request.url);

    // Build query parameters for filtering
    const params = new URLSearchParams();

    // Support common filters
    const status = searchParams.get('status');
    const assignedToUserId = searchParams.get('assignedToUserId');
    const instanceId = searchParams.get('instanceId');
    const processCode = searchParams.get('processCode');
    const stateCode = searchParams.get('stateCode');
    const taskType = searchParams.get('taskType');
    const page = searchParams.get('page');
    const size = searchParams.get('size');

    if (status) params.append('status', status);
    if (assignedToUserId) params.append('assignedToUserId', assignedToUserId);
    if (instanceId) params.append('instanceId', instanceId);
    if (processCode) params.append('processCode', processCode);
    if (stateCode) params.append('stateCode', stateCode);
    if (taskType) params.append('taskType', taskType);
    if (page) params.append('page', page);
    if (size) params.append('size', size);

    const queryString = params.toString();
    const url = `${GATEWAY_URL}/workflow/tasks${queryString ? `?${queryString}` : ''}`;

    const response = await fetch(url, {
      headers: {
        'Authorization': token || '',
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      const errorText = await response.text();
      return NextResponse.json({ error: errorText || 'Failed to fetch workflow tasks' }, { status: response.status });
    }

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    return NextResponse.json({ error: error instanceof Error ? error.message : 'Failed to fetch workflow tasks' }, { status: 500 });
  }
}
