import apiClient from './client';

// ========== Enums ==========

export enum OrderType {
  VN = 'VN',   // New Vehicle
  VO = 'VO',   // Used Vehicle
  EVO = 'EVO', // Evolution
}

export enum OrderStatus {
  DRAFT = 'DRAFT',
  PENDING = 'PENDING',
  CONFIRMED = 'CONFIRMED',
  IN_PRODUCTION = 'IN_PRODUCTION',
  READY_FOR_DELIVERY = 'READY_FOR_DELIVERY',
  DELIVERED = 'DELIVERED',
  INVOICED = 'INVOICED',
  CANCELLED = 'CANCELLED',
  ON_HOLD = 'ON_HOLD',
}

export enum DeliveryType {
  PICKUP = 'PICKUP',
  HOME_DELIVERY = 'HOME_DELIVERY',
  DEALER_DELIVERY = 'DEALER_DELIVERY',
}

export enum DeliveryStatus {
  SCHEDULED = 'SCHEDULED',
  CONFIRMED = 'CONFIRMED',
  IN_TRANSIT = 'IN_TRANSIT',
  DELIVERED = 'DELIVERED',
  FAILED = 'FAILED',
  CANCELLED = 'CANCELLED',
}

export enum ApprovalStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
}

// ========== Core Interfaces ==========

export interface Vehicle {
  id?: number;
  vin?: string;
  make?: string;
  model?: string;
  year?: number;
  trim?: string;
  colorExterior?: string;
  colorInterior?: string;
  semiClairModel?: string;
  semiClairVersion?: string;
  co2Level?: number;
  bodyType?: string;
  fuelType?: string;
}

export interface OrderOption {
  id?: number;
  orderId?: number;
  optionCode: string;
  optionName: string;
  optionCategory?: string;
  description?: string;
  price: number;
  cost?: number;
  isMandatory?: boolean;
  isFactoryOption?: boolean;
}

export interface OrderAccessory {
  id?: number;
  orderId?: number;
  accessoryCode: string;
  accessoryName: string;
  accessoryCategory?: string;
  description?: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  costPerUnit?: number;
  totalCost?: number;
  supplier?: string;
  installationRequired?: boolean;
}

export interface OrderContractService {
  id?: number;
  orderId?: number;
  serviceCode: string;
  serviceName: string;
  serviceType?: string;
  description?: string;
  price: number;
  cost?: number;
  durationMonths?: number;
  coverageDetails?: string;
  provider?: string;
}

export interface OrderAid {
  id?: number;
  orderId?: number;
  aidCode: string;
  aidName: string;
  aidType?: string;
  description?: string;
  amount: number;
  provider?: string;
  eligibilityCriteria?: string;
  approvalStatus?: ApprovalStatus;
  approvedByUserId?: number;
  approvedAt?: string;
}

export interface OrderSupplement {
  id?: number;
  orderId?: number;
  supplementCode: string;
  supplementName: string;
  supplementType?: string;
  description?: string;
  amount: number;
  reason?: string;
  approvedByUserId?: number;
  approvedAt?: string;
}

export interface OrderDelivery {
  id?: number;
  orderId?: number;
  deliveryType: DeliveryType;
  scheduledDate: string;
  scheduledTimeStart?: string;
  scheduledTimeEnd?: string;
  actualDeliveryDate?: string;
  deliveryAddressLine1?: string;
  deliveryAddressLine2?: string;
  deliveryCity?: string;
  deliveryState?: string;
  deliveryPostalCode?: string;
  deliveryCountry?: string;
  deliveryContactName?: string;
  deliveryContactPhone?: string;
  deliveryContactEmail?: string;
  deliveryInstructions?: string;
  deliveryStatus: DeliveryStatus;
  deliveredByUserId?: number;
  deliveryNotes?: string;
  signatureCaptured?: boolean;
  signatureData?: string;
  createdAt?: string;
  createdByUserId?: number;
  updatedAt?: string;
}

