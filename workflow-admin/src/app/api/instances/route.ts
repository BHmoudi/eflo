import { NextRequest, NextResponse } from 'next/server';

const GATEWAY_URL = process.env.GATEWAY_URL || 'http://localhost:8080/api/v1';
const USE_MOCK_DATA = process.env.USE_MOCK_DATA === 'true' || process.env.NODE_ENV === 'development';

// Mock data for development when backend is not available
const MOCK_INSTANCES = [
  {
    id: 1,
    workflowCode: 'PURCHASE_ORDER',
    status: 'ACTIVE',
    createdAt: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(),
    updatedAt: new Date().toISOString(),
  },
  {
    id: 2,
    workflowCode: 'EXPENSE_APPROVAL',
    status: 'COMPLETED',
    createdAt: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString(),
    updatedAt: new Date(Date.now() - 1 * 60 * 60 * 1000).toISOString(),
  },
  {
    id: 3,
    workflowCode: 'INVOICE_PROCESSING',
    status: 'ACTIVE',
    createdAt: new Date(Date.now() - 5 * 60 * 60 * 1000).toISOString(),
    updatedAt: new Date().toISOString(),
  },
];

export async function GET(request: NextRequest) {
  try {
    const { searchParams } = new URL(request.url);
    const status = searchParams.get('status');

    // If using mock data or in development mode without backend
    if (USE_MOCK_DATA) {
      console.log('[DEV] Using mock instances data');
      let filteredInstances = MOCK_INSTANCES;

      if (status) {
        filteredInstances = MOCK_INSTANCES.filter(
          (instance) => instance.status === status.toUpperCase()
        );
      }

      return NextResponse.json(filteredInstances);
    }

    const token = request.headers.get('authorization');
    let url = `${GATEWAY_URL}/workflow/instances`;

    // Add status filter if provided
    if (status) {
      url = `${GATEWAY_URL}/workflow/instances/status/${status}`;
    }

    const response = await fetch(url, {
      headers: {
        'Authorization': token || '',
        'Content-Type': 'application/json',
      },
      signal: AbortSignal.timeout(5000), // 5 second timeout
    });

    if (!response.ok) {
      const errorText = await response.text();
      console.error(`Failed to fetch instances: ${errorText}`);

      // Fall back to mock data in development if backend fails
      if (process.env.NODE_ENV === 'development') {
        console.log('[DEV] Backend failed, falling back to mock data');
        let filteredInstances = MOCK_INSTANCES;

        if (status) {
          filteredInstances = MOCK_INSTANCES.filter(
            (instance) => instance.status === status.toUpperCase()
          );
        }

        return NextResponse.json(filteredInstances);
      }

      return NextResponse.json(
        { error: errorText || 'Failed to fetch instances' },
        { status: response.status }
      );
    }

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    console.error('Error fetching instances:', error);

    // Fall back to mock data in development if there's a network error
    if (process.env.NODE_ENV === 'development') {
      console.log('[DEV] Error occurred, falling back to mock data');
      const { searchParams } = new URL(request.url);
      const status = searchParams.get('status');
      let filteredInstances = MOCK_INSTANCES;

      if (status) {
        filteredInstances = MOCK_INSTANCES.filter(
          (instance) => instance.status === status.toUpperCase()
        );
      }

      return NextResponse.json(filteredInstances);
    }

    return NextResponse.json(
      { error: error instanceof Error ? error.message : 'Failed to fetch instances' },
      { status: 500 }
    );
  }
}

export async function POST(request: NextRequest) {
  try {
    const token = request.headers.get('authorization');
    const body = await request.json();

    const response = await fetch(`${GATEWAY_URL}/workflow/instances`, {
      method: 'POST',
      headers: {
        'Authorization': token || '',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
    });

    if (!response.ok) {
      const errorText = await response.text();
      return NextResponse.json({ error: errorText || 'Failed to create instance' }, { status: response.status });
    }

    const data = await response.json();
    return NextResponse.json(data, { status: 201 });
  } catch (error) {
    return NextResponse.json({ error: error instanceof Error ? error.message : 'Failed to create instance' }, { status: 500 });
  }
}
