package com.eflo.workflow.service;

import com.eflo.workflow.domain.entity.WorkflowInstance;
import com.eflo.workflow.domain.entity.WorkflowInstanceTask;
import com.eflo.workflow.domain.enums.EventType;
import com.eflo.workflow.domain.repository.WorkflowInstanceRepository;
import com.eflo.workflow.domain.repository.WorkflowInstanceTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeadlineMonitoringService Unit Tests")
class DeadlineMonitoringServiceTest {

    @Mock
    private WorkflowInstanceRepository instanceRepository;

    @Mock
    private WorkflowInstanceTaskRepository taskRepository;

    @Mock
    private WorkflowEventPublisher eventPublisher;

    @InjectMocks
    private DeadlineMonitoringService deadlineMonitoringService;

    private WorkflowInstance testInstance;
    private WorkflowInstanceTask testTask;

    @BeforeEach
    void setUp() {
        testInstance = WorkflowInstance.builder()
                .id("instance-123")
                .orderId(1L)
                .isOverdue(false)
                .expectedCompletionDate(LocalDateTime.now().minusDays(1))
                .build();

        testTask = WorkflowInstanceTask.builder()
                .id("task-123")
                .instance(testInstance)
                .taskName("Review Order")
                .isOverdue(false)
                .dueDate(LocalDateTime.now().minusDays(2))
                .build();
    }

