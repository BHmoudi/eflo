package com.eflo.commission.service;

import com.eflo.commission.calculator.CommissionCalculationContext;
import com.eflo.commission.calculator.CommissionCalculationResult;
import com.eflo.commission.calculator.CommissionCalculator;
import com.eflo.commission.domain.entity.Commission;
import com.eflo.commission.domain.entity.CommissionHistory;
import com.eflo.commission.domain.entity.CommissionScale;
import com.eflo.commission.domain.enums.CommissionStatus;
import com.eflo.commission.domain.model.request.CalculateCommissionRequest;
import com.eflo.commission.domain.repository.CommissionHistoryRepository;
import com.eflo.commission.domain.repository.CommissionRepository;
import com.eflo.commission.domain.repository.CommissionScaleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommissionCalculationService Unit Tests")
class CommissionCalculationServiceTest {

    @Mock
    private CommissionRepository commissionRepository;

    @Mock
    private CommissionScaleRepository commissionScaleRepository;

    @Mock
    private CommissionHistoryRepository commissionHistoryRepository;

    @Mock
    private CommissionCalculator commissionCalculator;

    @Mock
    private ManagerSplitService managerSplitService;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private CommissionCalculationService commissionCalculationService;

    private CalculateCommissionRequest testRequest;
    private CommissionScale testScale;
    private CommissionCalculationResult testResult;
    private Commission testCommission;

    @BeforeEach
    void setUp() {
        // Setup test request
        testRequest = CalculateCommissionRequest.builder()
                .orderId(1L)
                .orderNumber("VN-12345678-ABC123")
                .orderType("VN")
                .orderTotalRevenue(BigDecimal.valueOf(30000))
                .orderNetMargin(BigDecimal.valueOf(5000))
                .businessUnitId(10L)
                .businessUnitName("Downtown Branch")
                .salespersonId(100L)
                .salespersonName("John Doe")
                .managerId(50L)
                .managerName("Jane Smith")
                .scaleCode("SCALE_VN_2024")
                .build();

        // Setup test scale
        testScale = CommissionScale.builder()
                .id(1L)
                .code("SCALE_VN_2024")
                .name("VN Commission Scale 2024")
                .orderType("VN")
                .calculationType("PERCENTAGE")
                .baseRate(BigDecimal.valueOf(3.5))
                .managerSplitEnabled(true)
                .managerSplitPercentage(BigDecimal.valueOf(20))
                .isActive(true)
                .build();

        // Setup test calculation result
        testResult = CommissionCalculationResult.builder()
                .baseCommission(BigDecimal.valueOf(1050.00))
                .bonusCommission(BigDecimal.ZERO)
                .totalCommissionExclTax(BigDecimal.valueOf(1050.00))
                .totalCommissionInclTax(BigDecimal.valueOf(1270.50))
                .taxAmount(BigDecimal.valueOf(220.50))
                .taxRate(BigDecimal.valueOf(21))
                .breakdown(new java.util.HashMap<>())
                .build();

        // Setup test commission
        testCommission = Commission.builder()
                .id(1L)
                .orderId(1L)
                .orderNumber("VN-12345678-ABC123")
                .orderType("VN")
                .status(CommissionStatus.CALCULATED)
                .baseCommission(BigDecimal.valueOf(1050.00))
                .totalCommissionExclTax(BigDecimal.valueOf(1050.00))
                .totalCommissionInclTax(BigDecimal.valueOf(1270.50))
                .taxAmount(BigDecimal.valueOf(220.50))
                .commissionScale(testScale)
                .salespersonId(100L)
                .build();
    }

