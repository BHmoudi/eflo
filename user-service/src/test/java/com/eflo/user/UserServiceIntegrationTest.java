package com.eflo.user;

import com.eflo.user.domain.entity.BusinessUnit;
import com.eflo.user.domain.entity.User;
import com.eflo.user.domain.entity.UserBusinessUnit;
import com.eflo.user.domain.enums.BusinessUnitType;
import com.eflo.user.domain.repository.BusinessUnitRepository;
import com.eflo.user.domain.repository.UserBusinessUnitRepository;
import com.eflo.user.domain.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end integration test for User Service.
 * Tests complete workflows across repositories and services.
 */
class UserServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusinessUnitRepository businessUnitRepository;

    @Autowired
    private UserBusinessUnitRepository userBusinessUnitRepository;

    @AfterEach
    void cleanup() {
        userBusinessUnitRepository.deleteAll();
        userRepository.deleteAll();
        businessUnitRepository.deleteAll();
    }

    @Test
    @Transactional
    void completeUserWorkflow_ShouldCreateUserWithBusinessUnitAssignment() {
        // Given - Create a business unit
        BusinessUnit businessUnit = new BusinessUnit();
        businessUnit.setCode("IT-DEPT");
        businessUnit.setName("IT Department");
        businessUnit.setType(BusinessUnitType.DEPARTMENT);
        businessUnit.setRegionCode("US-EAST");
        businessUnit.setActive(true);
        businessUnit = businessUnitRepository.save(businessUnit);

        // When - Create a user
        User user = new User();
        user.setKeycloakId(UUID.randomUUID());
        user.setEmail("integration.test@example.com");
        user.setFirstName("Integration");
        user.setLastName("Test");
        user.setEmployeeNumber("EMP-INT-001");
        user.setDepartment("IT");
        user.setActive(true);
        user.setDeleted(false);
        user = userRepository.save(user);

        // And - Assign user to business unit
        UserBusinessUnit assignment = new UserBusinessUnit();
        assignment.setUser(user);
        assignment.setBusinessUnit(businessUnit);
        assignment.setRole("MANAGER");
        assignment.setPrimary(true);
        assignment = userBusinessUnitRepository.save(assignment);

        // Then - Verify complete workflow
        Optional<User> savedUser = userRepository.findByEmail("integration.test@example.com");
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getFirstName()).isEqualTo("Integration");
        assertThat(savedUser.get().getEmployeeNumber()).isEqualTo("EMP-INT-001");

        Optional<BusinessUnit> savedBU = businessUnitRepository.findByCode("IT-DEPT");
        assertThat(savedBU).isPresent();
        assertThat(savedBU.get().getName()).isEqualTo("IT Department");

        Optional<UserBusinessUnit> savedAssignment = userBusinessUnitRepository
                .findByUserIdAndBusinessUnitId(user.getId(), businessUnit.getId());
        assertThat(savedAssignment).isPresent();
        assertThat(savedAssignment.get().getRole()).isEqualTo("MANAGER");
        assertThat(savedAssignment.get().getIsPrimary()).isTrue();
    }

    @Test
    @Transactional
    void userSearchWorkflow_ShouldFindUsersByVariousCriteria() {
        // Given - Create multiple users
        User user1 = createTestUser("john.doe@example.com", "John", "Doe", "EMP001", "IT");
        User user2 = createTestUser("jane.smith@example.com", "Jane", "Smith", "EMP002", "IT");
        User user3 = createTestUser("bob.jones@example.com", "Bob", "Jones", "EMP003", "Sales");

        // When - Search by various criteria
        Optional<User> byEmail = userRepository.findByEmail("john.doe@example.com");
        Optional<User> byEmpNumber = userRepository.findByEmployeeNumber("EMP002");
        var byDepartment = userRepository.findByDepartment("IT");
        var searchResults = userRepository.searchUsers("jane");

        // Then
        assertThat(byEmail).isPresent();
        assertThat(byEmail.get().getFirstName()).isEqualTo("John");

        assertThat(byEmpNumber).isPresent();
        assertThat(byEmpNumber.get().getFirstName()).isEqualTo("Jane");

        assertThat(byDepartment).hasSize(2);
        assertThat(byDepartment).extracting(User::getDepartment).containsOnly("IT");

        assertThat(searchResults).hasSize(1);
        assertThat(searchResults.get(0).getFirstName()).isEqualTo("Jane");
    }

    @Test
    @Transactional
    void businessUnitHierarchy_ShouldMaintainRelationships() {
        // Given - Create business unit hierarchy
        BusinessUnit headquarters = createBusinessUnit("HQ", "Headquarters", BusinessUnitType.DEPARTMENT, "US-EAST");
        BusinessUnit salesDept = createBusinessUnit("SALES", "Sales Department", BusinessUnitType.DEPARTMENT, "US-EAST");
        BusinessUnit itDept = createBusinessUnit("IT", "IT Department", BusinessUnitType.DEPARTMENT, "US-EAST");

        // When - Create users and assign to business units
        User ceo = createTestUser("ceo@example.com", "Chief", "Executive", "EMP-CEO", "Management");
        User salesManager = createTestUser("sales.mgr@example.com", "Sales", "Manager", "EMP-SM", "Sales");
        User itManager = createTestUser("it.mgr@example.com", "IT", "Manager", "EMP-IT", "IT");

        assignUserToBusinessUnit(ceo, headquarters, "ADMIN", true);
        assignUserToBusinessUnit(salesManager, salesDept, "MANAGER", true);
        assignUserToBusinessUnit(itManager, itDept, "MANAGER", true);

        // Then - Verify relationships
        var hqAssignments = userBusinessUnitRepository.findByBusinessUnitId(headquarters.getId());
        assertThat(hqAssignments).hasSize(1);
        assertThat(hqAssignments.get(0).getUser().getEmail()).isEqualTo("ceo@example.com");

        var salesAssignments = userBusinessUnitRepository.findByBusinessUnitId(salesDept.getId());
        assertThat(salesAssignments).hasSize(1);

        var managerAssignments = userBusinessUnitRepository
                .findByBusinessUnitIdAndRole(salesDept.getId(), "MANAGER");
        assertThat(managerAssignments).hasSize(1);
        assertThat(managerAssignments.get(0).getUser().getFirstName()).isEqualTo("Sales");
    }

    @Test
    @Transactional
    void userLifecycle_ShouldHandleActivationAndDeactivation() {
        // Given - Create active user
        User user = createTestUser("active.user@example.com", "Active", "User", "EMP-ACT", "IT");
        assertThat(user.getIsActive()).isTrue();

        // When - Deactivate user
        user.deactivate();
        user = userRepository.save(user);

        // Then - User should be inactive
        assertThat(user.getIsActive()).isFalse();

        // When - Reactivate user
        user.reactivate();
        user = userRepository.save(user);

        // Then - User should be active again
        assertThat(user.getIsActive()).isTrue();
    }

    @Test
    @Transactional
    void multipleBusinessUnitAssignments_ShouldSupportSecondaryAssignments() {
        // Given
        User user = createTestUser("multi.bu@example.com", "Multi", "BU", "EMP-MULTI", "IT");
        BusinessUnit primaryBU = createBusinessUnit("PRIMARY", "Primary Unit", BusinessUnitType.DEPARTMENT, "US-EAST");
        BusinessUnit secondaryBU = createBusinessUnit("SECONDARY", "Secondary Unit", BusinessUnitType.BRANCH, "US-WEST");

        // When - Assign to multiple business units
        assignUserToBusinessUnit(user, primaryBU, "MANAGER", true);
        assignUserToBusinessUnit(user, secondaryBU, "AGENT", false);

        // Then - Verify assignments
        var allAssignments = userBusinessUnitRepository.findByUserId(user.getId());
        assertThat(allAssignments).hasSize(2);

        var primaryAssignment = userBusinessUnitRepository.findByUserIdAndIsPrimaryTrue(user.getId());
        assertThat(primaryAssignment).isPresent();
        assertThat(primaryAssignment.get().getBusinessUnit().getCode()).isEqualTo("PRIMARY");
        assertThat(primaryAssignment.get().getRole()).isEqualTo("MANAGER");

        long assignmentCount = userBusinessUnitRepository.countByBusinessUnitId(primaryBU.getId());
        assertThat(assignmentCount).isEqualTo(1);
    }

    // Helper methods
    private User createTestUser(String email, String firstName, String lastName, String empNumber, String dept) {
        User user = new User();
        user.setKeycloakId(UUID.randomUUID());
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmployeeNumber(empNumber);
        user.setDepartment(dept);
        user.setActive(true);
        user.setDeleted(false);
        return userRepository.save(user);
    }

    private BusinessUnit createBusinessUnit(String code, String name, BusinessUnitType type, String regionCode) {
        BusinessUnit bu = new BusinessUnit();
        bu.setCode(code);
        bu.setName(name);
        bu.setType(type);
        bu.setRegionCode(regionCode);
        bu.setActive(true);
        return businessUnitRepository.save(bu);
    }

    private UserBusinessUnit assignUserToBusinessUnit(User user, BusinessUnit bu, String role, boolean isPrimary) {
        UserBusinessUnit assignment = new UserBusinessUnit();
        assignment.setUser(user);
        assignment.setBusinessUnit(bu);
        assignment.setRole(role);
        assignment.setPrimary(isPrimary);
        return userBusinessUnitRepository.save(assignment);
    }
}
