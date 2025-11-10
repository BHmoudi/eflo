package com.eflo.user.service;

import com.eflo.user.domain.entity.BusinessUnit;
import com.eflo.user.domain.entity.Hierarchy;
import com.eflo.user.domain.entity.User;
import com.eflo.user.domain.enums.ActivityType;
import com.eflo.user.domain.enums.BusinessUnitType;
import com.eflo.user.domain.repository.BusinessUnitRepository;
import com.eflo.user.domain.repository.HierarchyRepository;
import com.eflo.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HierarchyServiceTest {

    @Mock
    private HierarchyRepository hierarchyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BusinessUnitRepository businessUnitRepository;

    @Mock
    private UserActivityService userActivityService;

    @InjectMocks
    private HierarchyService hierarchyService;

    private User employee;
    private User manager;
    private BusinessUnit businessUnit;
    private Hierarchy testHierarchy;

    @BeforeEach
    void setUp() {
        employee = new User();
        employee.setId(1L);
        employee.setKeycloakId(UUID.randomUUID());
        employee.setEmail("employee@example.com");
        employee.setFirstName("Employee");
        employee.setLastName("User");
        employee.setActive(true);

        manager = new User();
        manager.setId(2L);
        manager.setKeycloakId(UUID.randomUUID());
        manager.setEmail("manager@example.com");
        manager.setFirstName("Manager");
        manager.setLastName("User");
        manager.setActive(true);

        businessUnit = new BusinessUnit();
        businessUnit.setId(1L);
        businessUnit.setCode("DEPT001");
        businessUnit.setName("Test Department");
        businessUnit.setType(BusinessUnitType.DEPARTMENT);
        businessUnit.setActive(true);

        testHierarchy = new Hierarchy();
        testHierarchy.setId(1L);
        testHierarchy.setEmployee(employee);
        testHierarchy.setManager(manager);
        testHierarchy.setBusinessUnit(businessUnit);
        testHierarchy.setLevel(1);
        testHierarchy.setActive(true);
    }

    @Test
    void createHierarchy_ShouldCreateSuccessfully_WhenValidData() {
        // Given
        Long employeeId = 1L;
        Long managerId = 2L;
        Long businessUnitId = 1L;
        String performedBy = "admin";

        when(userRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));
        when(businessUnitRepository.findById(businessUnitId)).thenReturn(Optional.of(businessUnit));
        when(hierarchyRepository.findActiveByEmployeeIdAndManagerId(employeeId, managerId))
            .thenReturn(Optional.empty());
        when(hierarchyRepository.save(any(Hierarchy.class))).thenReturn(testHierarchy);

        // When
        Hierarchy created = hierarchyService.createHierarchy(employeeId, managerId, businessUnitId, 1, performedBy);

        // Then
        assertThat(created).isNotNull();
        verify(hierarchyRepository).save(any(Hierarchy.class));
        verify(userActivityService).logActivity(any(User.class), eq(ActivityType.HIERARCHY_CHANGE), anyString(), eq(performedBy), any());
    }

    @Test
    void createHierarchy_ShouldThrowException_WhenEmployeeNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> hierarchyService.createHierarchy(1L, 2L, 1L, 1, "admin"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Employee not found");

        verify(hierarchyRepository, never()).save(any());
    }

    @Test
    void createHierarchy_ShouldThrowException_WhenManagerNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> hierarchyService.createHierarchy(1L, 2L, 1L, 1, "admin"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Manager not found");
    }

    @Test
    void createHierarchy_ShouldThrowException_WhenHierarchyAlreadyExists() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(userRepository.findById(2L)).thenReturn(Optional.of(manager));
        when(businessUnitRepository.findById(1L)).thenReturn(Optional.of(businessUnit));
        when(hierarchyRepository.findActiveByEmployeeIdAndManagerId(1L, 2L))
            .thenReturn(Optional.of(testHierarchy));

        // When/Then
        assertThatThrownBy(() -> hierarchyService.createHierarchy(1L, 2L, 1L, 1, "admin"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("already exists");
    }

    @Test
    void updateHierarchyLevel_ShouldUpdateSuccessfully() {
        // Given
        Long hierarchyId = 1L;
        Integer newLevel = 2;
        String performedBy = "admin";

        when(hierarchyRepository.findById(hierarchyId)).thenReturn(Optional.of(testHierarchy));
        when(hierarchyRepository.save(any(Hierarchy.class))).thenReturn(testHierarchy);

        // When
        Hierarchy updated = hierarchyService.updateHierarchyLevel(hierarchyId, newLevel, performedBy);

        // Then
        assertThat(updated).isNotNull();
        assertThat(updated.getLevel()).isEqualTo(newLevel);
        verify(hierarchyRepository).save(testHierarchy);
    }

    @Test
    void getSubordinates_ShouldReturnSubordinates() {
        // Given
        Long managerId = 2L;
        List<Hierarchy> subordinates = Arrays.asList(testHierarchy);

        when(hierarchyRepository.findByManagerIdAndIsActiveTrue(managerId)).thenReturn(subordinates);

        // When
        List<Hierarchy> result = hierarchyService.getSubordinates(managerId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmployee().getId()).isEqualTo(1L);
    }

    @Test
    void getManagersForEmployee_ShouldReturnManagers() {
        // Given
        Long employeeId = 1L;
        List<Hierarchy> managers = Arrays.asList(testHierarchy);

        when(hierarchyRepository.findActiveByEmployeeId(employeeId)).thenReturn(managers);

        // When
        List<Hierarchy> result = hierarchyService.getManagersForEmployee(employeeId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getManager().getId()).isEqualTo(2L);
    }

    @Test
    void getHierarchyForBusinessUnit_ShouldReturnHierarchies() {
        // Given
        Long businessUnitId = 1L;
        List<Hierarchy> hierarchies = Arrays.asList(testHierarchy);

        when(hierarchyRepository.findByBusinessUnitIdAndIsActiveTrue(businessUnitId)).thenReturn(hierarchies);

        // When
        List<Hierarchy> result = hierarchyService.getHierarchyForBusinessUnit(businessUnitId);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    void countSubordinates_ShouldReturnCorrectCount() {
        // Given
        Long managerId = 2L;
        when(hierarchyRepository.countActiveSubordinatesByManagerId(managerId)).thenReturn(5L);

        // When
        long count = hierarchyService.countSubordinates(managerId);

        // Then
        assertThat(count).isEqualTo(5);
    }

    @Test
    void deactivateHierarchy_ShouldDeactivateSuccessfully() {
        // Given
        Long hierarchyId = 1L;
        String performedBy = "admin";

        when(hierarchyRepository.findById(hierarchyId)).thenReturn(Optional.of(testHierarchy));
        when(hierarchyRepository.save(any(Hierarchy.class))).thenReturn(testHierarchy);

        // When
        hierarchyService.deactivateHierarchy(hierarchyId, performedBy);

        // Then
        assertThat(testHierarchy.getIsActive()).isFalse();
        verify(hierarchyRepository).save(testHierarchy);
        verify(userActivityService).logActivity(any(User.class), eq(ActivityType.HIERARCHY_CHANGE), anyString(), eq(performedBy), any());
    }

    @Test
    void changeManager_ShouldUpdateManagerSuccessfully() {
        // Given
        Long hierarchyId = 1L;
        Long newManagerId = 3L;
        String performedBy = "admin";

        User newManager = new User();
        newManager.setId(newManagerId);
        newManager.setEmail("newmanager@example.com");
        newManager.setActive(true);

        when(hierarchyRepository.findById(hierarchyId)).thenReturn(Optional.of(testHierarchy));
        when(userRepository.findById(newManagerId)).thenReturn(Optional.of(newManager));
        when(hierarchyRepository.save(any(Hierarchy.class))).thenReturn(testHierarchy);

        // When
        Hierarchy updated = hierarchyService.changeManager(hierarchyId, newManagerId, performedBy);

        // Then
        assertThat(updated).isNotNull();
        assertThat(updated.getManager().getId()).isEqualTo(newManagerId);
        verify(hierarchyRepository).save(testHierarchy);
    }

    @Test
    void getHierarchiesByLevel_ShouldReturnHierarchiesAtLevel() {
        // Given
        Long employeeId = 1L;
        Integer level = 1;
        List<Hierarchy> hierarchies = Arrays.asList(testHierarchy);

        when(hierarchyRepository.findActiveByEmployeeIdAndLevel(employeeId, level)).thenReturn(hierarchies);

        // When
        List<Hierarchy> result = hierarchyService.getHierarchiesByLevel(employeeId, level);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLevel()).isEqualTo(level);
    }
}
