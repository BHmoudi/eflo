import { NextRequest, NextResponse } from 'next/server';

// Mock orders endpoint for development
export async function GET(request: NextRequest) {
  const searchParams = request.nextUrl.searchParams;
  const page = parseInt(searchParams.get('page') || '0');
  const limit = parseInt(searchParams.get('limit') || '20');

  // Mock orders matching the database data
  const allOrders = [
    {
      id: 38,
      orderNumber: '25080111185912-FULL',
      type: 'VN',
      status: 'DRAFT',
      customerName: 'John Doe',
      vehicleName: '2024 RENAULT CAPTUR',
      vin: 'VF1RJEF0066123456',
      totalPrice: 24750.00,
      amountDue: 24750.00,
      paymentStatus: 'PENDING',
      businessUnitName: 'Paris North',
      salesPersonName: 'Marie Dubois',
      createdAt: '2025-10-06T03:35:26.435Z',
      estimatedDeliveryDate: null,
    },
    {
      id: 35,
      orderNumber: '25080ddd111185912',
      type: 'VN',
      status: 'DRAFT',
      customerName: 'Jane Smith',
      vehicleName: '2024 RENAULT CAPTUR',
      vin: 'VF1RJEF0066123457',
      totalPrice: 24750.00,
      amountDue: 24750.00,
      paymentStatus: 'PENDING',
      businessUnitName: 'Lyon Center',
      salesPersonName: 'Pierre Martin',
      createdAt: '2025-10-05T19:57:10.885Z',
      estimatedDeliveryDate: null,
    },
    {
      id: 32,
      orderNumber: 'TEST-1759694163',
      type: 'VN',
      status: 'DRAFT',
      customerName: 'Test Customer',
      vehicleName: '2024 RENAULT Test Model',
      vin: null,
      totalPrice: 0.00,
      amountDue: 0.00,
      paymentStatus: 'PENDING',
      businessUnitName: 'Test Unit',
      salesPersonName: 'Test Salesperson',
      createdAt: '2025-10-05T19:56:03.233Z',
      estimatedDeliveryDate: null,
    },
    {
      id: 29,
      orderNumber: 'TEST-API-1759693383',
      type: 'VN',
      status: 'DRAFT',
      customerName: 'API Test User',
      vehicleName: '2024 RENAULT MEGANE E-TECH',
      vin: null,
      totalPrice: 0.00,
      amountDue: 0.00,
      paymentStatus: 'PENDING',
      businessUnitName: 'API Test Unit',
      salesPersonName: 'API Salesperson',
      createdAt: '2025-10-05T19:43:03.219Z',
      estimatedDeliveryDate: null,
    },
    {
      id: 28,
      orderNumber: 'TEST-1759693245',
      type: 'VN',
      status: 'DRAFT',
      customerName: 'Another Test Customer',
      vehicleName: '2024 RENAULT Test Model',
      vin: null,
      totalPrice: 0.00,
      amountDue: 0.00,
      paymentStatus: 'PENDING',
      businessUnitName: 'Test Unit 2',
      salesPersonName: 'Test Salesperson 2',
      createdAt: '2025-10-05T19:40:44.902Z',
      estimatedDeliveryDate: null,
    },
  ];

  const total = allOrders.length;
  const start = page * limit;
  const end = start + limit;
  const orders = allOrders.slice(start, end);
  const totalPages = Math.ceil(total / limit);

  return NextResponse.json({
    content: orders,
    totalElements: total,
    totalPages: totalPages,
    number: page,
    size: limit,
  });
}