export interface OrderHistory {
  id: number;
  orderId: number;
  eventType: string;
  eventDescription: string;
  previousStatus?: string;
  newStatus?: string;
  previousData?: Record<string, any>;
  newData?: Record<string, any>;
  changedByUserId: number;
  changedAt: string;
  ipAddress?: string;
  userAgent?: string;
}

export interface OrderCommercialAction {
  id?: number;
  orderId?: number;
  actionCode: string;
  actionName: string;
  actionType?: string;
  amount: number;
  description?: string;
}

export interface OrderTradeIn {
  id?: number;
  orderId?: number;
  vehicleId?: number;
  vin?: string;
  make?: string;
  model?: string;
  year?: number;
  estimatedValue: number;
  finalValue?: number;
  condition?: string;
  notes?: string;
}

// ========== Order Main Interface ==========

export interface Order {
  id?: number;
  orderNumber: string;
  orderType: OrderType;

  // Customer & Business Info
  customerId: number;
  businessUnitId: number;
  salespersonId: number;

  // Vehicle Info (denormalized)
  vehicleId?: number;
  vin?: string;
  make?: string;
  model?: string;
  year?: number;
  trim?: string;
  colorExterior?: string;
  colorInterior?: string;

  // MOVE-specific vehicle fields
  semiClairModel?: string;
  semiClairVersion?: string;
  co2Level?: number;
  bodyType?: string;
  fuelType?: string;

  // MOVE-specific business metadata
  productType?: string;
  tariffNumber?: number;
  barcode?: string;
  familyBarcode?: string;
  distrinetCode?: string;
  distrinetExportNumber?: string;

  // Business organization fields
  establishmentName?: string;
  identifiantRr?: string;
  rattachement?: string;
  sellerType?: string;

  // Customer denormalized fields
  customerFirstName?: string;
  customerLastName?: string;
  customerEmail?: string;
  customerPhone?: string;
  customerAddress?: string;
  customerPostalCode?: string;
  customerCity?: string;
  customerCivility?: string;
  customerType?: string;
  customerSa?: number;
  customerCommercialName?: string;

  // Salesperson denormalized fields
  salespersonIpn?: string;
  salespersonName?: string;

  // Pricing Fields
  basePrice: number;
  optionsTotal?: number;
  accessoriesTotal?: number;
  servicesTotal?: number;
  aidsTotal?: number;
  supplementsTotal?: number;
  subtotal?: number;
  discountAmount?: number;
  discountPercentage?: number;
  totalBeforeTax?: number;
  vatRate?: number;
  vatAmount?: number;
  totalAmount?: number;

  // Margin Fields
  costPrice?: number;
  grossMargin?: number;
  netMargin?: number;
  marginPercentage?: number;

  // Trade-in
  tradeinVehicleId?: number;
  tradeinValue?: number;

  // Financing
  financingType?: string;
  financingInstitution?: string;
  financingAmount?: number;
  financingTermMonths?: number;
  financingInterestRate?: number;
  financingContractDiac?: string;
  financingWithDeposit?: boolean;
  financingNumberOfServices?: number;

  // Aids
  aideRpe?: number;
  aideAutres?: number;
  hasTradeIn?: boolean;

  // Workflow
  workflowInstanceId?: number;
  workflowCurrentState?: string;

  // Status & Lifecycle
  status: OrderStatus;

  // Delivery
  expectedDeliveryDate?: string;
  actualDeliveryDate?: string;
  deliveryLocation?: string;
  deliveryNotes?: string;

  // Notes & Comments
  notes?: string;
  internalComments?: string;

  // Child collections
  options?: OrderOption[];
  accessories?: OrderAccessory[];
  contractServices?: OrderContractService[];
  aids?: OrderAid[];
  supplements?: OrderSupplement[];
  deliveries?: OrderDelivery[];
  history?: OrderHistory[];
  commercialActions?: OrderCommercialAction[];
  tradeIns?: OrderTradeIn[];
  assignedConditions?: OrderAssignedCondition[];

