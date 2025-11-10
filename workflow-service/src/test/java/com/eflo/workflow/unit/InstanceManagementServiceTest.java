package com.eflo.workflow.unit;

import com.eflo.workflow.domain.entity.WorkflowInstance;
import com.eflo.workflow.domain.entity.WorkflowProcess;
import com.eflo.workflow.domain.entity.WorkflowState;
import com.eflo.workflow.domain.enums.InstanceStatus;
import com.eflo.workflow.domain.enums.Priority;
import com.eflo.workflow.domain.enums.StateType;
import com.eflo.workflow.domain.model.request.CreateInstanceRequest;
import com.eflo.workflow.domain.model.response.InstanceResponse;
import com.eflo.workflow.domain.repository.*;
import com.eflo.workflow.mapper.WorkflowMapper;
import com.eflo.workflow.service.InstanceManagementService;
import com.eflo.workflow.service.TaskManagementService;
import com.eflo.workflow.service.WorkflowEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for InstanceManagementService
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class InstanceManagementServiceTest {

    @Mock
    private WorkflowInstanceRepository instanceRepository;

    @Mock
    private WorkflowProcessRepository processRepository;

    @Mock
    private WorkflowStateRepository stateRepository;

    @Mock
    private WorkflowHistoryRepository historyRepository;

    @Mock
    private WorkflowMapper mapper;

    @Mock
    private WorkflowEventPublisher eventPublisher;

    @Mock
    private TaskManagementService taskManagementService;

    @InjectMocks
    private InstanceManagementService instanceManagementService;

    private WorkflowProcess testProcess;
    private WorkflowState testStartState;
    private WorkflowInstance testInstance;

    @BeforeEach
    void setUp() {
        testProcess = WorkflowProcess.builder()
                .id(1L)
                .processCode("VN_STANDARD")
                .processName("VN Standard Workflow")
                .orderType("VN")
                .maxDurationDays(30)
                .isActive(true)
                .build();

        testStartState = WorkflowState.builder()
                .id(1L)
                .process(testProcess)
                .stateCode("START")
                .stateName("Start State")
                .stateType(StateType.START)
                .build();

        testInstance = WorkflowInstance.builder()
                .id(1L)
                .process(testProcess)
                .orderId(100L)
                .instanceStatus(InstanceStatus.CREATED)
                .priority(Priority.NORMAL)
                .build();
    }

    @Test
    void shouldCreateInstanceSuccessfully() {
        // Given
        CreateInstanceRequest request = CreateInstanceRequest.builder()
                .processId(1L)
                .orderId(100L)
                .priority(Priority.NORMAL)
                .build();

        when(processRepository.findById(1L)).thenReturn(Optional.of(testProcess));
        when(mapper.toInstance(request)).thenReturn(testInstance);
        when(instanceRepository.save(any(WorkflowInstance.class))).thenReturn(testInstance);
        when(mapper.toInstanceResponse(testInstance)).thenReturn(
                InstanceResponse.builder()
                        .id(1L)
                        .orderId(100L)
                        .instanceStatus(InstanceStatus.CREATED)
                        .build()
        );

        // When
        InstanceResponse response = instanceManagementService.createInstance(request, "testUser");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getOrderId()).isEqualTo(100L);
        assertThat(response.getInstanceStatus()).isEqualTo(InstanceStatus.CREATED);

        verify(processRepository).findById(1L);
        verify(instanceRepository, times(2)).save(any(WorkflowInstance.class));
        verify(eventPublisher).publishInstanceEvent(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldStartInstanceSuccessfully() {
        // Given
        when(instanceRepository.findById(1L)).thenReturn(Optional.of(testInstance));
        when(stateRepository.findStartStateByProcessId(1L)).thenReturn(Optional.of(testStartState));
        when(instanceRepository.save(any(WorkflowInstance.class))).thenReturn(testInstance);
        when(mapper.toInstanceResponse(testInstance)).thenReturn(
                InstanceResponse.builder()
                        .id(1L)
                        .instanceStatus(InstanceStatus.RUNNING)
                        .build()
        );

        // When
        InstanceResponse response = instanceManagementService.startInstance(1L, "testUser");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getInstanceStatus()).isEqualTo(InstanceStatus.RUNNING);

        verify(taskManagementService).createTasksForState(any(), any(), any());
        verify(eventPublisher).publishInstanceEvent(any(), any(), any(), any(), any(), any(), any(), any());
    }
}