    @Test
    @DisplayName("Calculate Commission - Success")
    void calculateCommission_Success() {
        // Arrange
        when(commissionRepository.findByOrderId(1L)).thenReturn(Optional.empty());
        when(commissionScaleRepository.findApplicableScale(any(), any(), any(), any()))
                .thenReturn(Optional.of(testScale));
        when(commissionCalculator.calculate(any(CommissionCalculationContext.class)))
                .thenReturn(testResult);
        when(commissionRepository.save(any(Commission.class)))
                .thenReturn(testCommission);
        doNothing().when(managerSplitService).applyManagerSplit(any(), any());
        when(commissionHistoryRepository.save(any(CommissionHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Commission result = commissionCalculationService.calculateCommission(testRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(1L);
        assertThat(result.getOrderNumber()).isEqualTo("VN-12345678-ABC123");

        verify(commissionRepository).findByOrderId(1L);
        verify(commissionCalculator).calculate(any(CommissionCalculationContext.class));
        verify(managerSplitService).applyManagerSplit(any(Commission.class), eq(BigDecimal.valueOf(20)));
        verify(commissionRepository).save(any(Commission.class));
        verify(commissionHistoryRepository).save(any(CommissionHistory.class));
        verify(kafkaTemplate).send(eq("commission.calculated"), any(), any());
    }

    @Test
    @DisplayName("Calculate Commission - Throws Exception If Already Exists")
    void calculateCommission_ThrowsExceptionIfAlreadyExists() {
        // Arrange
        when(commissionRepository.findByOrderId(1L))
                .thenReturn(Optional.of(testCommission));

        // Act & Assert
        assertThatThrownBy(() -> commissionCalculationService.calculateCommission(testRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Commission already exists for order");

        verify(commissionRepository).findByOrderId(1L);
        verify(commissionCalculator, never()).calculate(any());
        verify(commissionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Calculate Commission - Without Manager Split")
    void calculateCommission_WithoutManagerSplit() {
        // Arrange
        testScale.setManagerSplitEnabled(false);
        testRequest.setManagerId(null);

        when(commissionRepository.findByOrderId(1L)).thenReturn(Optional.empty());
        when(commissionScaleRepository.findApplicableScale(any(), any(), any(), any()))
                .thenReturn(Optional.of(testScale));
        when(commissionCalculator.calculate(any(CommissionCalculationContext.class)))
                .thenReturn(testResult);
        when(commissionRepository.save(any(Commission.class)))
                .thenReturn(testCommission);

        // Act
        Commission result = commissionCalculationService.calculateCommission(testRequest);

        // Assert
        assertThat(result).isNotNull();
        verify(managerSplitService, never()).applyManagerSplit(any(), any());
    }

    @Test
    @DisplayName("Calculate Commission - Creates History Record")
    void calculateCommission_CreatesHistoryRecord() {
        // Arrange
        when(commissionRepository.findByOrderId(1L)).thenReturn(Optional.empty());
        when(commissionScaleRepository.findApplicableScale(any(), any(), any(), any()))
                .thenReturn(Optional.of(testScale));
        when(commissionCalculator.calculate(any(CommissionCalculationContext.class)))
                .thenReturn(testResult);
        when(commissionRepository.save(any(Commission.class)))
                .thenReturn(testCommission);

        ArgumentCaptor<CommissionHistory> historyCaptor = ArgumentCaptor.forClass(CommissionHistory.class);
        when(commissionHistoryRepository.save(historyCaptor.capture()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        commissionCalculationService.calculateCommission(testRequest);

        // Assert
        CommissionHistory history = historyCaptor.getValue();
        assertThat(history).isNotNull();
        assertThat(history.getAction()).isEqualTo("Commission calculated");
    }

    @Test
    @DisplayName("Calculate Commission - Publishes Kafka Event")
    void calculateCommission_PublishesKafkaEvent() {
        // Arrange
        when(commissionRepository.findByOrderId(1L)).thenReturn(Optional.empty());
        when(commissionScaleRepository.findApplicableScale(any(), any(), any(), any()))
                .thenReturn(Optional.of(testScale));
        when(commissionCalculator.calculate(any(CommissionCalculationContext.class)))
                .thenReturn(testResult);
        when(commissionRepository.save(any(Commission.class)))
                .thenReturn(testCommission);

        // Act
        commissionCalculationService.calculateCommission(testRequest);

        // Assert
        verify(kafkaTemplate).send(eq("commission.calculated"), any(), any());
    }

    @Test
    @DisplayName("Recalculate Commission - Success")
    void recalculateCommission_Success() {
        // Arrange
        testCommission.setStatus(CommissionStatus.CALCULATED);
        testCommission.setPaid(false);
        testCommission.setManagerSplitEnabled(true);
        testCommission.setManagerId(50L);
        testCommission.setManagerSplitPercentage(BigDecimal.valueOf(20));

        when(commissionRepository.findById(1L)).thenReturn(Optional.of(testCommission));
        when(commissionCalculator.calculate(any(CommissionCalculationContext.class)))
                .thenReturn(testResult);
        when(commissionRepository.save(any(Commission.class)))
                .thenReturn(testCommission);
        doNothing().when(managerSplitService).applyManagerSplit(any(), any());

        // Act
        Commission result = commissionCalculationService.recalculateCommission(1L);

        // Assert
        assertThat(result).isNotNull();
        verify(commissionRepository).findById(1L);
        verify(commissionCalculator).calculate(any(CommissionCalculationContext.class));
        verify(managerSplitService).applyManagerSplit(any(), eq(BigDecimal.valueOf(20)));
        verify(commissionRepository).save(any(Commission.class));
        verify(commissionHistoryRepository).save(any(CommissionHistory.class));
    }

    @Test
    @DisplayName("Recalculate Commission - Throws Exception If Paid")
    void recalculateCommission_ThrowsExceptionIfPaid() {
        // Arrange
        testCommission.setPaid(true);
        when(commissionRepository.findById(1L)).thenReturn(Optional.of(testCommission));

        // Act & Assert
        assertThatThrownBy(() -> commissionCalculationService.recalculateCommission(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot recalculate paid commission");

        verify(commissionRepository).findById(1L);
        verify(commissionCalculator, never()).calculate(any());
        verify(commissionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Recalculate Commission - Throws Exception If Not Found")
    void recalculateCommission_ThrowsExceptionIfNotFound() {
        // Arrange
        when(commissionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> commissionCalculationService.recalculateCommission(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Commission not found");

        verify(commissionRepository).findById(999L);
        verify(commissionCalculator, never()).calculate(any());
    }

    @Test
    @DisplayName("Recalculate Commission - Creates History With Old and New Amounts")
    void recalculateCommission_CreatesHistoryWithOldAndNewAmounts() {
        // Arrange
        testCommission.setStatus(CommissionStatus.CALCULATED);
        testCommission.setPaid(false);
        BigDecimal oldAmount = BigDecimal.valueOf(1000.00);
        testCommission.setTotalCommissionExclTax(oldAmount);

        when(commissionRepository.findById(1L)).thenReturn(Optional.of(testCommission));
        when(commissionCalculator.calculate(any(CommissionCalculationContext.class)))
                .thenReturn(testResult);
        when(commissionRepository.save(any(Commission.class)))
                .thenReturn(testCommission);

        ArgumentCaptor<CommissionHistory> historyCaptor = ArgumentCaptor.forClass(CommissionHistory.class);
        when(commissionHistoryRepository.save(historyCaptor.capture()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        commissionCalculationService.recalculateCommission(1L);

        // Assert
        CommissionHistory history = historyCaptor.getValue();
        assertThat(history).isNotNull();
        assertThat(history.getAction()).isEqualTo("Commission recalculated");
        assertThat(history.getNotes()).contains("Old amount");
        assertThat(history.getNotes()).contains("New amount");
    }

    @Test
    @DisplayName("Recalculate Commission - Resets Status to Calculated")
    void recalculateCommission_ResetsStatusToCalculated() {
        // Arrange
        testCommission.setStatus(CommissionStatus.VALIDATED);
        testCommission.setPaid(false);

        when(commissionRepository.findById(1L)).thenReturn(Optional.of(testCommission));
        when(commissionCalculator.calculate(any(CommissionCalculationContext.class)))
                .thenReturn(testResult);

        ArgumentCaptor<Commission> commissionCaptor = ArgumentCaptor.forClass(Commission.class);
        when(commissionRepository.save(commissionCaptor.capture()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        commissionCalculationService.recalculateCommission(1L);

        // Assert
        Commission savedCommission = commissionCaptor.getValue();
        assertThat(savedCommission.getStatus()).isEqualTo(CommissionStatus.CALCULATED);
        assertThat(savedCommission.getCalculationDate()).isNotNull();
    }

    @Test
    @DisplayName("Build Calculation Context - Contains All Required Fields")
    void buildCalculationContext_ContainsAllRequiredFields() {
        // Arrange
        when(commissionRepository.findByOrderId(1L)).thenReturn(Optional.empty());
        when(commissionScaleRepository.findApplicableScale(any(), any(), any(), any()))
                .thenReturn(Optional.of(testScale));

        ArgumentCaptor<CommissionCalculationContext> contextCaptor =
                ArgumentCaptor.forClass(CommissionCalculationContext.class);
        when(commissionCalculator.calculate(contextCaptor.capture()))
                .thenReturn(testResult);
        when(commissionRepository.save(any(Commission.class)))
                .thenReturn(testCommission);

        // Act
        commissionCalculationService.calculateCommission(testRequest);

        // Assert
        CommissionCalculationContext context = contextCaptor.getValue();
        assertThat(context.getOrderId()).isEqualTo(1L);
        assertThat(context.getOrderNumber()).isEqualTo("VN-12345678-ABC123");
        assertThat(context.getOrderType()).isEqualTo("VN");
        assertThat(context.getOrderTotalRevenue()).isEqualTo(BigDecimal.valueOf(30000));
        assertThat(context.getOrderNetMargin()).isEqualTo(BigDecimal.valueOf(5000));
        assertThat(context.getSalespersonId()).isEqualTo(100L);
        assertThat(context.getBusinessUnitId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Calculate Commission - Handles Scale Not Found")
    void calculateCommission_HandlesScaleNotFound() {
        // Arrange
        when(commissionRepository.findByOrderId(1L)).thenReturn(Optional.empty());
        when(commissionScaleRepository.findApplicableScale(any(), any(), any(), any()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> commissionCalculationService.calculateCommission(testRequest))
                .isInstanceOf(Exception.class);

        verify(commissionRepository).findByOrderId(1L);
        verify(commissionCalculator, never()).calculate(any());
    }
}
