package com.eflo.order.web;

import com.eflo.order.domain.entity.Order;
import com.eflo.order.domain.model.dto.MoveOrderRequest;
import com.eflo.order.domain.model.dto.OrderDTO;
import com.eflo.order.domain.model.dto.WorkflowInstanceDTO;
import com.eflo.order.domain.model.request.ApplyDiscountRequest;
import com.eflo.order.domain.model.request.CreateOrderRequest;
import com.eflo.order.domain.model.request.TransitionRequest;
import com.eflo.order.domain.model.request.UpdateOrderRequest;
import com.eflo.order.service.EventPublisherService;
import com.eflo.order.service.MoveImportService;
import com.eflo.order.service.OrderService;
import com.eflo.order.service.PricingService;
import com.eflo.order.service.WorkflowIntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import com.eflo.order.security.SecurityUtils;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "APIs for managing vehicle orders (VN, VO, EVO)")
public class OrderController {

    private final OrderService orderService;
    private final PricingService pricingService;
    private final MoveImportService moveImportService;
    private final EventPublisherService eventPublisherService;
    private final WorkflowIntegrationService workflowIntegrationService;

    @GetMapping
    @Operation(summary = "Get all orders (paginated)")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON', 'VIEWER')")
    public ResponseEntity<Page<OrderDTO>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<OrderDTO> orders = orderService.getAllOrders(PageRequest.of(page, size));
        return ResponseEntity.ok(orders);
    }

    @PostMapping
    @Operation(summary = "Create a new order")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON')")
    public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody CreateOrderRequest request,
                                                @AuthenticationPrincipal Jwt jwt) {
        Long userId = SecurityUtils.getUserId(jwt);
        OrderDTO order = orderService.createOrder(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON', 'VIEWER')")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        OrderDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "Get order by order number")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON', 'VIEWER')")
    public ResponseEntity<OrderDTO> getOrderByOrderNumber(@PathVariable String orderNumber) {
        OrderDTO order = orderService.getOrderByOrderNumber(orderNumber);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get orders by customer ID")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON', 'VIEWER')")
    public ResponseEntity<List<OrderDTO>> getOrdersByCustomer(@PathVariable Long customerId) {
        List<OrderDTO> orders = orderService.getOrdersByCustomer(customerId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/salesperson/{salespersonId}")
    @Operation(summary = "Get orders by salesperson ID")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON', 'VIEWER')")
    public ResponseEntity<List<OrderDTO>> getOrdersBySalesperson(@PathVariable Long salespersonId) {
        List<OrderDTO> orders = orderService.getOrdersBySalesperson(salespersonId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/business-unit/{businessUnitId}")
    @Operation(summary = "Get orders by business unit ID")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'VIEWER')")
    public ResponseEntity<List<OrderDTO>> getOrdersByBusinessUnit(@PathVariable Long businessUnitId) {
        List<OrderDTO> orders = orderService.getOrdersByBusinessUnit(businessUnitId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get orders by status")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON', 'VIEWER')")
    public ResponseEntity<List<OrderDTO>> getOrdersByStatus(@PathVariable Order.OrderStatus status) {
        List<OrderDTO> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active orders")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON', 'VIEWER')")
    public ResponseEntity<List<OrderDTO>> getAllActiveOrders() {
        List<OrderDTO> orders = orderService.getAllActiveOrders();
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an order")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON')")
    public ResponseEntity<OrderDTO> updateOrder(@PathVariable Long id,
                                                @Valid @RequestBody UpdateOrderRequest request,
                                                @AuthenticationPrincipal Jwt jwt) {
        Long userId = SecurityUtils.getUserId(jwt);
        OrderDTO order = orderService.updateOrder(id, request, userId);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/{id}/status/{status}")
    @Operation(summary = "Change order status")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON')")
    public ResponseEntity<OrderDTO> changeOrderStatus(@PathVariable Long id,
                                                      @PathVariable Order.OrderStatus status,
                                                      @AuthenticationPrincipal Jwt jwt) {
        Long userId = SecurityUtils.getUserId(jwt);
        OrderDTO order = orderService.changeOrderStatus(id, status, userId);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{id}/validate")
    @Operation(summary = "Validate an order")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON')")
    public ResponseEntity<OrderDTO> validateOrder(@PathVariable Long id,
                                                  @AuthenticationPrincipal Jwt jwt) {
        Long userId = SecurityUtils.getUserId(jwt);
        OrderDTO order = orderService.validateOrder(id, userId);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{id}/recalculate")
    @Operation(summary = "Recalculate order pricing")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON')")
    public ResponseEntity<OrderDTO> recalculatePricing(@PathVariable Long id,
                                                       @AuthenticationPrincipal Jwt jwt) {
        Long userId = SecurityUtils.getUserId(jwt);
        OrderDTO order = orderService.recalculatePricing(id, userId);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{id}/discount")
    @Operation(summary = "Apply discount to order")
    @PreAuthorize("hasRole('SALES_MANAGER')")
    public ResponseEntity<OrderDTO> applyDiscount(@PathVariable Long id,
                                                  @Valid @RequestBody ApplyDiscountRequest request,
                                                  @AuthenticationPrincipal Jwt jwt) {
        Long userId = SecurityUtils.getUserId(jwt);
        OrderDTO order = orderService.getOrderById(id);
        // Apply discount logic would be here
        return ResponseEntity.ok(order);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an order (soft delete)")
    @PreAuthorize("hasRole('SALES_MANAGER')")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id,
                                           @AuthenticationPrincipal Jwt jwt) {
        Long userId = SecurityUtils.getUserId(jwt);
        orderService.deleteOrder(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import/move")
    @Operation(summary = "Import order from MOVE/DIAC system")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON', 'SYSTEM_IMPORT')")
    public ResponseEntity<Order> importMoveOrder(@Valid @RequestBody MoveOrderRequest request,
                                                 @AuthenticationPrincipal Jwt jwt) {
        Long userId = SecurityUtils.getUserId(jwt);
        Order order = moveImportService.importMoveOrder(request, userId);

        // Publish order.created event - Kafka listener will auto-assign conditions
        eventPublisherService.publishOrderCreated(order, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/{id}/workflow")
    @Operation(summary = "Get workflow instance for an order")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON', 'VIEWER')")
    public ResponseEntity<WorkflowInstanceDTO> getOrderWorkflow(@PathVariable Long id) {
        WorkflowInstanceDTO workflowInstance = workflowIntegrationService.getWorkflowForOrder(id);
        return ResponseEntity.ok(workflowInstance);
    }

    @PostMapping("/{id}/workflow/transition")
    @Operation(summary = "Execute a workflow transition for an order")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON')")
    public ResponseEntity<WorkflowInstanceDTO> executeWorkflowTransition(
            @PathVariable Long id,
            @Valid @RequestBody TransitionRequest request) {
        WorkflowInstanceDTO workflowInstance = workflowIntegrationService.executeWorkflowTransition(id, request);
        return ResponseEntity.ok(workflowInstance);
    }

    // User extraction centralized in SecurityUtils
}
