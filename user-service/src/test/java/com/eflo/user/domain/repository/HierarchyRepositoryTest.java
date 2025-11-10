package com.eflo.user.domain.repository;

import com.eflo.user.BaseRepositoryTest;
import com.eflo.user.domain.entity.BusinessUnit;
import com.eflo.user.domain.entity.Hierarchy;
import com.eflo.user.domain.entity.User;
import com.eflo.user.domain.enums.BusinessUnitType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HierarchyRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private HierarchyRepository hierarchyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusinessUnitRepository businessUnitRepository;

    private User manager;
    private User employee;
    private BusinessUnit businessUnit;
    private Hierarchy testHierarchy;

    @BeforeEach
    void setUp() {
        hierarchyRepository.deleteAll();
        userRepository.deleteAll();
        businessUnitRepository.deleteAll();

        // Create test manager
        manager = new User();
        manager.setKeycloakId(UUID.randomUUID());
        manager.setEmail("manager@example.com");
        manager.setFirstName("Manager");
        manager.setLastName("User");
        manager.setActive(true);
        manager = userRepository.save(manager);

        // Create test employee
        employee = new User();
        employee.setKeycloakId(UUID.randomUUID());
        employee.setEmail("employee@example.com");
        employee.setFirstName("Employee");
        employee.setLastName("User");
        employee.setActive(true);
        employee = userRepository.save(employee);

        // Create test business unit
        businessUnit = new BusinessUnit();
        businessUnit.setCode("DEPT001");
        businessUnit.setName("Test Department");
        businessUnit.setType(BusinessUnitType.DEPARTMENT);
        businessUnit.setActive(true);
        businessUnit = businessUnitRepository.save(businessUnit);

        // Create test hierarchy
        testHierarchy = new Hierarchy();
        testHierarchy.setEmployee(employee);
        testHierarchy.setManager(manager);
        testHierarchy.setBusinessUnit(businessUnit);
        testHierarchy.setLevel(1);
        testHierarchy.setActive(true);
        testHierarchy = hierarchyRepository.save(testHierarchy);
    }

    @Test
    void findByEmployeeId_ShouldReturnHierarchies() {
        // When
        List<Hierarchy> hierarchies = hierarchyRepository.findByEmployeeId(employee.getId());

        // Then
        assertThat(hierarchies).hasSize(1);
        assertThat(hierarchies.get(0).getManager().getEmail()).isEqualTo("manager@example.com");
    }

    @Test
    void findByManagerId_ShouldReturnSubordinates() {
        // When
        List<Hierarchy> subordinates = hierarchyRepository.findByManagerId(manager.getId());

        // Then
        assertThat(subordinates).hasSize(1);
        assertThat(subordinates.get(0).getEmployee().getEmail()).isEqualTo("employee@example.com");
    }

    @Test
    void findByManagerIdAndIsActiveTrue_ShouldReturnOnlyActiveSubordinates() {
        // Given
        User inactiveEmployee = new User();
        inactiveEmployee.setKeycloakId(UUID.randomUUID());
        inactiveEmployee.setEmail("inactive@example.com");
        inactiveEmployee.setFirstName("Inactive");
        inactiveEmployee.setLastName("Employee");
        inactiveEmployee.setActive(true);
        inactiveEmployee = userRepository.save(inactiveEmployee);

        Hierarchy inactiveHierarchy = new Hierarchy();
        inactiveHierarchy.setEmployee(inactiveEmployee);
        inactiveHierarchy.setManager(manager);
        inactiveHierarchy.setBusinessUnit(businessUnit);
        inactiveHierarchy.setLevel(1);
        inactiveHierarchy.setActive(false);
        hierarchyRepository.save(inactiveHierarchy);

        // When
        List<Hierarchy> activeSubordinates = hierarchyRepository.findByManagerIdAndIsActiveTrue(manager.getId());

        // Then
        assertThat(activeSubordinates).hasSize(1);
        assertThat(activeSubordinates.get(0).getEmployee().getEmail()).isEqualTo("employee@example.com");
    }

    @Test
    void findByBusinessUnitId_ShouldReturnHierarchiesForBusinessUnit() {
        // When
        List<Hierarchy> hierarchies = hierarchyRepository.findByBusinessUnitId(businessUnit.getId());

        // Then
        assertThat(hierarchies).hasSize(1);
    }

    @Test
    void findActiveByEmployeeId_ShouldReturnActiveHierarchies() {
        // Given
        testHierarchy.setActive(false);
        hierarchyRepository.save(testHierarchy);

        // When
        List<Hierarchy> active = hierarchyRepository.findActiveByEmployeeId(employee.getId());

        // Then
        assertThat(active).isEmpty();
    }

    @Test
    void findByEmployeeIdAndManagerId_ShouldReturnSpecificHierarchy() {
        // When
        Optional<Hierarchy> found = hierarchyRepository
            .findByEmployeeIdAndManagerId(employee.getId(), manager.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getLevel()).isEqualTo(1);
    }

    @Test
    void findActiveByEmployeeIdAndManagerId_ShouldReturnActiveHierarchy() {
        // When
        Optional<Hierarchy> found = hierarchyRepository
            .findActiveByEmployeeIdAndManagerId(employee.getId(), manager.getId());

        // Then
        assertThat(found).isPresent();
    }

    @Test
    void findActiveByEmployeeIdAndBusinessUnitId_ShouldReturnActiveHierarchy() {
        // When
        Optional<Hierarchy> found = hierarchyRepository
            .findActiveByEmployeeIdAndBusinessUnitId(employee.getId(), businessUnit.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getManager().getId()).isEqualTo(manager.getId());
    }

    @Test
    void countActiveSubordinatesByManagerId_ShouldReturnCorrectCount() {
        // Given
        User employee2 = new User();
        employee2.setKeycloakId(UUID.randomUUID());
        employee2.setEmail("employee2@example.com");
        employee2.setFirstName("Employee");
        employee2.setLastName("Two");
        employee2.setActive(true);
        employee2 = userRepository.save(employee2);

        Hierarchy hierarchy2 = new Hierarchy();
        hierarchy2.setEmployee(employee2);
        hierarchy2.setManager(manager);
        hierarchy2.setBusinessUnit(businessUnit);
        hierarchy2.setLevel(1);
        hierarchy2.setActive(true);
        hierarchyRepository.save(hierarchy2);

        // When
        long count = hierarchyRepository.countActiveSubordinatesByManagerId(manager.getId());

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void findActiveByEmployeeIdAndLevel_ShouldReturnHierarchiesAtLevel() {
        // When
        List<Hierarchy> level1 = hierarchyRepository.findActiveByEmployeeIdAndLevel(employee.getId(), 1);
        List<Hierarchy> level2 = hierarchyRepository.findActiveByEmployeeIdAndLevel(employee.getId(), 2);

        // Then
        assertThat(level1).hasSize(1);
        assertThat(level2).isEmpty();
    }
}
