package com.eflo.user.domain.repository;

import com.eflo.user.BaseRepositoryTest;
import com.eflo.user.domain.entity.BusinessUnit;
import com.eflo.user.domain.entity.User;
import com.eflo.user.domain.entity.UserBusinessUnit;
import com.eflo.user.domain.enums.BusinessUnitType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserBusinessUnitRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private UserBusinessUnitRepository userBusinessUnitRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusinessUnitRepository businessUnitRepository;

    private User testUser;
    private BusinessUnit testBusinessUnit;
    private UserBusinessUnit testAssignment;

    @BeforeEach
    void setUp() {
        userBusinessUnitRepository.deleteAll();
        userRepository.deleteAll();
        businessUnitRepository.deleteAll();

        // Create test user
        testUser = new User();
        testUser.setKeycloakId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setActive(true);
        testUser = userRepository.save(testUser);

        // Create test business unit
        testBusinessUnit = new BusinessUnit();
        testBusinessUnit.setCode("DEPT001");
        testBusinessUnit.setName("Test Department");
        testBusinessUnit.setType(BusinessUnitType.DEPARTMENT);
        testBusinessUnit.setActive(true);
        testBusinessUnit = businessUnitRepository.save(testBusinessUnit);

        // Create test assignment
        testAssignment = new UserBusinessUnit();
        testAssignment.setUser(testUser);
        testAssignment.setBusinessUnit(testBusinessUnit);
        testAssignment.setRole("MANAGER");
        testAssignment.setPrimary(true);
        testAssignment = userBusinessUnitRepository.save(testAssignment);
    }

    @Test
    void findByUserId_ShouldReturnUserAssignments() {
        // When
        List<UserBusinessUnit> assignments = userBusinessUnitRepository.findByUserId(testUser.getId());

        // Then
        assertThat(assignments).hasSize(1);
        assertThat(assignments.get(0).getRole()).isEqualTo("MANAGER");
    }

    @Test
    void findByBusinessUnitId_ShouldReturnBusinessUnitAssignments() {
        // When
        List<UserBusinessUnit> assignments = userBusinessUnitRepository.findByBusinessUnitId(testBusinessUnit.getId());

        // Then
        assertThat(assignments).hasSize(1);
        assertThat(assignments.get(0).getUser().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void findByUserIdAndBusinessUnitId_ShouldReturnSpecificAssignment() {
        // When
        Optional<UserBusinessUnit> assignment = userBusinessUnitRepository
            .findByUserIdAndBusinessUnitId(testUser.getId(), testBusinessUnit.getId());

        // Then
        assertThat(assignment).isPresent();
        assertThat(assignment.get().getRole()).isEqualTo("MANAGER");
    }

    @Test
    void findByUserIdAndIsPrimaryTrue_ShouldReturnPrimaryAssignment() {
        // Given
        BusinessUnit bu2 = new BusinessUnit();
        bu2.setCode("DEPT002");
        bu2.setName("Secondary Department");
        bu2.setType(BusinessUnitType.DEPARTMENT);
        bu2.setActive(true);
        bu2 = businessUnitRepository.save(bu2);

        UserBusinessUnit secondaryAssignment = new UserBusinessUnit();
        secondaryAssignment.setUser(testUser);
        secondaryAssignment.setBusinessUnit(bu2);
        secondaryAssignment.setRole("AGENT");
        secondaryAssignment.setPrimary(false);
        userBusinessUnitRepository.save(secondaryAssignment);

        // When
        Optional<UserBusinessUnit> primary = userBusinessUnitRepository
            .findByUserIdAndIsPrimaryTrue(testUser.getId());

        // Then
        assertThat(primary).isPresent();
        assertThat(primary.get().getBusinessUnit().getCode()).isEqualTo("DEPT001");
    }

    @Test
    void findByBusinessUnitIdAndRole_ShouldReturnAssignmentsByRole() {
        // Given
        User user2 = new User();
        user2.setKeycloakId(UUID.randomUUID());
        user2.setEmail("agent@example.com");
        user2.setFirstName("Agent");
        user2.setLastName("User");
        user2.setActive(true);
        user2 = userRepository.save(user2);

        UserBusinessUnit agentAssignment = new UserBusinessUnit();
        agentAssignment.setUser(user2);
        agentAssignment.setBusinessUnit(testBusinessUnit);
        agentAssignment.setRole("AGENT");
        agentAssignment.setPrimary(true);
        userBusinessUnitRepository.save(agentAssignment);

        // When
        List<UserBusinessUnit> managers = userBusinessUnitRepository
            .findByBusinessUnitIdAndRole(testBusinessUnit.getId(), "MANAGER");
        List<UserBusinessUnit> agents = userBusinessUnitRepository
            .findByBusinessUnitIdAndRole(testBusinessUnit.getId(), "AGENT");

        // Then
        assertThat(managers).hasSize(1);
        assertThat(agents).hasSize(1);
    }

    @Test
    void countByBusinessUnitId_ShouldReturnCorrectCount() {
        // Given
        User user2 = new User();
        user2.setKeycloakId(UUID.randomUUID());
        user2.setEmail("user2@example.com");
        user2.setFirstName("User");
        user2.setLastName("Two");
        user2.setActive(true);
        user2 = userRepository.save(user2);

        UserBusinessUnit assignment2 = new UserBusinessUnit();
        assignment2.setUser(user2);
        assignment2.setBusinessUnit(testBusinessUnit);
        assignment2.setRole("AGENT");
        assignment2.setPrimary(true);
        userBusinessUnitRepository.save(assignment2);

        // When
        long count = userBusinessUnitRepository.countByBusinessUnitId(testBusinessUnit.getId());

        // Then
        assertThat(count).isEqualTo(2);
    }
}
