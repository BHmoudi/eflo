import { NextResponse } from 'next/server';

export async function POST() {
  try {
    // Create response
    const response = NextResponse.json({ success: true, message: 'Logged out successfully' });

    // Clear authentication cookies
    response.cookies.delete('authToken');
    response.cookies.delete('refreshToken');

    // Optionally: Call Keycloak logout endpoint to revoke tokens
    // This would require admin credentials or the user's refresh token
    // For now, just clearing cookies is sufficient for client-side logout

    return response;
  } catch (error) {
    console.error('Logout error:', error);
    return NextResponse.json(
      { error: 'Logout failed' },
      { status: 500 }
    );
  }
}
