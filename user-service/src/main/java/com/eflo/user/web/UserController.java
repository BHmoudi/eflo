package com.eflo.user.web;

import com.eflo.user.domain.dto.*;
import com.eflo.user.domain.entity.User;
import com.eflo.user.mapper.UserMapper;
import com.eflo.user.service.UserService;
import com.eflo.user.util.JwtAuthenticationHelper;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final JwtAuthenticationHelper jwtHelper;

    public UserController(UserService userService, UserMapper userMapper, JwtAuthenticationHelper jwtHelper) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.jwtHelper = jwtHelper;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_LOCAL')")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("Creating new user: {}", request.getEmail());

        String performedBy;
        try {
            performedBy = jwtHelper.getCurrentUsername();
        } catch (Exception e) {
            performedBy = "system"; // Fallback when no authentication
        }

        User user = userMapper.toEntity(request);
        User createdUser = userService.createUser(user, request.getTemporaryPassword(), performedBy);

        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toDTO(createdUser));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_LOCAL', 'SALES_MANAGER', 'SALESPERSON', 'VIEWER')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        log.info("Fetching user by ID: {}", id);

        return userService.findById(id)
                .map(userMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_LOCAL', 'SALES_MANAGER', 'SALESPERSON', 'VIEWER')")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        log.info("Fetching user by email: {}", email);

        return userService.findByEmail(email)
                .map(userMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/employee/{employeeNumber}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_LOCAL', 'SALES_MANAGER', 'SALESPERSON', 'VIEWER')")
    public ResponseEntity<UserDTO> getUserByEmployeeNumber(@PathVariable String employeeNumber) {
        log.info("Fetching user by employee number: {}", employeeNumber);

        return userService.findByEmployeeNumber(employeeNumber)
                .map(userMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_LOCAL', 'SALES_MANAGER', 'VIEWER')")
    public ResponseEntity<PagedUserResponse> getAllUsers(Pageable pageable) {
        log.info("Fetching all users with pagination: {}", pageable);

        Page<User> usersPage = userService.findAll(pageable);

        PagedUserResponse response = PagedUserResponse.builder()
                .users(userMapper.toDTOList(usersPage.getContent()))
                .currentPage(usersPage.getNumber())
                .totalPages(usersPage.getTotalPages())
                .totalElements(usersPage.getTotalElements())
                .pageSize(usersPage.getSize())
                .hasNext(usersPage.hasNext())
                .hasPrevious(usersPage.hasPrevious())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_LOCAL', 'SALES_MANAGER', 'VIEWER')")
    public ResponseEntity<List<UserDTO>> getActiveUsers() {
        log.info("Fetching all active users");

        List<User> users = userService.findAllActive();
        return ResponseEntity.ok(userMapper.toDTOList(users));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_LOCAL', 'SALES_MANAGER', 'VIEWER')")
    public ResponseEntity<List<UserDTO>> searchUsers(@RequestParam String q) {
        log.info("Searching users with term: {}", q);

        List<User> users = userService.searchUsers(q);
        return ResponseEntity.ok(userMapper.toDTOList(users));
    }

    @GetMapping("/department/{department}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_LOCAL', 'SALES_MANAGER', 'VIEWER')")
    public ResponseEntity<List<UserDTO>> getUsersByDepartment(@PathVariable String department) {
        log.info("Fetching users by department: {}", department);

        List<User> users = userService.findByDepartment(department);
        return ResponseEntity.ok(userMapper.toDTOList(users));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_LOCAL') or #id == principal.claims['user_id']")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        log.info("Updating user: {}", id);
        String performedBy = jwtHelper.getCurrentUsername();

        User updatedUser = userMapper.toEntity(new CreateUserRequest());
        userMapper.updateEntity(request, updatedUser);
        User savedUser = userService.updateUser(id, updatedUser, performedBy);

        return ResponseEntity.ok(userMapper.toDTO(savedUser));
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_LOCAL')")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        log.info("Deactivating user: {}", id);
        String performedBy = jwtHelper.getCurrentUsername();

        userService.deactivateUser(id, performedBy);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reactivate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_LOCAL')")
    public ResponseEntity<Void> reactivateUser(@PathVariable Long id) {
        log.info("Reactivating user: {}", id);
        String performedBy = jwtHelper.getCurrentUsername();

        userService.reactivateUser(id, performedBy);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Deleting user: {}", id);
        String performedBy = jwtHelper.getCurrentUsername();

        userService.deleteUser(id, performedBy);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_LOCAL')")
    public ResponseEntity<Void> resetPassword(
            @PathVariable Long id,
            @RequestParam String newPassword,
            @RequestParam(defaultValue = "true") boolean temporary) {
        log.info("Resetting password for user: {}", id);
        String performedBy = jwtHelper.getCurrentUsername();

        userService.resetPassword(id, newPassword, temporary, performedBy);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/login")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> recordLogin(
            @PathVariable Long id,
            @RequestParam(required = false) String ipAddress,
            @RequestParam(required = false) String userAgent) {
        log.info("Recording login for user: {}", id);

        userService.recordLogin(id, ipAddress, userAgent);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_LOCAL')")
    public ResponseEntity<Long> getUserCount() {
        long count = userService.count();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/count/active")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN_LOCAL')")
    public ResponseEntity<Long> getActiveUserCount() {
        long count = userService.countActive();
        return ResponseEntity.ok(count);
    }

    /**
     * Get current authenticated user's context
     * Returns: user_ipn, rrf, roles, business units, etc.
     */
    @GetMapping("/me/context")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserContextDTO> getCurrentUserContext() {
        try {
            // Get username from JWT (tries preferred_username, email, then sub)
            String username = jwtHelper.getCurrentUsername();

            log.info("Fetching context for current user: {}", username);

            if (username == null) {
                log.warn("Could not extract username from JWT token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Try finding by keycloak username first, then by email
            Optional<User> userOpt = userService.findByKeycloakUsername(username);
            if (userOpt.isEmpty()) {
                userOpt = userService.findByEmail(username);
            }

            return userOpt
                    .map(this::buildUserContext)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error getting current user context: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Build user context with all custom fields
     */
    private UserContextDTO buildUserContext(User user) {
        // Get roles
        List<String> roles = userService.getUserRoles(user.getId());

        // Get business units with RRF
        List<UserContextDTO.BusinessUnitInfo> businessUnits = user.getBusinessUnits().stream()
                .filter(ubu -> ubu.getIsActive() != null && ubu.getIsActive())
                .filter(ubu -> ubu.getBusinessUnit() != null)
                .map(ubu -> UserContextDTO.BusinessUnitInfo.builder()
                        .id(ubu.getBusinessUnit().getId())
                        .code(ubu.getBusinessUnit().getCode())
                        .name(ubu.getBusinessUnit().getName())
                        .rrf(ubu.getBusinessUnit().getRrfCode())
                        .isPrimary(ubu.getIsPrimary())
                        .build())
                .toList();

        // Get primary BU info
        UserContextDTO.BusinessUnitInfo primaryBU = businessUnits.stream()
                .filter(bu -> bu.getIsPrimary() != null && bu.getIsPrimary())
                .findFirst()
                .orElse(null);

        return UserContextDTO.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .username(user.getKeycloakUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .employeeNumber(user.getEmployeeNumber())
                .userIpn(user.getUserIpn())
                .department(user.getDepartment())
                .jobTitle(user.getJobTitle())
                .primaryBusinessUnitId(primaryBU != null ? primaryBU.getId() : null)
                .primaryBusinessUnitCode(primaryBU != null ? primaryBU.getCode() : null)
                .primaryBusinessUnitName(primaryBU != null ? primaryBU.getName() : null)
                .rrf(primaryBU != null ? primaryBU.getRrf() : null)
                .roles(roles)
                .businessUnits(businessUnits)
                .build();
    }
}
