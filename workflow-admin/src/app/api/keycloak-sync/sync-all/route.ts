import { NextRequest, NextResponse } from 'next/server';

const GATEWAY_URL = process.env.GATEWAY_URL || 'http://localhost:8080';

export async function POST(request: NextRequest) {
  try {
    // Get auth token from request headers
    const authToken = request.headers.get('Authorization');

    // Forward request to backend
    const response = await fetch(`${GATEWAY_URL}/api/v1/keycloak-sync/sync-all`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(authToken && { Authorization: authToken }),
      },
    });

    const data = await response.json();

    if (!response.ok) {
      return NextResponse.json(
        { message: data.message || 'Failed to sync users from Keycloak', error: data.error },
        { status: response.status }
      );
    }

    return NextResponse.json(data, { status: 200 });
  } catch (error: any) {
    console.error('Error syncing all users from Keycloak:', error);
    return NextResponse.json(
      { message: 'Internal server error', error: error.message },
      { status: 500 }
    );
  }
}
