package com.eflo.user.web;

import com.eflo.user.domain.dto.UserContextDTO;
import com.eflo.user.domain.entity.User;
import com.eflo.user.service.UserService;
import com.eflo.user.util.JwtAuthenticationHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/**
 * User Context Controller
 * Provides current authenticated user's context with custom fields
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/context")
public class UserContextController {

    private final UserService userService;
    private final JwtAuthenticationHelper jwtHelper;

    public UserContextController(UserService userService, JwtAuthenticationHelper jwtHelper) {
        this.userService = userService;
        this.jwtHelper = jwtHelper;
    }

    /**
     * Get current user's context with user_ipn, rrf, roles
     * GET /api/v1/context/me
     */
    @GetMapping("/me")
    public ResponseEntity<UserContextDTO> getCurrentUserContext() {
        try {
            // Get username from JWT
            String username = jwtHelper.getCurrentUsername();
            log.info("Getting context for user: {}", username);

            if (username == null) {
                log.warn("No username in JWT");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Find user by keycloak username or email
            Optional<User> userOpt = userService.findByKeycloakUsername(username);
            if (userOpt.isEmpty()) {
                userOpt = userService.findByEmail(username);
            }

            if (userOpt.isEmpty()) {
                log.warn("User not found: {}", username);
                return ResponseEntity.notFound().build();
            }

            User user = userOpt.get();
            UserContextDTO context = buildUserContext(user);

            return ResponseEntity.ok(context);

        } catch (Exception e) {
            log.error("Error getting user context", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Build user context with all fields
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

        // Get primary BU
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