    @Test
    @DisplayName("Check Overdue Instances - Marks Instance as Overdue")
    void checkOverdueInstances_MarksInstanceAsOverdue() {
        // Arrange
        when(instanceRepository.findInstancesExceedingDeadline(any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(testInstance));
        when(instanceRepository.save(any(WorkflowInstance.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<WorkflowInstance> instanceCaptor = ArgumentCaptor.forClass(WorkflowInstance.class);

        // Act
        deadlineMonitoringService.checkOverdueInstances();

        // Assert
        verify(instanceRepository).save(instanceCaptor.capture());
        WorkflowInstance savedInstance = instanceCaptor.getValue();
        assertThat(savedInstance.getIsOverdue()).isTrue();
        assertThat(savedInstance.getOverdueSince()).isNotNull();
    }

    @Test
    @DisplayName("Check Overdue Instances - Publishes Overdue Event")
    void checkOverdueInstances_PublishesOverdueEvent() {
        // Arrange
        when(instanceRepository.findInstancesExceedingDeadline(any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(testInstance));
        when(instanceRepository.save(any(WorkflowInstance.class)))
                .thenReturn(testInstance);

        // Act
        deadlineMonitoringService.checkOverdueInstances();

        // Assert
        verify(eventPublisher).publishDeadlineEvent(
                eq(EventType.INSTANCE_OVERDUE),
                eq("instance-123"),
                eq(1L),
                eq("Instance exceeded expected completion date")
        );
    }

    @Test
    @DisplayName("Check Overdue Instances - Does Not Republish If Already Overdue")
    void checkOverdueInstances_DoesNotRepublishIfAlreadyOverdue() {
        // Arrange
        testInstance.setIsOverdue(true);
        testInstance.setOverdueSince(LocalDateTime.now().minusDays(1));
        when(instanceRepository.findInstancesExceedingDeadline(any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(testInstance));

        // Act
        deadlineMonitoringService.checkOverdueInstances();

        // Assert
        verify(instanceRepository, never()).save(any());
        verify(eventPublisher, never()).publishDeadlineEvent(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Check Overdue Instances - Processes Multiple Instances")
    void checkOverdueInstances_ProcessesMultipleInstances() {
        // Arrange
        WorkflowInstance instance1 = testInstance;
        WorkflowInstance instance2 = WorkflowInstance.builder()
                .id("instance-456")
                .orderId(2L)
                .isOverdue(false)
                .expectedCompletionDate(LocalDateTime.now().minusDays(3))
                .build();

        when(instanceRepository.findInstancesExceedingDeadline(any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(instance1, instance2));
        when(instanceRepository.save(any(WorkflowInstance.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        deadlineMonitoringService.checkOverdueInstances();

        // Assert
        verify(instanceRepository, times(2)).save(any(WorkflowInstance.class));
        verify(eventPublisher, times(2)).publishDeadlineEvent(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Check Overdue Instances - Handles Empty List")
    void checkOverdueInstances_HandlesEmptyList() {
        // Arrange
        when(instanceRepository.findInstancesExceedingDeadline(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // Act
        deadlineMonitoringService.checkOverdueInstances();

        // Assert
        verify(instanceRepository, never()).save(any());
        verify(eventPublisher, never()).publishDeadlineEvent(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Check Overdue Tasks - Marks Task as Overdue")
    void checkOverdueTasks_MarksTaskAsOverdue() {
        // Arrange
        when(taskRepository.findTasksExceedingDeadline(any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(testTask));
        when(taskRepository.save(any(WorkflowInstanceTask.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<WorkflowInstanceTask> taskCaptor = ArgumentCaptor.forClass(WorkflowInstanceTask.class);

        // Act
        deadlineMonitoringService.checkOverdueTasks();

        // Assert
        verify(taskRepository).save(taskCaptor.capture());
        WorkflowInstanceTask savedTask = taskCaptor.getValue();
        assertThat(savedTask.getIsOverdue()).isTrue();
        assertThat(savedTask.getOverdueSince()).isNotNull();
    }

    @Test
    @DisplayName("Check Overdue Tasks - Publishes Task Overdue Event")
    void checkOverdueTasks_PublishesTaskOverdueEvent() {
        // Arrange
        when(taskRepository.findTasksExceedingDeadline(any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(testTask));
        when(taskRepository.save(any(WorkflowInstanceTask.class)))
                .thenReturn(testTask);

        // Act
        deadlineMonitoringService.checkOverdueTasks();

        // Assert
        verify(eventPublisher).publishDeadlineEvent(
                eq(EventType.TASK_OVERDUE),
                eq("instance-123"),
                eq(1L),
                contains("Review Order")
        );
    }

    @Test
    @DisplayName("Check Overdue Tasks - Does Not Republish If Already Overdue")
    void checkOverdueTasks_DoesNotRepublishIfAlreadyOverdue() {
        // Arrange
        testTask.setIsOverdue(true);
        testTask.setOverdueSince(LocalDateTime.now().minusDays(1));
        when(taskRepository.findTasksExceedingDeadline(any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(testTask));

        // Act
        deadlineMonitoringService.checkOverdueTasks();

        // Assert
        verify(taskRepository, never()).save(any());
        verify(eventPublisher, never()).publishDeadlineEvent(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Check Overdue Tasks - Processes Multiple Tasks")
    void checkOverdueTasks_ProcessesMultipleTasks() {
        // Arrange
        WorkflowInstanceTask task1 = testTask;
        WorkflowInstanceTask task2 = WorkflowInstanceTask.builder()
                .id("task-456")
                .instance(testInstance)
                .taskName("Approve Order")
                .isOverdue(false)
                .dueDate(LocalDateTime.now().minusDays(1))
                .build();

        when(taskRepository.findTasksExceedingDeadline(any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(task1, task2));
        when(taskRepository.save(any(WorkflowInstanceTask.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        deadlineMonitoringService.checkOverdueTasks();

        // Assert
        verify(taskRepository, times(2)).save(any(WorkflowInstanceTask.class));
        verify(eventPublisher, times(2)).publishDeadlineEvent(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Check Overdue Tasks - Handles Empty List")
    void checkOverdueTasks_HandlesEmptyList() {
        // Arrange
        when(taskRepository.findTasksExceedingDeadline(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // Act
        deadlineMonitoringService.checkOverdueTasks();

        // Assert
        verify(taskRepository, never()).save(any());
        verify(eventPublisher, never()).publishDeadlineEvent(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Get Overdue Instance Count - Returns Count")
    void getOverdueInstanceCount_ReturnsCount() {
        // Arrange
        when(instanceRepository.countOverdueInstances()).thenReturn(5L);

        // Act
        long count = deadlineMonitoringService.getOverdueInstanceCount();

        // Assert
        assertThat(count).isEqualTo(5L);
        verify(instanceRepository).countOverdueInstances();
    }

    @Test
    @DisplayName("Get Overdue Task Count - Returns Count")
    void getOverdueTaskCount_ReturnsCount() {
        // Arrange
        when(taskRepository.countOverdueTasks()).thenReturn(10L);

        // Act
        long count = deadlineMonitoringService.getOverdueTaskCount();

        // Assert
        assertThat(count).isEqualTo(10L);
        verify(taskRepository).countOverdueTasks();
    }

    @Test
    @DisplayName("Set Overdue Since - Records Correct Timestamp")
    void setOverdueSince_RecordsCorrectTimestamp() {
        // Arrange
        LocalDateTime beforeTest = LocalDateTime.now();
        when(instanceRepository.findInstancesExceedingDeadline(any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(testInstance));

        ArgumentCaptor<WorkflowInstance> instanceCaptor = ArgumentCaptor.forClass(WorkflowInstance.class);
        when(instanceRepository.save(instanceCaptor.capture())).thenReturn(testInstance);

        // Act
        deadlineMonitoringService.checkOverdueInstances();
        LocalDateTime afterTest = LocalDateTime.now();

        // Assert
        WorkflowInstance savedInstance = instanceCaptor.getValue();
        assertThat(savedInstance.getOverdueSince())
                .isAfterOrEqualTo(beforeTest)
                .isBeforeOrEqualTo(afterTest);
    }
}
