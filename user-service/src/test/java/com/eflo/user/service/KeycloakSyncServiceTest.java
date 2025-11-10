package com.eflo.user.service;

import com.eflo.user.domain.entity.User;
import com.eflo.user.domain.entity.UserRole;
import com.eflo.user.domain.enums.ActivityType;
import com.eflo.user.domain.enums.RoleSource;
import com.eflo.user.domain.enums.UserRoleEnum;
import com.eflo.user.domain.repository.UserRepository;
import com.eflo.user.domain.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeycloakSyncServiceTest {

    @Mock
    private Keycloak keycloak;

    @Mock
    private RealmResource realmResource;

    @Mock
    private UsersResource usersResource;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private UserActivityService userActivityService;

    @InjectMocks
    private KeycloakSyncService keycloakSyncService;

    private User testUser;
    private UUID keycloakUserId;

    @BeforeEach
    void setUp() {
        keycloakUserId = UUID.randomUUID();
        testUser = new User();
        testUser.setId(1L);
        testUser.setKeycloakId(keycloakUserId);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setActive(true);

        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
    }

    @Test
    void syncUsersFromKeycloak_ShouldSyncNewUsers() {
        // Given
        UserRepresentation kcUser = new UserRepresentation();
        kcUser.setId(keycloakUserId.toString());
        kcUser.setEmail("newuser@example.com");
        kcUser.setFirstName("New");
        kcUser.setLastName("User");
        kcUser.setUsername("newuser@example.com");
        kcUser.setEnabled(true);

        when(usersResource.list()).thenReturn(Collections.singletonList(kcUser));
        when(userRepository.findByKeycloakId(any(UUID.class))).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        keycloakSyncService.syncUsersFromKeycloak();

        // Then
        verify(usersResource).list();
        verify(userRepository).save(any(User.class));
        verify(userActivityService).logActivity(any(User.class), eq(ActivityType.SYNC), anyString(), anyString(), any());
    }

    @Test
    void syncUsersFromKeycloak_ShouldUpdateExistingUsers() {
        // Given
        UserRepresentation kcUser = new UserRepresentation();
        kcUser.setId(keycloakUserId.toString());
        kcUser.setEmail("test@example.com");
        kcUser.setFirstName("Updated");
        kcUser.setLastName("Name");
        kcUser.setUsername("test@example.com");
        kcUser.setEnabled(true);

        when(usersResource.list()).thenReturn(Collections.singletonList(kcUser));
        when(userRepository.findByKeycloakId(keycloakUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        keycloakSyncService.syncUsersFromKeycloak();

        // Then
        verify(userRepository).findByKeycloakId(keycloakUserId);
        verify(userRepository).save(testUser);
        assertThat(testUser.getFirstName()).isEqualTo("Updated");
    }

    @Test
    void syncUsersFromKeycloak_ShouldHandleDisabledUsers() {
        // Given
        UserRepresentation kcUser = new UserRepresentation();
        kcUser.setId(keycloakUserId.toString());
        kcUser.setEmail("test@example.com");
        kcUser.setFirstName("Test");
        kcUser.setLastName("User");
        kcUser.setUsername("test@example.com");
        kcUser.setEnabled(false);

        when(usersResource.list()).thenReturn(Collections.singletonList(kcUser));
        when(userRepository.findByKeycloakId(keycloakUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        keycloakSyncService.syncUsersFromKeycloak();

        // Then
        verify(userRepository).save(testUser);
        assertThat(testUser.getIsActive()).isFalse();
    }

    @Test
    void syncRolesFromKeycloak_ShouldSyncUserRoles() {
        // Given
        RoleRepresentation managerRole = new RoleRepresentation();
        managerRole.setName("MANAGER");

        RoleRepresentation agentRole = new RoleRepresentation();
        agentRole.setName("AGENT");

        List<RoleRepresentation> roles = Arrays.asList(managerRole, agentRole);

        when(userRepository.findAll()).thenReturn(Collections.singletonList(testUser));
        when(userRoleRepository.findActiveByUserIdAndSource(anyLong(), eq(RoleSource.KEYCLOAK)))
            .thenReturn(Collections.emptyList());

        // When
        keycloakSyncService.syncRolesFromKeycloak();

        // Then
        verify(userRepository).findAll();
        verify(userRoleRepository).findActiveByUserIdAndSource(testUser.getId(), RoleSource.KEYCLOAK);
    }

    @Test
    void deactivateOldKeycloakRoles_ShouldDeactivateRemovedRoles() {
        // Given
        UserRole existingRole = new UserRole();
        existingRole.setId(1L);
        existingRole.setUser(testUser);
        existingRole.setRoleName(UserRoleEnum.MANAGER);
        existingRole.setSource(RoleSource.KEYCLOAK);
        existingRole.setActive(true);

        List<UserRole> existingRoles = Collections.singletonList(existingRole);
        Set<UserRoleEnum> currentRoles = Collections.emptySet();

        when(userRoleRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        keycloakSyncService.deactivateOldKeycloakRoles(testUser, existingRoles, currentRoles);

        // Then
        verify(userRoleRepository).saveAll(anyList());
        assertThat(existingRole.getIsActive()).isFalse();
    }

    @Test
    void addNewKeycloakRoles_ShouldAddNewRoles() {
        // Given
        Set<UserRoleEnum> newRoles = new HashSet<>(Arrays.asList(UserRoleEnum.MANAGER, UserRoleEnum.AGENT));
        List<UserRole> existingRoles = Collections.emptyList();

        when(userRoleRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        keycloakSyncService.addNewKeycloakRoles(testUser, existingRoles, newRoles, null);

        // Then
        verify(userRoleRepository).saveAll(anyList());
    }

    @Test
    void syncSingleUserFromKeycloak_ShouldSyncSpecificUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserRepresentation kcUser = new UserRepresentation();
        kcUser.setId(keycloakUserId.toString());
        kcUser.setEmail("test@example.com");
        kcUser.setFirstName("Synced");
        kcUser.setLastName("User");
        kcUser.setEnabled(true);

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        keycloakSyncService.syncSingleUserFromKeycloak(1L);

        // Then
        verify(userRepository).findById(1L);
    }
}
