import { NextResponse } from 'next/server';

export async function POST() {
  try {
    const keycloakUrl = process.env.NEXT_PUBLIC_KEYCLOAK_URL || 'http://localhost:8180';
    const clientSecret = process.env.KEYCLOAK_CLIENT_SECRET;

    if (!clientSecret) {
      return NextResponse.json(
        { error: 'Server configuration error', details: 'Client secret not configured' },
        { status: 500 }
      );
    }

    // Server-side call to Keycloak (no CORS)
    const response = await fetch(`${keycloakUrl}/realms/eflo/protocol/openid-connect/token`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body: new URLSearchParams({
        client_id: 'eflo-microservices',
        client_secret: clientSecret,
        grant_type: 'client_credentials',
      }),
    });

    if (!response.ok) {
      const errorText = await response.text();
      let errorData;
      try {
        errorData = JSON.parse(errorText);
      } catch {
        errorData = { error: 'Authentication failed', details: errorText };
      }
      return NextResponse.json(errorData, { status: response.status });
    }

    const data = await response.json();

    // Set secure HTTP-only cookie for middleware authentication
    const res = NextResponse.json(data);
    res.cookies.set('authToken', data.access_token, {
      httpOnly: true,
      secure: process.env.NODE_ENV === 'production',
      sameSite: 'lax',
      maxAge: 60 * 30, // 30 minutes
      path: '/',
    });

    if (data.refresh_token) {
      res.cookies.set('refreshToken', data.refresh_token, {
        httpOnly: true,
        secure: process.env.NODE_ENV === 'production',
        sameSite: 'lax',
        maxAge: 60 * 30, // 30 minutes
        path: '/',
      });
    }

    return res;
  } catch (error) {
    return NextResponse.json(
      { error: 'Failed to authenticate', details: error instanceof Error ? error.message : 'Unknown error' },
      { status: 500 }
    );
  }
}
