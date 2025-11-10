package com.eflo.user.service;

import com.eflo.user.domain.entity.User;
import com.eflo.user.domain.entity.UserActivityLog;
import com.eflo.user.domain.enums.ActivityType;
import com.eflo.user.domain.repository.UserActivityLogRepository;
import com.eflo.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserActivityLogRepository userActivityLogRepository;

    @Mock
    private KeycloakUserManagementService keycloakUserManagementService;

    @Mock
    private UserActivityService userActivityService;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UUID keycloakId;

    @BeforeEach
    void setUp() {
        keycloakId = UUID.randomUUID();
        testUser = new User();
        testUser.setId(1L);
        testUser.setKeycloakId(keycloakId);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEmployeeNumber("EMP001");
        testUser.setActive(true);
        testUser.setDeleted(false);
    }

    @Test
    void createUser_ShouldCreateSuccessfully_WhenValidData() {
        // Given
        String temporaryPassword = "TempPass123!";
        String performedBy = "admin";
        User newUser = new User();
        newUser.setEmail("new@example.com");
        newUser.setFirstName("New");
        newUser.setLastName("User");
        newUser.setEmployeeNumber("EMP002");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmployeeNumber(anyString())).thenReturn(Optional.empty());
        when(keycloakUserManagementService.createUserInKeycloak(any(User.class), anyString()))
            .thenReturn(keycloakId);
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // When
        User created = userService.createUser(newUser, temporaryPassword, performedBy);

        // Then
        assertThat(created).isNotNull();
        verify(userRepository).findByEmail("new@example.com");
        verify(userRepository).findByEmployeeNumber("EMP002");
        verify(keycloakUserManagementService).createUserInKeycloak(newUser, temporaryPassword);
        verify(userRepository).save(newUser);
        verify(userActivityService).logActivity(any(User.class), eq(ActivityType.USER_CREATED), anyString(), eq(performedBy), any());
    }

    @Test
    void createUser_ShouldThrowException_WhenEmailAlreadyExists() {
        // Given
        User newUser = new User();
        newUser.setEmail("existing@example.com");
        newUser.setEmployeeNumber("EMP002");

        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(testUser));

        // When/Then
        assertThatThrownBy(() -> userService.createUser(newUser, "password", "admin"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("already exists");

        verify(keycloakUserManagementService, never()).createUserInKeycloak(any(), any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_ShouldThrowException_WhenEmployeeNumberAlreadyExists() {
        // Given
        User newUser = new User();
        newUser.setEmail("new@example.com");
        newUser.setEmployeeNumber("EMP001");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmployeeNumber("EMP001")).thenReturn(Optional.of(testUser));

        // When/Then
        assertThatThrownBy(() -> userService.createUser(newUser, "password", "admin"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("already exists");
    }

    @Test
    void updateUser_ShouldUpdateSuccessfully_WhenUserExists() {
        // Given
        Long userId = 1L;
        String performedBy = "admin";
        User updates = new User();
        updates.setFirstName("Updated");
        updates.setLastName("Name");
        updates.setEmail("updated@example.com");
        updates.setPhoneNumber("+9876543210");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User updated = userService.updateUser(userId, updates, performedBy);

        // Then
        assertThat(updated).isNotNull();
        assertThat(testUser.getFirstName()).isEqualTo("Updated");
        assertThat(testUser.getLastName()).isEqualTo("Name");
        verify(keycloakUserManagementService).updateUserInKeycloak(testUser);
        verify(userActivityService).logActivity(any(User.class), eq(ActivityType.PROFILE_UPDATE), anyString(), eq(performedBy), any());
    }

    @Test
    void updateUser_ShouldThrowException_WhenUserNotFound() {
        // Given
        Long userId = 999L;
        User updates = new User();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.updateUser(userId, updates, "admin"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("not found");

        verify(userRepository, never()).save(any());
    }

    @Test
    void findById_ShouldReturnUser_WhenExists() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> found = userService.findById(1L);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void findByKeycloakId_ShouldReturnUser_WhenExists() {
        // Given
        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> found = userService.findByKeycloakId(keycloakId);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getKeycloakId()).isEqualTo(keycloakId);
    }

    @Test
    void findByEmail_ShouldReturnUser_WhenExists() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        Optional<User> found = userService.findByEmail("test@example.com");

        // Then
        assertThat(found).isPresent();
    }

    @Test
    void findAllActive_ShouldReturnActiveUsers() {
        // Given
        List<User> activeUsers = Arrays.asList(testUser);
        when(userRepository.findAllActiveAndNotDeleted()).thenReturn(activeUsers);

        // When
        List<User> result = userService.findAllActive();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void searchUsers_ShouldReturnMatchingUsers() {
        // Given
        List<User> users = Arrays.asList(testUser);
        when(userRepository.searchUsers("test")).thenReturn(users);

        // When
        List<User> result = userService.searchUsers("test");

        // Then
        assertThat(result).hasSize(1);
        verify(userRepository).searchUsers("test");
    }

    @Test
    void recordLogin_ShouldUpdateLoginInformation() {
        // Given
        Long userId = 1L;
        String ipAddress = "192.168.1.1";
        String userAgent = "Mozilla/5.0";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.recordLogin(userId, ipAddress, userAgent);

        // Then
        verify(userRepository).save(testUser);
        verify(userActivityLogRepository).save(any(UserActivityLog.class));

        ArgumentCaptor<UserActivityLog> logCaptor = ArgumentCaptor.forClass(UserActivityLog.class);
        verify(userActivityLogRepository).save(logCaptor.capture());
        UserActivityLog log = logCaptor.getValue();
        assertThat(log.getActivityType()).isEqualTo(ActivityType.LOGIN);
        assertThat(log.getIpAddress()).isEqualTo(ipAddress);
        assertThat(log.getUserAgent()).isEqualTo(userAgent);
    }

    @Test
    void deactivateUser_ShouldDeactivateSuccessfully() {
        // Given
        Long userId = 1L;
        String performedBy = "admin";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.deactivateUser(userId, performedBy);

        // Then
        assertThat(testUser.getIsActive()).isFalse();
        verify(keycloakUserManagementService).disableUserInKeycloak(keycloakId);
        verify(userRepository).save(testUser);
        verify(userActivityService).logActivity(any(User.class), eq(ActivityType.STATUS_CHANGE), anyString(), eq(performedBy), any());
    }

    @Test
    void reactivateUser_ShouldReactivateSuccessfully() {
        // Given
        Long userId = 1L;
        String performedBy = "admin";
        testUser.setActive(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.reactivateUser(userId, performedBy);

        // Then
        assertThat(testUser.getIsActive()).isTrue();
        verify(keycloakUserManagementService).enableUserInKeycloak(keycloakId);
        verify(userRepository).save(testUser);
    }

    @Test
    void deleteUser_ShouldSoftDeleteUser() {
        // Given
        Long userId = 1L;
        String performedBy = "admin";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.deleteUser(userId, performedBy);

        // Then
        assertThat(testUser.getIsDeleted()).isTrue();
        assertThat(testUser.getIsActive()).isFalse();
        verify(keycloakUserManagementService).disableUserInKeycloak(keycloakId);
        verify(userActivityService).logActivity(any(User.class), eq(ActivityType.USER_DELETED), anyString(), eq(performedBy), any());
    }

    @Test
    void resetPassword_ShouldResetSuccessfully() {
        // Given
        Long userId = 1L;
        String newPassword = "NewPass123!";
        String performedBy = "admin";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        userService.resetPassword(userId, newPassword, true, performedBy);

        // Then
        verify(keycloakUserManagementService).setUserPassword(keycloakId, newPassword, true);
        verify(userActivityService).logActivity(any(User.class), eq(ActivityType.PASSWORD_CHANGE), anyString(), eq(performedBy), any());
    }

    @Test
    void countActive_ShouldReturnCorrectCount() {
        // Given
        List<User> activeUsers = Arrays.asList(testUser, new User(), new User());
        when(userRepository.findAllActiveAndNotDeleted()).thenReturn(activeUsers);

        // When
        long count = userService.countActive();

        // Then
        assertThat(count).isEqualTo(3);
    }
}
