package com.eflo.user.domain.repository;

import com.eflo.user.BaseRepositoryTest;
import com.eflo.user.domain.entity.BusinessUnit;
import com.eflo.user.domain.entity.User;
import com.eflo.user.domain.entity.UserRole;
import com.eflo.user.domain.enums.BusinessUnitType;
import com.eflo.user.domain.enums.RoleSource;
import com.eflo.user.domain.enums.UserRoleEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserRoleRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusinessUnitRepository businessUnitRepository;

    private User testUser;
    private BusinessUnit testBusinessUnit;
    private UserRole testRole;

    @BeforeEach
    void setUp() {
        userRoleRepository.deleteAll();
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

        // Create test role
        testRole = new UserRole();
        testRole.setUser(testUser);
        testRole.setRoleName(UserRoleEnum.MANAGER);
        testRole.setSource(RoleSource.MANUAL);
        testRole.setBusinessUnit(testBusinessUnit);
        testRole.setActive(true);
        testRole.setGrantedBy("system");
        testRole = userRoleRepository.save(testRole);
    }

    @Test
    void findByUserId_ShouldReturnUserRoles() {
        // When
        List<UserRole> roles = userRoleRepository.findByUserId(testUser.getId());

        // Then
        assertThat(roles).hasSize(1);
        assertThat(roles.get(0).getRoleName()).isEqualTo(UserRoleEnum.MANAGER);
    }

    @Test
    void findByUserIdAndIsActiveTrue_ShouldReturnOnlyActiveRoles() {
        // Given
        UserRole inactiveRole = new UserRole();
        inactiveRole.setUser(testUser);
        inactiveRole.setRoleName(UserRoleEnum.AGENT);
        inactiveRole.setSource(RoleSource.KEYCLOAK);
        inactiveRole.setActive(false);
        userRoleRepository.save(inactiveRole);

        // When
        List<UserRole> activeRoles = userRoleRepository.findByUserIdAndIsActiveTrue(testUser.getId());

        // Then
        assertThat(activeRoles).hasSize(1);
        assertThat(activeRoles.get(0).getRoleName()).isEqualTo(UserRoleEnum.MANAGER);
    }

    @Test
    void findByRoleName_ShouldReturnRolesByName() {
        // Given
        User user2 = new User();
        user2.setKeycloakId(UUID.randomUUID());
        user2.setEmail("user2@example.com");
        user2.setFirstName("User");
        user2.setLastName("Two");
        user2.setActive(true);
        user2 = userRepository.save(user2);

        UserRole role2 = new UserRole();
        role2.setUser(user2);
        role2.setRoleName(UserRoleEnum.MANAGER);
        role2.setSource(RoleSource.KEYCLOAK);
        role2.setActive(true);
        userRoleRepository.save(role2);

        // When
        List<UserRole> managers = userRoleRepository.findByRoleName(UserRoleEnum.MANAGER);

        // Then
        assertThat(managers).hasSize(2);
    }

    @Test
    void findByUserIdAndRoleName_ShouldReturnSpecificRole() {
        // When
        Optional<UserRole> found = userRoleRepository
            .findByUserIdAndRoleName(testUser.getId(), UserRoleEnum.MANAGER);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getSource()).isEqualTo(RoleSource.MANUAL);
    }

    @Test
    void findActiveByUserIdAndRoleName_ShouldReturnActiveRole() {
        // When
        Optional<UserRole> found = userRoleRepository
            .findActiveByUserIdAndRoleName(testUser.getId(), UserRoleEnum.MANAGER);

        // Then
        assertThat(found).isPresent();
    }

    @Test
    void findByUserIdAndSource_ShouldReturnRolesBySource() {
        // Given
        UserRole keycloakRole = new UserRole();
        keycloakRole.setUser(testUser);
        keycloakRole.setRoleName(UserRoleEnum.AGENT);
        keycloakRole.setSource(RoleSource.KEYCLOAK);
        keycloakRole.setActive(true);
        userRoleRepository.save(keycloakRole);

        // When
        List<UserRole> manualRoles = userRoleRepository
            .findByUserIdAndSource(testUser.getId(), RoleSource.MANUAL);
        List<UserRole> keycloakRoles = userRoleRepository
            .findByUserIdAndSource(testUser.getId(), RoleSource.KEYCLOAK);

        // Then
        assertThat(manualRoles).hasSize(1);
        assertThat(keycloakRoles).hasSize(1);
    }

    @Test
    void findActiveByUserIdAndSource_ShouldReturnActiveRolesBySource() {
        // Given
        UserRole inactiveRole = new UserRole();
        inactiveRole.setUser(testUser);
        inactiveRole.setRoleName(UserRoleEnum.AGENT);
        inactiveRole.setSource(RoleSource.MANUAL);
        inactiveRole.setActive(false);
        userRoleRepository.save(inactiveRole);

        // When
        List<UserRole> activeManual = userRoleRepository
            .findActiveByUserIdAndSource(testUser.getId(), RoleSource.MANUAL);

        // Then
        assertThat(activeManual).hasSize(1);
        assertThat(activeManual.get(0).getRoleName()).isEqualTo(UserRoleEnum.MANAGER);
    }

    @Test
    void findByBusinessUnitId_ShouldReturnRolesForBusinessUnit() {
        // When
        List<UserRole> roles = userRoleRepository.findByBusinessUnitId(testBusinessUnit.getId());

        // Then
        assertThat(roles).hasSize(1);
    }

    @Test
    void findActiveByBusinessUnitId_ShouldReturnActiveRolesForBusinessUnit() {
        // When
        List<UserRole> activeRoles = userRoleRepository.findActiveByBusinessUnitId(testBusinessUnit.getId());

        // Then
        assertThat(activeRoles).hasSize(1);
    }

    @Test
    void findActiveByUserIdAndBusinessUnitId_ShouldReturnActiveRolesForUserInBusinessUnit() {
        // When
        List<UserRole> roles = userRoleRepository
            .findActiveByUserIdAndBusinessUnitId(testUser.getId(), testBusinessUnit.getId());

        // Then
        assertThat(roles).hasSize(1);
        assertThat(roles.get(0).getRoleName()).isEqualTo(UserRoleEnum.MANAGER);
    }

    @Test
    void countActiveByUserId_ShouldReturnCorrectCount() {
        // Given
        UserRole role2 = new UserRole();
        role2.setUser(testUser);
        role2.setRoleName(UserRoleEnum.AGENT);
        role2.setSource(RoleSource.KEYCLOAK);
        role2.setActive(true);
        userRoleRepository.save(role2);

        UserRole inactiveRole = new UserRole();
        inactiveRole.setUser(testUser);
        inactiveRole.setRoleName(UserRoleEnum.VIEWER);
        inactiveRole.setSource(RoleSource.MANUAL);
        inactiveRole.setActive(false);
        userRoleRepository.save(inactiveRole);

        // When
        long count = userRoleRepository.countActiveByUserId(testUser.getId());

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void findActiveRoleNamesByUserId_ShouldReturnDistinctRoleNames() {
        // Given
        UserRole role2 = new UserRole();
        role2.setUser(testUser);
        role2.setRoleName(UserRoleEnum.AGENT);
        role2.setSource(RoleSource.KEYCLOAK);
        role2.setActive(true);
        userRoleRepository.save(role2);

        // Duplicate role name but different source
        UserRole role3 = new UserRole();
        role3.setUser(testUser);
        role3.setRoleName(UserRoleEnum.MANAGER);
        role3.setSource(RoleSource.KEYCLOAK);
        role3.setActive(true);
        userRoleRepository.save(role3);

        // When
        List<UserRoleEnum> roleNames = userRoleRepository.findActiveRoleNamesByUserId(testUser.getId());

        // Then
        assertThat(roleNames).hasSize(2);
        assertThat(roleNames).containsExactlyInAnyOrder(UserRoleEnum.MANAGER, UserRoleEnum.AGENT);
    }
}
