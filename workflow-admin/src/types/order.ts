export type OrderStatus =
  | 'DRAFT'
  | 'PENDING'
  | 'CONFIRMED'
  | 'IN_PRODUCTION'
  | 'READY_FOR_DELIVERY'
  | 'DELIVERED'
  | 'INVOICED'
  | 'CANCELLED'
  | 'ON_HOLD';

export type OrderType =
  | 'VN'
  | 'VO'
  | 'EVO';

export type PaymentStatus =
  | 'PENDING'
  | 'DEPOSIT_RECEIVED'
  | 'PARTIALLY_PAID'
  | 'PAID'
  | 'REFUNDED';

export interface Customer {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  address?: string;
  city?: string;
  state?: string;
  zipCode?: string;
}

export interface Vehicle {
  id: number;
  vin?: string;
  make: string;
  model: string;
  year: number;
  trim?: string;
  color?: string;
  mileage?: number;
  stockNumber?: string;
  imageUrl?: string;
  basePrice: number;
}

export interface OrderOption {
  id: number;
  code: string;
  name: string;
  description?: string;
  price: number;
  category: string;
}

export interface OrderDocument {
  id: number;
  name: string;
  type: string;
  url: string;
  uploadedAt: string;
  uploadedBy: string;
}

export interface Commission {
  id: number;
  salesPersonId: number;
  salesPersonName: string;
  amount: number;
  percentage: number;
  type: string;
}

export interface Payment {
  id: number;
  amount: number;
  method: string;
  status: PaymentStatus;
  date: string;
  reference?: string;
}

export interface OrderHistoryEntry {
  id: number;
  action: string;
  description: string;
  performedBy: string;
  performedAt: string;
  oldValue?: any;
  newValue?: any;
}

export interface Order {
  id: number;
  orderNumber: string;
  status: OrderStatus;
  type?: OrderType;
  customerId: number;
  customer?: Customer;
  vehicleId: number;
  vehicle?: Vehicle;
  businessUnitId: number;
  businessUnitName?: string;
  salesPersonId: number;
  salesPersonName?: string;

  // Pricing
  basePrice: number;
  optionsTotal: number;
  taxAmount: number;
  fees: number;
  discount: number;
  tradeInValue: number;
  totalPrice: number;

  // Options and accessories
  options?: OrderOption[];

  // Payments
  depositAmount: number;
  amountPaid: number;
  amountDue: number;
  paymentStatus: PaymentStatus;
  payments?: Payment[];

  // Commission
  commissions?: Commission[];

  // Documents
  documents?: OrderDocument[];

  // Workflow
  workflowInstanceId?: number;
  workflowStatus?: string;

  // Timeline
  history?: OrderHistoryEntry[];

  // Metadata
  notes?: string;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
  updatedBy: string;

  // Dates
  estimatedDeliveryDate?: string;
  actualDeliveryDate?: string;
}

export interface OrderListItem {
  id: number;
  orderNumber: string;
  status: OrderStatus;
  type: OrderType;
  customerName: string;
  vehicleName: string;
  vin?: string;
  totalPrice: number;
  amountDue: number;
  paymentStatus: PaymentStatus;
  businessUnitName: string;
  salesPersonName: string;
  createdAt: string;
  estimatedDeliveryDate?: string;
}

export interface CreateOrderRequest {
  type?: OrderType;
  orderType?: OrderType;
  customerId: number;
  vehicleId?: number;
  businessUnitId: number;
  salesPersonId?: number;
  salespersonId?: number;
  basePrice?: number;
  optionIds?: number[];
  depositAmount?: number;
  discount?: number;
  tradeInValue?: number;
  notes?: string;
  estimatedDeliveryDate?: string;
}

export interface UpdateOrderRequest {
  status?: OrderStatus;
  optionIds?: number[];
  depositAmount?: number;
  discount?: number;
  tradeInValue?: number;
  notes?: string;
  estimatedDeliveryDate?: string;
}

export interface OrderFilters {
  status?: OrderStatus[];
  type?: OrderType[];
  businessUnitId?: number;
  salesPersonId?: number;
  customerId?: number;
  dateFrom?: string;
  dateTo?: string;
  search?: string;
  page?: number;
  limit?: number;
  sortBy?: string;
  sortOrder?: 'asc' | 'desc';
}

export interface OrdersResponse {
  orders: OrderListItem[];
  total: number;
  page: number;
  limit: number;
  totalPages: number;
}
