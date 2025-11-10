package com.eflo.user.domain.repository;

import com.eflo.user.BaseRepositoryTest;
import com.eflo.user.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = new User();
        testUser.setKeycloakId(UUID.randomUUID());
        testUser.setEmail("test.user@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEmployeeNumber("EMP001");
        testUser.setDepartment("IT");
        testUser.setPhoneNumber("+1234567890");
        testUser.setActive(true);
        testUser.setDeleted(false);

        testUser = userRepository.save(testUser);
    }

    @Test
    void findByKeycloakId_ShouldReturnUser_WhenExists() {
        // When
        Optional<User> found = userRepository.findByKeycloakId(testUser.getKeycloakId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test.user@example.com");
    }

    @Test
    void findByKeycloakId_ShouldReturnEmpty_WhenNotExists() {
        // When
        Optional<User> found = userRepository.findByKeycloakId(UUID.randomUUID());

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByEmail_ShouldReturnUser_WhenExists() {
        // When
        Optional<User> found = userRepository.findByEmail("test.user@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("Test");
    }

    @Test
    void findByEmployeeNumber_ShouldReturnUser_WhenExists() {
        // When
        Optional<User> found = userRepository.findByEmployeeNumber("EMP001");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getLastName()).isEqualTo("User");
    }

    @Test
    void findByIsActiveTrue_ShouldReturnOnlyActiveUsers() {
        // Given
        User inactiveUser = new User();
        inactiveUser.setKeycloakId(UUID.randomUUID());
        inactiveUser.setEmail("inactive@example.com");
        inactiveUser.setFirstName("Inactive");
        inactiveUser.setLastName("User");
        inactiveUser.setActive(false);
        userRepository.save(inactiveUser);

        // When
        List<User> activeUsers = userRepository.findByIsActiveTrue();

        // Then
        assertThat(activeUsers).hasSize(1);
        assertThat(activeUsers.get(0).getEmail()).isEqualTo("test.user@example.com");
    }

    @Test
    void findByDepartment_ShouldReturnUsersInDepartment() {
        // Given
        User user2 = new User();
        user2.setKeycloakId(UUID.randomUUID());
        user2.setEmail("user2@example.com");
        user2.setFirstName("User");
        user2.setLastName("Two");
        user2.setDepartment("IT");
        user2.setActive(true);
        userRepository.save(user2);

        User user3 = new User();
        user3.setKeycloakId(UUID.randomUUID());
        user3.setEmail("user3@example.com");
        user3.setFirstName("User");
        user3.setLastName("Three");
        user3.setDepartment("Sales");
        user3.setActive(true);
        userRepository.save(user3);

        // When
        List<User> itUsers = userRepository.findByDepartment("IT");

        // Then
        assertThat(itUsers).hasSize(2);
        assertThat(itUsers).allMatch(u -> "IT".equals(u.getDepartment()));
    }

    @Test
    void searchUsers_ShouldFindUsersByVariousCriteria() {
        // When - Search by first name
        List<User> byFirstName = userRepository.searchUsers("test");

        // Then
        assertThat(byFirstName).hasSize(1);
        assertThat(byFirstName.get(0).getFirstName()).isEqualTo("Test");

        // When - Search by email
        List<User> byEmail = userRepository.searchUsers("test.user");

        // Then
        assertThat(byEmail).hasSize(1);

        // When - Search by employee number
        List<User> byEmpNumber = userRepository.searchUsers("EMP001");

        // Then
        assertThat(byEmpNumber).hasSize(1);
    }

    @Test
    void findAllActiveAndNotDeleted_ShouldReturnOnlyValidUsers() {
        // Given
        User deletedUser = new User();
        deletedUser.setKeycloakId(UUID.randomUUID());
        deletedUser.setEmail("deleted@example.com");
        deletedUser.setFirstName("Deleted");
        deletedUser.setLastName("User");
        deletedUser.setActive(true);
        deletedUser.setDeleted(true);
        userRepository.save(deletedUser);

        // When
        List<User> validUsers = userRepository.findAllActiveAndNotDeleted();

        // Then
        assertThat(validUsers).hasSize(1);
        assertThat(validUsers.get(0).getEmail()).isEqualTo("test.user@example.com");
    }

    @Test
    void findActiveByDepartment_ShouldReturnOnlyActiveUsersInDepartment() {
        // Given
        User inactiveItUser = new User();
        inactiveItUser.setKeycloakId(UUID.randomUUID());
        inactiveItUser.setEmail("inactive.it@example.com");
        inactiveItUser.setFirstName("Inactive");
        inactiveItUser.setLastName("IT");
        inactiveItUser.setDepartment("IT");
        inactiveItUser.setActive(false);
        userRepository.save(inactiveItUser);

        // When
        List<User> activeItUsers = userRepository.findActiveByDepartment("IT");

        // Then
        assertThat(activeItUsers).hasSize(1);
        assertThat(activeItUsers.get(0).getEmail()).isEqualTo("test.user@example.com");
    }
}
