import { NextRequest, NextResponse } from 'next/server';

const GATEWAY_URL = process.env.GATEWAY_URL || 'http://localhost:8080/api/v1';

// For development, we need to get a service token or bypass auth
// Option: Use a service account token or internal API key
const getServiceToken = () => {
  // TODO: Get service token from environment or Keycloak
  // For now, try to forward the user's token
  return null;
};

export async function GET(
  request: NextRequest,
  { params }: { params: { code: string } }
) {
  try {
    const { code } = params;

    console.log(`[DEV] Fetching states for workflow ${code} via workflow-service`);

    // Get auth token from request
    const authHeader = request.headers.get('authorization');
    const serviceToken = getServiceToken();

    const headers: HeadersInit = {
      'Content-Type': 'application/json',
    };

    // Try user token first, then service token
    if (authHeader) {
      headers['Authorization'] = authHeader;
    } else if (serviceToken) {
      headers['Authorization'] = `Bearer ${serviceToken}`;
    }

    const response = await fetch(
      `${GATEWAY_URL}/workflow/processes/${code}/states`,
      {
        method: 'GET',
        headers,
      }
    );

    if (!response.ok) {
      const errorText = await response.text();
      console.error(`[DEV] workflow-service returned ${response.status}:`, errorText);

      // For development, return empty array instead of failing
      if (response.status === 401) {
        console.warn('[DEV] Auth failed, returning empty array for development');
        return NextResponse.json([]);
      }

      return NextResponse.json(
        { error: errorText || 'Failed to fetch states' },
        { status: response.status }
      );
    }

    const data = await response.json();
    console.log(`[DEV] Loaded ${Array.isArray(data) ? data.length : 0} states from workflow-service`);

    return NextResponse.json(data);
  } catch (error) {
    console.error('[DEV] Error calling workflow-service:', error);
    // For development, return empty array instead of failing
    console.warn('[DEV] Returning empty array due to error');
    return NextResponse.json([]);
  }
}

export async function POST(
  request: NextRequest,
  { params }: { params: { code: string } }
) {
  try {
    const { code } = params;
    const body = await request.json();

    console.log(`[DEV] Creating state in workflow ${code} via workflow-service:`, body.name);

    // Get auth token
    const authHeader = request.headers.get('authorization');
    const serviceToken = getServiceToken();

    // Transform frontend field names to backend field names
    const transformedBody = {
      stateCode: body.code,
      stateName: body.name,
      description: body.description,
      stateOrder: body.order,
      stateType: body.isInitial ? 'START' : body.isFinal ? 'FINAL' : 'NORMAL',
      isFinalState: body.isFinal || false,
      requiresApproval: body.requiresApproval || false,
      allowSkip: body.allowSkip || false,
      expectedDurationHours: body.expectedDurationHours,
      configuration: body.configuration,
    };

    const headers: HeadersInit = {
      'Content-Type': 'application/json',
    };

    if (authHeader) {
      headers['Authorization'] = authHeader;
    } else if (serviceToken) {
      headers['Authorization'] = `Bearer ${serviceToken}`;
    }

    const response = await fetch(
      `${GATEWAY_URL}/workflow/processes/${code}/states`,
      {
        method: 'POST',
        headers,
        body: JSON.stringify(transformedBody),
      }
    );

    if (!response.ok) {
      const errorText = await response.text();
      console.error(`[DEV] Failed to create state via workflow-service:`, errorText);
      return NextResponse.json(
        { error: errorText || 'Failed to create state' },
        { status: response.status }
      );
    }

    const data = await response.json();
    console.log(`[DEV] State created successfully via workflow-service`);
    return NextResponse.json(data, { status: 201 });
  } catch (error) {
    console.error('[DEV] Error creating state:', error);
    return NextResponse.json(
      { error: error instanceof Error ? error.message : 'Failed to create state' },
      { status: 500 }
    );
  }
}
