import { NextRequest, NextResponse } from 'next/server';

const GATEWAY_URL = 'http://localhost:8080/api/v1';

export async function GET(request: NextRequest) {
  try {
    const token = request.headers.get('authorization');
    const searchParams = request.nextUrl.searchParams;
    const role = searchParams.get('role');
    const page = searchParams.get('page') || '0';
    const size = searchParams.get('size') || '20';

    let url = `${GATEWAY_URL}/users`;
    if (role) {
      url = `${GATEWAY_URL}/users/by-role?role=${encodeURIComponent(role)}`;
    } else {
      url = `${GATEWAY_URL}/users?page=${page}&size=${size}`;
    }

    const response = await fetch(url, {
      headers: {
        'Authorization': token || '',
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      const errorText = await response.text();
      return NextResponse.json({ error: errorText || 'Failed to fetch users' }, { status: response.status });
    }

    const data = await response.json();

    // Transform backend PagedUserResponse to frontend PaginatedUsers format
    // Backend: { users, currentPage, totalPages, totalElements, pageSize, hasNext, hasPrevious }
    // Frontend: { content, number, totalPages, totalElements, size }
    if (data.users) {
      const transformedData = {
        content: data.users.map((user: any) => ({
          id: user.id,
          email: user.email,
          firstName: user.firstName,
          lastName: user.lastName,
          role: user.jobTitle || 'N/A', // Use job title as role placeholder since roles are in separate table
          phone: user.phoneNumber || user.mobileNumber || '',
          active: user.isActive !== undefined ? user.isActive : true,
          createdAt: user.createdAt,
          updatedAt: user.updatedAt,
        })),
        number: data.currentPage,
        totalPages: data.totalPages,
        totalElements: data.totalElements,
        size: data.pageSize,
      };
      return NextResponse.json(transformedData);
    }

    return NextResponse.json(data);
  } catch (error) {
    return NextResponse.json({ error: error instanceof Error ? error.message : 'Failed to fetch users' }, { status: 500 });
  }
}

export async function POST(request: NextRequest) {
  try {
    const token = request.headers.get('authorization');
    const body = await request.json();

    const response = await fetch(`${GATEWAY_URL}/users`, {
      method: 'POST',
      headers: {
        'Authorization': token || '',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
    });

    if (!response.ok) {
      const errorText = await response.text();
      return NextResponse.json({ error: errorText || 'Failed to create user' }, { status: response.status });
    }

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    return NextResponse.json({ error: error instanceof Error ? error.message : 'Failed to create user' }, { status: 500 });
  }
}