  // Audit Fields
  createdAt?: string;
  createdByUserId?: number;
  updatedAt?: string;
  updatedByUserId?: number;
  deletedAt?: string;
  deletedByUserId?: number;
  version?: number;
}

// ========== DTO Interfaces ==========

export interface CreateOrderDto {
  orderType: OrderType;
  customerId: number;
  businessUnitId: number;
  salespersonId: number;

  // Vehicle Info (optional for draft)
  vehicleId?: number;
  vin?: string;
  make?: string;
  model?: string;
  year?: number;
  trim?: string;
  colorExterior?: string;
  colorInterior?: string;

  // Pricing
  basePrice: number;
  vatRate?: number;

  // Trade-in
  tradeinVehicleId?: number;
  tradeinValue?: number;

  // Financing
  financingType?: string;
  financingInstitution?: string;
  financingAmount?: number;
  financingTermMonths?: number;
  financingInterestRate?: number;

  // Delivery
  expectedDeliveryDate?: string;
  deliveryLocation?: string;
  deliveryNotes?: string;

  // Notes
  notes?: string;
  internalComments?: string;
}

export interface UpdateOrderDto {
  vehicleId?: number;
  vin?: string;
  make?: string;
  model?: string;
  year?: number;
  trim?: string;
  colorExterior?: string;
  colorInterior?: string;
  basePrice?: number;
  tradeinVehicleId?: number;
  tradeinValue?: number;
  financingType?: string;
  financingInstitution?: string;
  financingAmount?: number;
  financingTermMonths?: number;
  financingInterestRate?: number;
  expectedDeliveryDate?: string;
  deliveryLocation?: string;
  deliveryNotes?: string;
  notes?: string;
  internalComments?: string;
}

export interface ApplyDiscountDto {
  discountAmount?: number;
  discountPercentage?: number;
  reason?: string;
}

export interface OrderDetailResponse {
  order: Order;
  deliveries?: OrderDelivery[];
  history?: OrderHistory[];
  assignedConditions?: OrderAssignedCondition[];
}

