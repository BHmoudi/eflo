package com.eflo.order.service;

import com.eflo.order.domain.entity.*;
import com.eflo.order.domain.model.dto.OrderDTO;
import com.eflo.order.domain.model.request.CreateOrderRequest;
import com.eflo.order.domain.model.request.UpdateOrderRequest;
import com.eflo.order.domain.repository.*;
import com.eflo.order.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final OrderMapper orderMapper;
    private final PricingService pricingService;
    private final EventPublisherService eventPublisherService;
    private final WorkflowIntegrationService workflowIntegrationService;

    /**
     * Create a new order
     */
    @Transactional
    public OrderDTO createOrder(CreateOrderRequest request, Long userId) {
        log.info("Creating new order for customer: {}", request.getCustomerId());

        // Create order entity
        Order order = orderMapper.toEntity(request, userId);

        // Generate unique order number
        order.setOrderNumber(generateOrderNumber(order.getOrderType()));

        // Calculate initial pricing
        pricingService.recalculateOrderPricing(order);

        // Save order
        order = orderRepository.save(order);

        // Record history
        recordHistory(order, "ORDER_CREATED", "Order created", null,
                      Order.OrderStatus.DRAFT.name(), userId);

        // Publish event
        eventPublisherService.publishOrderCreated(order, userId);

        // Initialize workflow for order (non-fatal if workflow service unavailable)
        try {
            workflowIntegrationService.initializeWorkflowForOrder(order);
        } catch (Exception e) {
            log.warn("Failed to initialize workflow for order {}: {}", order.getOrderNumber(), e.getMessage());
        }

        log.info("Order created successfully: {}", order.getOrderNumber());
        return orderMapper.toDTO(order);
    }

    /**
     * Get all orders (paginated)
     */
    @Transactional(readOnly = true)
    public Page<OrderDTO> getAllOrders(Pageable pageable) {
        log.debug("Fetching all orders - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Order> orders = orderRepository.findAll(pageable);
        return orders.map(orderMapper::toDTO);
    }

    /**
     * Update an existing order
     */
    @Transactional
    public OrderDTO updateOrder(Long orderId, UpdateOrderRequest request, Long userId) {
        log.info("Updating order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // Update fields
        orderMapper.updateEntityFromRequest(order, request);
        order.setUpdatedByUserId(userId);

        // Recalculate pricing if base price changed
        if (request.getBasePrice() != null) {
            pricingService.recalculateOrderPricing(order);
        }

        order = orderRepository.save(order);

        // Record history
        recordHistory(order, "ORDER_UPDATED", "Order updated", null, null, userId);

        // Publish event
        eventPublisherService.publishOrderUpdated(order, userId);

        log.info("Order updated successfully: {}", order.getOrderNumber());
        return orderMapper.toDTO(order);
    }

    /**
     * Change order status
     */
    @Transactional
    public OrderDTO changeOrderStatus(Long orderId, Order.OrderStatus newStatus, Long userId) {
        log.info("Changing order {} status to: {}", orderId, newStatus);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        Order.OrderStatus previousStatus = order.getStatus();

        // Validate status transition
        validateStatusTransition(previousStatus, newStatus);

        order.setStatus(newStatus);
        order.setUpdatedByUserId(userId);

        order = orderRepository.save(order);

        // Record history
        recordHistory(order, "STATUS_CHANGED", "Order status changed",
                      previousStatus.name(), newStatus.name(), userId);

        // Publish event
        eventPublisherService.publishOrderStatusChanged(order, previousStatus, newStatus, userId);

        // Sync with workflow if workflow exists
        if (order.getWorkflowInstanceId() != null) {
            try {
                workflowIntegrationService.syncOrderWithWorkflowState(orderId, newStatus.name());
            } catch (Exception e) {
                log.warn("Failed to sync order {} with workflow: {}", orderId, e.getMessage());
                // Continue even if workflow sync fails
            }
        }

        // Complete workflow when order is delivered
        if (newStatus == Order.OrderStatus.DELIVERED) {
            workflowIntegrationService.completeWorkflowForOrder(orderId);
        }

        log.info("Order {} status changed from {} to {}",
                 order.getOrderNumber(), previousStatus, newStatus);
        return orderMapper.toDTO(order);
    }

    /**
     * Get order by ID
     */
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        return orderMapper.toDTO(order);
    }

    /**
     * Get order by order number
     */
    @Transactional(readOnly = true)
    public OrderDTO getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderNumber));
        return orderMapper.toDTO(order);
    }

    /**
     * Get orders by customer
     */
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByCustomer(Long customerId) {
        List<Order> orders = orderRepository.findByCustomerId(customerId);
        return orderMapper.toDTOs(orders);
    }

    /**
     * Get orders by salesperson
     */
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersBySalesperson(Long salespersonId) {
        List<Order> orders = orderRepository.findBySalespersonId(salespersonId);
        return orderMapper.toDTOs(orders);
    }

    /**
     * Get orders by business unit
     */
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByBusinessUnit(Long businessUnitId) {
        List<Order> orders = orderRepository.findByBusinessUnitId(businessUnitId);
        return orderMapper.toDTOs(orders);
    }

    /**
     * Get orders by status
     */
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByStatus(Order.OrderStatus status) {
        List<Order> orders = orderRepository.findByStatus(status);
        return orderMapper.toDTOs(orders);
    }

    /**
     * Get all active orders
     */
    @Transactional(readOnly = true)
    public List<OrderDTO> getAllActiveOrders() {
        List<Order> orders = orderRepository.findAllActive();
        return orderMapper.toDTOs(orders);
    }

    /**
     * Delete order (soft delete)
     */
    @Transactional
    public void deleteOrder(Long orderId, Long userId) {
        log.info("Deleting order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        order.setDeletedAt(LocalDateTime.now());
        order.setDeletedByUserId(userId);

        orderRepository.save(order);

        // Record history
        recordHistory(order, "ORDER_DELETED", "Order deleted", null, null, userId);

        log.info("Order {} deleted successfully", order.getOrderNumber());
    }

    /**
     * Validate order (business validation)
     */
    @Transactional
    public OrderDTO validateOrder(Long orderId, Long userId) {
        log.info("Validating order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // Perform validation checks
        if (order.getVehicleId() == null && order.getVin() == null) {
            throw new RuntimeException("Order must have vehicle information");
        }

        // Change status to PENDING if DRAFT
        if (order.getStatus() == Order.OrderStatus.DRAFT) {
            order.setStatus(Order.OrderStatus.PENDING);
        }

        order = orderRepository.save(order);

        // Record history
        recordHistory(order, "ORDER_VALIDATED", "Order validated", null, null, userId);

        // Publish event
        eventPublisherService.publishOrderValidated(order, userId);

        log.info("Order {} validated successfully", order.getOrderNumber());
        return orderMapper.toDTO(order);
    }

    /**
     * Recalculate order pricing
     */
    @Transactional
    public OrderDTO recalculatePricing(Long orderId, Long userId) {
        log.info("Recalculating pricing for order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        pricingService.recalculateOrderPricing(order);
        order.setUpdatedByUserId(userId);

        order = orderRepository.save(order);

        // Record history
        recordHistory(order, "PRICE_RECALCULATED", "Order pricing recalculated", null, null, userId);

        // Publish event
        eventPublisherService.publishOrderPriceChanged(order, userId);

        log.info("Order {} pricing recalculated", order.getOrderNumber());
        return orderMapper.toDTO(order);
    }

    /**
     * Generate unique order number
     */
    private String generateOrderNumber(Order.OrderType orderType) {
        String prefix = switch (orderType) {
            case VN -> "VN";
            case VO -> "VO";
            case EVO -> "EVO";
        };

        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        return prefix + "-" + timestamp.substring(timestamp.length() - 8) + "-" + random;
    }

    /**
     * Validate status transition
     */
    private void validateStatusTransition(Order.OrderStatus from, Order.OrderStatus to) {
        // Define valid transitions
        boolean isValid = switch (from) {
            case DRAFT -> to == Order.OrderStatus.PENDING || to == Order.OrderStatus.CANCELLED;
            case PENDING -> to == Order.OrderStatus.CONFIRMED || to == Order.OrderStatus.CANCELLED ||
                           to == Order.OrderStatus.ON_HOLD;
            case CONFIRMED -> to == Order.OrderStatus.IN_PRODUCTION || to == Order.OrderStatus.CANCELLED ||
                             to == Order.OrderStatus.ON_HOLD;
            case IN_PRODUCTION -> to == Order.OrderStatus.READY_FOR_DELIVERY ||
                                  to == Order.OrderStatus.ON_HOLD;
            case READY_FOR_DELIVERY -> to == Order.OrderStatus.DELIVERED || to == Order.OrderStatus.ON_HOLD;
            case DELIVERED -> to == Order.OrderStatus.INVOICED;
            case ON_HOLD -> to == Order.OrderStatus.PENDING || to == Order.OrderStatus.CONFIRMED ||
                           to == Order.OrderStatus.IN_PRODUCTION || to == Order.OrderStatus.CANCELLED;
            case INVOICED, CANCELLED -> false; // Terminal states
        };

        if (!isValid) {
            throw new RuntimeException("Invalid status transition from " + from + " to " + to);
        }
    }

    /**
     * Record order history
     */
    private void recordHistory(Order order, String eventType, String description,
                               String previousStatus, String newStatus, Long userId) {
        OrderHistory history = OrderHistory.builder()
                .order(order)
                .eventType(eventType)
                .eventDescription(description)
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .changedByUserId(userId)
                .build();

        orderHistoryRepository.save(history);
    }
}
