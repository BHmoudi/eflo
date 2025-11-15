package com.eflo.order.service;

import com.eflo.order.domain.entity.Order;
import com.eflo.order.domain.entity.OrderHistory;
import com.eflo.order.domain.model.dto.OrderDTO;
import com.eflo.order.domain.model.request.CreateOrderRequest;
import com.eflo.order.domain.model.request.UpdateOrderRequest;
import com.eflo.order.domain.repository.OrderHistoryRepository;
import com.eflo.order.domain.repository.OrderRepository;
import com.eflo.order.mapper.OrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderHistoryRepository orderHistoryRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private PricingService pricingService;

    @Mock
    private EventPublisherService eventPublisherService;

    @Mock
    private WorkflowIntegrationService workflowIntegrationService;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;
    private OrderDTO testOrderDTO;
    private CreateOrderRequest createRequest;
    private UpdateOrderRequest updateRequest;
    private static final Long TEST_USER_ID = 1L;

    @BeforeEach
    void setUp() {
        // Setup test order entity
        testOrder = Order.builder()
                .id(1L)
                .orderNumber("VN-12345678-ABC123")
                .orderType(Order.OrderType.VN)
                .status(Order.OrderStatus.DRAFT)
                .customerId(100L)
                .businessUnitId(10L)
                .salespersonId(50L)
                .vehicleId(200L)
                .vin("1HGBH41JXMN109186")
                .make("Toyota")
                .model("Camry")
                .year(2024)
                .basePrice(BigDecimal.valueOf(25000.00))
                .totalPrice(BigDecimal.valueOf(28000.00))
                .createdByUserId(TEST_USER_ID)
                .build();

        // Setup test DTO
        testOrderDTO = OrderDTO.builder()
                .id(1L)
                .orderNumber("VN-12345678-ABC123")
                .status("DRAFT")
                .build();

        // Setup create request
        createRequest = CreateOrderRequest.builder()
                .orderType("VN")
                .customerId(100L)
                .businessUnitId(10L)
                .salespersonId(50L)
                .vehicleId(200L)
                .vin("1HGBH41JXMN109186")
                .basePrice(BigDecimal.valueOf(25000.00))
                .build();

        // Setup update request
        updateRequest = UpdateOrderRequest.builder()
                .basePrice(BigDecimal.valueOf(26000.00))
                .build();
    }

    @Test
    @DisplayName("Create Order - Success")
    void createOrder_Success() {
        // Arrange
        when(orderMapper.toEntity(createRequest, TEST_USER_ID)).thenReturn(testOrder);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);
        doNothing().when(pricingService).recalculateOrderPricing(any(Order.class));
        doNothing().when(eventPublisherService).publishOrderCreated(any(Order.class), eq(TEST_USER_ID));
        doNothing().when(workflowIntegrationService).initializeWorkflowForOrder(any(Order.class));

        // Act
        OrderDTO result = orderService.createOrder(createRequest, TEST_USER_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getOrderNumber()).isEqualTo("VN-12345678-ABC123");

        // Verify interactions
        verify(orderMapper).toEntity(createRequest, TEST_USER_ID);
        verify(pricingService).recalculateOrderPricing(any(Order.class));
        verify(orderRepository).save(any(Order.class));
        verify(orderHistoryRepository).save(any(OrderHistory.class));
        verify(eventPublisherService).publishOrderCreated(any(Order.class), eq(TEST_USER_ID));
        verify(workflowIntegrationService).initializeWorkflowForOrder(any(Order.class));
    }

    @Test
    @DisplayName("Create Order - Workflow Initialization Failure Should Not Fail Order Creation")
    void createOrder_WorkflowFailure_ShouldNotFailOrderCreation() {
        // Arrange
        when(orderMapper.toEntity(createRequest, TEST_USER_ID)).thenReturn(testOrder);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);
        doThrow(new RuntimeException("Workflow service unavailable"))
                .when(workflowIntegrationService).initializeWorkflowForOrder(any(Order.class));

        // Act
        OrderDTO result = orderService.createOrder(createRequest, TEST_USER_ID);

        // Assert
        assertThat(result).isNotNull();
        verify(workflowIntegrationService).initializeWorkflowForOrder(any(Order.class));
        // Order should still be created despite workflow failure
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Create Order - Order Number Generation for VN Type")
    void createOrder_GeneratesCorrectOrderNumber_VN() {
        // Arrange
        when(orderMapper.toEntity(createRequest, TEST_USER_ID)).thenReturn(testOrder);
        when(orderMapper.toDTO(any(Order.class))).thenReturn(testOrderDTO);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        when(orderRepository.save(orderCaptor.capture())).thenReturn(testOrder);

        // Act
        orderService.createOrder(createRequest, TEST_USER_ID);

        // Assert
        Order savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.getOrderNumber()).startsWith("VN-");
        assertThat(savedOrder.getOrderNumber()).hasSize(22); // VN-XXXXXXXX-XXXXXX
    }

    @Test
    @DisplayName("Create Order - Order Number Generation for VO Type")
    void createOrder_GeneratesCorrectOrderNumber_VO() {
        // Arrange
        Order voOrder = testOrder.toBuilder().orderType(Order.OrderType.VO).build();
        CreateOrderRequest voRequest = createRequest.toBuilder().orderType("VO").build();

        when(orderMapper.toEntity(voRequest, TEST_USER_ID)).thenReturn(voOrder);
        when(orderMapper.toDTO(any(Order.class))).thenReturn(testOrderDTO);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        when(orderRepository.save(orderCaptor.capture())).thenReturn(voOrder);

        // Act
        orderService.createOrder(voRequest, TEST_USER_ID);

        // Assert
        Order savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.getOrderNumber()).startsWith("VO-");
    }

    @Test
    @DisplayName("Create Order - Order Number Generation for EVO Type")
    void createOrder_GeneratesCorrectOrderNumber_EVO() {
        // Arrange
        Order evoOrder = testOrder.toBuilder().orderType(Order.OrderType.EVO).build();
        CreateOrderRequest evoRequest = createRequest.toBuilder().orderType("EVO").build();

        when(orderMapper.toEntity(evoRequest, TEST_USER_ID)).thenReturn(evoOrder);
        when(orderMapper.toDTO(any(Order.class))).thenReturn(testOrderDTO);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        when(orderRepository.save(orderCaptor.capture())).thenReturn(evoOrder);

        // Act
        orderService.createOrder(evoRequest, TEST_USER_ID);

        // Assert
        Order savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.getOrderNumber()).startsWith("EVO-");
    }

    @Test
    @DisplayName("Update Order - Success")
    void updateOrder_Success() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);
        doNothing().when(orderMapper).updateEntityFromRequest(any(Order.class), any(UpdateOrderRequest.class));
        doNothing().when(pricingService).recalculateOrderPricing(any(Order.class));

        // Act
        OrderDTO result = orderService.updateOrder(1L, updateRequest, TEST_USER_ID);

        // Assert
        assertThat(result).isNotNull();
        verify(orderRepository).findById(1L);
        verify(orderMapper).updateEntityFromRequest(testOrder, updateRequest);
        verify(pricingService).recalculateOrderPricing(testOrder);
        verify(orderRepository).save(testOrder);
        verify(orderHistoryRepository).save(any(OrderHistory.class));
        verify(eventPublisherService).publishOrderUpdated(testOrder, TEST_USER_ID);
    }

    @Test
    @DisplayName("Update Order - Not Found")
    void updateOrder_NotFound_ThrowsException() {
        // Arrange
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.updateOrder(999L, updateRequest, TEST_USER_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Order not found: 999");

        verify(orderRepository).findById(999L);
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Change Order Status - Valid Transition DRAFT to PENDING")
    void changeOrderStatus_ValidTransition_DraftToPending() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

        // Act
        OrderDTO result = orderService.changeOrderStatus(1L, Order.OrderStatus.PENDING, TEST_USER_ID);

        // Assert
        assertThat(result).isNotNull();
        verify(orderRepository).save(testOrder);
        verify(orderHistoryRepository).save(any(OrderHistory.class));
        verify(eventPublisherService).publishOrderStatusChanged(
                eq(testOrder), eq(Order.OrderStatus.DRAFT), eq(Order.OrderStatus.PENDING), eq(TEST_USER_ID));
    }

    @Test
    @DisplayName("Change Order Status - Invalid Transition Throws Exception")
    void changeOrderStatus_InvalidTransition_ThrowsException() {
        // Arrange
        testOrder.setStatus(Order.OrderStatus.INVOICED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        assertThatThrownBy(() -> orderService.changeOrderStatus(1L, Order.OrderStatus.PENDING, TEST_USER_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid status transition");

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Change Order Status - Complete Workflow When Delivered")
    void changeOrderStatus_ToDelivered_CompletesWorkflow() {
        // Arrange
        testOrder.setStatus(Order.OrderStatus.READY_FOR_DELIVERY);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);
        doNothing().when(workflowIntegrationService).completeWorkflowForOrder(1L);

        // Act
        orderService.changeOrderStatus(1L, Order.OrderStatus.DELIVERED, TEST_USER_ID);

        // Assert
        verify(workflowIntegrationService).completeWorkflowForOrder(1L);
    }

    @Test
    @DisplayName("Change Order Status - Sync With Workflow When Workflow Instance Exists")
    void changeOrderStatus_WithWorkflowInstance_SyncsWithWorkflow() {
        // Arrange
        testOrder.setStatus(Order.OrderStatus.PENDING);
        testOrder.setWorkflowInstanceId("workflow-123");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);
        doNothing().when(workflowIntegrationService).syncOrderWithWorkflowState(1L, "CONFIRMED");

        // Act
        orderService.changeOrderStatus(1L, Order.OrderStatus.CONFIRMED, TEST_USER_ID);

        // Assert
        verify(workflowIntegrationService).syncOrderWithWorkflowState(1L, "CONFIRMED");
    }

    @Test
    @DisplayName("Get Order By ID - Success")
    void getOrderById_Success() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

        // Act
        OrderDTO result = orderService.getOrderById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getOrderNumber()).isEqualTo("VN-12345678-ABC123");
        verify(orderRepository).findById(1L);
    }

    @Test
    @DisplayName("Get Order By ID - Not Found")
    void getOrderById_NotFound_ThrowsException() {
        // Arrange
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.getOrderById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Order not found: 999");
    }

    @Test
    @DisplayName("Get Order By Order Number - Success")
    void getOrderByOrderNumber_Success() {
        // Arrange
        when(orderRepository.findByOrderNumber("VN-12345678-ABC123")).thenReturn(Optional.of(testOrder));
        when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

        // Act
        OrderDTO result = orderService.getOrderByOrderNumber("VN-12345678-ABC123");

        // Assert
        assertThat(result).isNotNull();
        verify(orderRepository).findByOrderNumber("VN-12345678-ABC123");
    }

    @Test
    @DisplayName("Get Orders By Customer - Returns List")
    void getOrdersByCustomer_ReturnsOrders() {
        // Arrange
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findByCustomerId(100L)).thenReturn(orders);
        when(orderMapper.toDTOs(orders)).thenReturn(Arrays.asList(testOrderDTO));

        // Act
        List<OrderDTO> result = orderService.getOrdersByCustomer(100L);

        // Assert
        assertThat(result).hasSize(1);
        verify(orderRepository).findByCustomerId(100L);
    }

    @Test
    @DisplayName("Get Orders By Salesperson - Returns List")
    void getOrdersBySalesperson_ReturnsOrders() {
        // Arrange
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findBySalespersonId(50L)).thenReturn(orders);
        when(orderMapper.toDTOs(orders)).thenReturn(Arrays.asList(testOrderDTO));

        // Act
        List<OrderDTO> result = orderService.getOrdersBySalesperson(50L);

        // Assert
        assertThat(result).hasSize(1);
        verify(orderRepository).findBySalespersonId(50L);
    }

    @Test
    @DisplayName("Get Orders By Business Unit - Returns List")
    void getOrdersByBusinessUnit_ReturnsOrders() {
        // Arrange
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findByBusinessUnitId(10L)).thenReturn(orders);
        when(orderMapper.toDTOs(orders)).thenReturn(Arrays.asList(testOrderDTO));

        // Act
        List<OrderDTO> result = orderService.getOrdersByBusinessUnit(10L);

        // Assert
        assertThat(result).hasSize(1);
        verify(orderRepository).findByBusinessUnitId(10L);
    }

    @Test
    @DisplayName("Get Orders By Status - Returns List")
    void getOrdersByStatus_ReturnsOrders() {
        // Arrange
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findByStatus(Order.OrderStatus.DRAFT)).thenReturn(orders);
        when(orderMapper.toDTOs(orders)).thenReturn(Arrays.asList(testOrderDTO));

        // Act
        List<OrderDTO> result = orderService.getOrdersByStatus(Order.OrderStatus.DRAFT);

        // Assert
        assertThat(result).hasSize(1);
        verify(orderRepository).findByStatus(Order.OrderStatus.DRAFT);
    }

    @Test
    @DisplayName("Get All Active Orders - Returns List")
    void getAllActiveOrders_ReturnsOrders() {
        // Arrange
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findAllActive()).thenReturn(orders);
        when(orderMapper.toDTOs(orders)).thenReturn(Arrays.asList(testOrderDTO));

        // Act
        List<OrderDTO> result = orderService.getAllActiveOrders();

        // Assert
        assertThat(result).hasSize(1);
        verify(orderRepository).findAllActive();
    }

    @Test
    @DisplayName("Get All Orders Paginated - Returns Page")
    void getAllOrders_Paginated_ReturnsPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(Arrays.asList(testOrder), pageable, 1);
        when(orderRepository.findAll(pageable)).thenReturn(orderPage);
        when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

        // Act
        Page<OrderDTO> result = orderService.getAllOrders(pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(orderRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Delete Order - Soft Delete Success")
    void deleteOrder_SoftDelete_Success() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);

        // Act
        orderService.deleteOrder(1L, TEST_USER_ID);

        // Assert
        verify(orderRepository).save(orderCaptor.capture());
        Order deletedOrder = orderCaptor.getValue();
        assertThat(deletedOrder.getDeletedAt()).isNotNull();
        assertThat(deletedOrder.getDeletedByUserId()).isEqualTo(TEST_USER_ID);
        verify(orderHistoryRepository).save(any(OrderHistory.class));
    }

    @Test
    @DisplayName("Validate Order - Success With Vehicle Info")
    void validateOrder_Success_WithVehicleInfo() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

        // Act
        OrderDTO result = orderService.validateOrder(1L, TEST_USER_ID);

        // Assert
        assertThat(result).isNotNull();
        verify(orderHistoryRepository).save(any(OrderHistory.class));
        verify(eventPublisherService).publishOrderValidated(testOrder, TEST_USER_ID);
    }

    @Test
    @DisplayName("Validate Order - Fails Without Vehicle Info")
    void validateOrder_FailsWithoutVehicleInfo() {
        // Arrange
        testOrder.setVehicleId(null);
        testOrder.setVin(null);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        assertThatThrownBy(() -> orderService.validateOrder(1L, TEST_USER_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Order must have vehicle information");

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Validate Order - Changes Status From DRAFT to PENDING")
    void validateOrder_ChangesDraftToPending() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);

        // Act
        orderService.validateOrder(1L, TEST_USER_ID);

        // Assert
        verify(orderRepository).save(orderCaptor.capture());
        Order validatedOrder = orderCaptor.getValue();
        assertThat(validatedOrder.getStatus()).isEqualTo(Order.OrderStatus.PENDING);
    }

    @Test
    @DisplayName("Recalculate Pricing - Success")
    void recalculatePricing_Success() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);
        doNothing().when(pricingService).recalculateOrderPricing(testOrder);

        // Act
        OrderDTO result = orderService.recalculatePricing(1L, TEST_USER_ID);

        // Assert
        assertThat(result).isNotNull();
        verify(pricingService).recalculateOrderPricing(testOrder);
        verify(orderRepository).save(testOrder);
        verify(orderHistoryRepository).save(any(OrderHistory.class));
        verify(eventPublisherService).publishOrderPriceChanged(testOrder, TEST_USER_ID);
    }

    @Test
    @DisplayName("Status Transition Validation - All Valid DRAFT Transitions")
    void validateStatusTransition_DraftTransitions() {
        // Valid: DRAFT -> PENDING
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

        orderService.changeOrderStatus(1L, Order.OrderStatus.PENDING, TEST_USER_ID);
        verify(orderRepository, times(1)).save(testOrder);

        // Valid: DRAFT -> CANCELLED
        testOrder.setStatus(Order.OrderStatus.DRAFT);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        orderService.changeOrderStatus(1L, Order.OrderStatus.CANCELLED, TEST_USER_ID);
        verify(orderRepository, times(2)).save(testOrder);
    }

    @Test
    @DisplayName("Status Transition Validation - All Valid PENDING Transitions")
    void validateStatusTransition_PendingTransitions() {
        // PENDING -> CONFIRMED
        testOrder.setStatus(Order.OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

        orderService.changeOrderStatus(1L, Order.OrderStatus.CONFIRMED, TEST_USER_ID);
        verify(orderRepository).save(testOrder);
    }

    @Test
    @DisplayName("Status Transition Validation - All Valid CONFIRMED Transitions")
    void validateStatusTransition_ConfirmedTransitions() {
        // CONFIRMED -> IN_PRODUCTION
        testOrder.setStatus(Order.OrderStatus.CONFIRMED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

        orderService.changeOrderStatus(1L, Order.OrderStatus.IN_PRODUCTION, TEST_USER_ID);
        verify(orderRepository).save(testOrder);
    }

    @Test
    @DisplayName("Status Transition Validation - Terminal States Cannot Transition")
    void validateStatusTransition_TerminalStatesCannotTransition() {
        // INVOICED is terminal
        testOrder.setStatus(Order.OrderStatus.INVOICED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        assertThatThrownBy(() -> orderService.changeOrderStatus(1L, Order.OrderStatus.PENDING, TEST_USER_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid status transition");

        // CANCELLED is terminal
        testOrder.setStatus(Order.OrderStatus.CANCELLED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        assertThatThrownBy(() -> orderService.changeOrderStatus(1L, Order.OrderStatus.PENDING, TEST_USER_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid status transition");
    }

    @Test
    @DisplayName("Status Transition Validation - ON_HOLD Can Resume To Multiple States")
    void validateStatusTransition_OnHoldCanResume() {
        // ON_HOLD -> PENDING
        testOrder.setStatus(Order.OrderStatus.ON_HOLD);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

        orderService.changeOrderStatus(1L, Order.OrderStatus.PENDING, TEST_USER_ID);
        verify(orderRepository).save(testOrder);
    }

    @Test
    @DisplayName("History Recording - Records All Order Events")
    void recordHistory_RecordsAllEvents() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

        ArgumentCaptor<OrderHistory> historyCaptor = ArgumentCaptor.forClass(OrderHistory.class);

        // Act
        orderService.changeOrderStatus(1L, Order.OrderStatus.PENDING, TEST_USER_ID);

        // Assert
        verify(orderHistoryRepository).save(historyCaptor.capture());
        OrderHistory history = historyCaptor.getValue();
        assertThat(history.getEventType()).isEqualTo("STATUS_CHANGED");
        assertThat(history.getPreviousStatus()).isEqualTo("DRAFT");
        assertThat(history.getNewStatus()).isEqualTo("PENDING");
        assertThat(history.getChangedByUserId()).isEqualTo(TEST_USER_ID);
    }
}