export interface PaginatedOrders {
  content: Order[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface OrderFilters {
  page?: number;
  size?: number;
  status?: OrderStatus;
  orderType?: OrderType;
  customerId?: number;
  salespersonId?: number;
  businessUnitId?: number;
  startDate?: string;
  endDate?: string;
  search?: string;
}

// ========== Condition-related Interfaces ==========

export interface ConditionCategory {
  id: number;
  code: string;
  label: string;
  description?: string;
  isActive: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface OrderCondition {
  id: number;
  code: string;
  label: string;
  description?: string;
  categoryId?: number;
  categoryCode?: string;
  categoryLabel?: string;
  priority?: number;
  isActive: boolean;
  color?: string;
  icon?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface OrderConditionCriteria {
  id?: number;
  ruleId?: number;
  fieldPath: string;
  operator: string;
  value: string;
  criteriaGroup?: number;
  sequence?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface OrderConditionRule {
  id: number;
  conditionId: number;
  name: string;
  description?: string;
  logicalOperator: string;
  priority?: number;
  isActive: boolean;
  criteria: OrderConditionCriteria[];
  createdAt?: string;
  updatedAt?: string;
}

export interface OrderAssignedCondition {
  id: number;
  orderId: number;
  conditionId: number;
  conditionCode: string;
  conditionLabel: string;
  conditionColor?: string;
  conditionIcon?: string;
  ruleId?: number;
  ruleName?: string;
  assignedAt: string;
  assignedBy?: string;
  isManual: boolean;
  metadata?: Record<string, any>;
  createdAt?: string;
  updatedAt?: string;
}

export interface ConditionFieldDefinition {
  id: number;
  fieldPath: string;
  fieldLabel: string;
  fieldType: string;
  entity: string;
  availableOperators?: string;
  valueSource: string;
  lookupTable?: string;
  lookupField?: string;
  description?: string;
  isActive: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateConditionDto {
  code: string;
  label: string;
  description?: string;
  categoryId: number;
  priority?: number;
  isActive?: boolean;
  color?: string;
  icon?: string;
}

export interface CreateRuleDto {
  conditionId: number;
  name: string;
  description?: string;
  logicalOperator: string;
  priority?: number;
  isActive?: boolean;
  criteria?: {
    fieldPath: string;
    operator: string;
    value: string;
    criteriaGroup?: number;
    sequence?: number;
  }[];
}

export interface ScheduleDeliveryDto {
  deliveryType: DeliveryType;
  scheduledDate: string;
  timeStart?: string;
  timeEnd?: string;
  address?: string;
  city?: string;
  state?: string;
  postalCode?: string;
  country?: string;
  contactName?: string;
  contactPhone?: string;
  contactEmail?: string;
  instructions?: string;
}

export interface UpdateDeliveryDto {
  scheduledDate?: string;
  timeStart?: string;
  timeEnd?: string;
  instructions?: string;
}

export interface CompleteDeliveryDto {
  notes?: string;
  signatureCaptured?: boolean;
  signatureData?: string;
}

// ========== API Methods ==========

export const orderApi = {
  // ========== Order CRUD ==========

  /**
   * Get all orders (paginated)
   */
  async getOrders(filters?: OrderFilters): Promise<PaginatedOrders> {
    // Use real database connection (bypasses auth for development)
    const response = await apiClient.get('/api/orders', {
      params: filters,
    });
    return response.data;
  },

  /**
   * Create a new order
   */
  async createOrder(orderData: CreateOrderDto): Promise<Order> {
    const response = await apiClient.post('/api/orders', orderData);
    return response.data;
  },

  /**
   * Get order by ID
   */
  async getOrderById(id: number): Promise<Order> {
    const response = await apiClient.get(`/api/orders/${id}`);
    return response.data;
  },

  /**
   * Get order by ID (alias) - using dev endpoint
   */
  async getOrder(id: number): Promise<Order> {
    const response = await apiClient.get(`/api/orders/${id}`);
    return response.data;
  },

  /**
   * Get order by order number
   */
  async getOrderByOrderNumber(orderNumber: string): Promise<Order> {
    const response = await apiClient.get(`/api/orders/number/${orderNumber}`);
    return response.data;
  },

  /**
   * Update an order
   */
  async updateOrder(id: number, orderData: UpdateOrderDto): Promise<Order> {
    const response = await apiClient.put(`/api/orders/${id}`, orderData);
    return response.data;
  },

  /**
   * Delete an order (soft delete)
   */
  async deleteOrder(id: number): Promise<void> {
    await apiClient.delete(`/api/orders/${id}`);
  },

  // ========== Order Queries ==========

  /**
   * Get orders by customer ID
   */
  async getOrdersByCustomer(customerId: number): Promise<Order[]> {
    const response = await apiClient.get(`/api/orders/customer/${customerId}`);
    return response.data;
  },

  /**
   * Get orders by salesperson ID
   */
  async getOrdersBySalesperson(salespersonId: number): Promise<Order[]> {
    const response = await apiClient.get(`/api/orders/salesperson/${salespersonId}`);
    return response.data;
  },

  /**
   * Get orders by business unit ID
   */
  async getOrdersByBusinessUnit(businessUnitId: number): Promise<Order[]> {
    const response = await apiClient.get(`/api/orders/business-unit/${businessUnitId}`);
    return response.data;
  },

  /**
   * Get orders by status
   */
  async getOrdersByStatus(status: OrderStatus): Promise<Order[]> {
    const response = await apiClient.get(`/api/orders/status/${status}`);
    return response.data;
  },

  /**
   * Get all active orders
   */
  async getActiveOrders(): Promise<Order[]> {
    const response = await apiClient.get('/api/orders/active');
    return response.data;
  },

  // ========== Order Status Management ==========

  /**
   * Change order status
   */
  async changeOrderStatus(id: number, status: OrderStatus): Promise<Order> {
    const response = await apiClient.patch(`/api/orders/${id}/status/${status}`);
    return response.data;
  },

  /**
   * Validate an order
   */
  async validateOrder(id: number): Promise<Order> {
    const response = await apiClient.post(`/api/orders/${id}/validate`);
    return response.data;
  },

  /**
   * Cancel an order
   */
  async cancelOrder(id: number, reason?: string): Promise<Order> {
    const response = await apiClient.patch(`/api/orders/${id}/status/CANCELLED`, { reason });
    return response.data;
  },

  /**
   * Export orders to CSV
   */
  async exportOrders(filters?: OrderFilters): Promise<Blob> {
    const response = await apiClient.get('/api/orders/export', {
      params: filters,
      responseType: 'blob',
    });
    return response.data;
  },

  /**
   * Delete a document from an order
   */
  async deleteDocument(orderId: number, documentId: number): Promise<void> {
    await apiClient.delete(`/api/orders/${orderId}/documents/${documentId}`);
  },

  // ========== Pricing Management ==========

  /**
   * Recalculate order pricing
   */
  async recalculatePricing(id: number): Promise<Order> {
    const response = await apiClient.post(`/api/orders/${id}/recalculate`);
    return response.data;
  },

  /**
   * Apply discount to order
   */
  async applyDiscount(id: number, discountData: ApplyDiscountDto): Promise<Order> {
    const response = await apiClient.post(`/api/orders/${id}/discount`, discountData);
    return response.data;
  },

  // ========== Import ==========

  /**
   * Import order from MOVE/DIAC system
   */
  async importMoveOrder(moveData: any): Promise<Order> {
    const response = await apiClient.post('/api/orders/import/move', moveData);
    return response.data;
  },

  // ========== Delivery Management ==========

  /**
   * Schedule a delivery for an order
   */
  async scheduleDelivery(orderId: number, deliveryData: ScheduleDeliveryDto): Promise<OrderDelivery> {
    const response = await apiClient.post(`/api/deliveries/order/${orderId}/schedule`, null, {
      params: deliveryData,
    });
    return response.data;
  },

  /**
   * Update delivery details
   */
  async updateDelivery(deliveryId: number, deliveryData: UpdateDeliveryDto): Promise<OrderDelivery> {
    const response = await apiClient.put(`/api/deliveries/${deliveryId}`, null, {
      params: deliveryData,
    });
    return response.data;
  },

  /**
   * Complete a delivery
   */
  async completeDelivery(deliveryId: number, completionData: CompleteDeliveryDto): Promise<OrderDelivery> {
    const response = await apiClient.post(`/api/deliveries/${deliveryId}/complete`, null, {
      params: completionData,
    });
    return response.data;
  },

  /**
   * Cancel a delivery
   */
  async cancelDelivery(deliveryId: number, reason: string): Promise<OrderDelivery> {
    const response = await apiClient.post(`/api/deliveries/${deliveryId}/cancel`, null, {
      params: { reason },
    });
    return response.data;
  },

  /**
   * Get deliveries for an order
   */
  async getDeliveriesForOrder(orderId: number): Promise<OrderDelivery[]> {
    const response = await apiClient.get(`/api/deliveries/order/${orderId}`);
    return response.data;
  },

  /**
   * Get deliveries by scheduled date
   */
  async getDeliveriesByDate(date: string): Promise<OrderDelivery[]> {
    const response = await apiClient.get(`/api/deliveries/date/${date}`);
    return response.data;
  },

  /**
   * Get deliveries by status
   */
  async getDeliveriesByStatus(status: DeliveryStatus): Promise<OrderDelivery[]> {
    const response = await apiClient.get(`/api/deliveries/status/${status}`);
    return response.data;
  },

  // ========== Condition Management ==========

  /**
   * Get all condition categories
   */
  async getConditionCategories(): Promise<ConditionCategory[]> {
    const response = await apiClient.get('/api/conditions/categories');
    return response.data;
  },

  /**
   * Get category by ID
   */
  async getConditionCategoryById(id: number): Promise<ConditionCategory> {
    const response = await apiClient.get(`/api/conditions/categories/${id}`);
    return response.data;
  },

  /**
   * Get all conditions
   */
  async getConditions(): Promise<OrderCondition[]> {
    const response = await apiClient.get('/api/conditions');
    return response.data;
  },

  /**
   * Get condition by ID
   */
  async getConditionById(id: number): Promise<OrderCondition> {
    const response = await apiClient.get(`/api/conditions/${id}`);
    return response.data;
  },

  /**
   * Get conditions by category code
   */
  async getConditionsByCategory(categoryCode: string): Promise<OrderCondition[]> {
    const response = await apiClient.get(`/api/conditions/category/${categoryCode}`);
    return response.data;
  },

  /**
   * Create a new condition
   */
  async createCondition(conditionData: CreateConditionDto): Promise<OrderCondition> {
    const response = await apiClient.post('/api/conditions', conditionData);
    return response.data;
  },

  /**
   * Update a condition
   */
  async updateCondition(id: number, conditionData: CreateConditionDto): Promise<OrderCondition> {
    const response = await apiClient.put(`/api/conditions/${id}`, conditionData);
    return response.data;
  },

  /**
   * Delete a condition
   */
  async deleteCondition(id: number): Promise<void> {
    await apiClient.delete(`/api/conditions/${id}`);
  },

  /**
   * Create a new rule for a condition
   */
  async createConditionRule(ruleData: CreateRuleDto): Promise<OrderConditionRule> {
    const response = await apiClient.post('/api/conditions/rules', ruleData);
    return response.data;
  },

  /**
   * Get all rules for a condition
   */
  async getConditionRules(conditionId: number): Promise<OrderConditionRule[]> {
    const response = await apiClient.get(`/api/conditions/${conditionId}/rules`);
    return response.data;
  },

  /**
   * Evaluate and assign conditions to an order
   */
  async evaluateOrderConditions(orderId: number): Promise<OrderAssignedCondition[]> {
    const response = await apiClient.post(`/api/conditions/orders/${orderId}/evaluate`);
    return response.data;
  },

  /**
   * Get assigned conditions for an order
   */
  async getOrderConditions(orderId: number): Promise<OrderAssignedCondition[]> {
    const response = await apiClient.get(`/api/conditions/orders/${orderId}`);
    return response.data;
  },

  /**
   * Manually assign a condition to an order
   */
  async assignConditionToOrder(orderId: number, conditionId: number): Promise<OrderAssignedCondition> {
    const response = await apiClient.post(`/api/conditions/orders/${orderId}/assign/${conditionId}`);
    return response.data;
  },

  /**
   * Remove a condition from an order
   */
  async removeConditionFromOrder(orderId: number, conditionId: number): Promise<void> {
    await apiClient.delete(`/api/conditions/orders/${orderId}/conditions/${conditionId}`);
  },

  /**
   * Get all field definitions for rule building
   */
  async getConditionFields(): Promise<ConditionFieldDefinition[]> {
    const response = await apiClient.get('/api/conditions/fields');
    return response.data;
  },

  /**
   * Get field definitions by entity
   */
  async getConditionFieldsByEntity(entity: string): Promise<ConditionFieldDefinition[]> {
    const response = await apiClient.get(`/api/conditions/fields/entity/${entity}`);
    return response.data;
  },
};

export default orderApi;
