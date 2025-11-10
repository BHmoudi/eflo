package com.eflo.user.web;

import com.eflo.user.domain.dto.AssignUserRequest;
import com.eflo.user.domain.dto.UserBusinessUnitDTO;
import com.eflo.user.domain.entity.UserBusinessUnit;
import com.eflo.user.mapper.UserBusinessUnitMapper;
import com.eflo.user.service.UserBusinessUnitService;
import com.eflo.user.util.JwtAuthenticationHelper;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/user-business-units")
public class UserBusinessUnitController {

    private final UserBusinessUnitService userBusinessUnitService;
    private final UserBusinessUnitMapper userBusinessUnitMapper;
    private final JwtAuthenticationHelper jwtHelper;

    public UserBusinessUnitController(UserBusinessUnitService userBusinessUnitService,
                                      UserBusinessUnitMapper userBusinessUnitMapper,
                                      JwtAuthenticationHelper jwtHelper) {
        this.userBusinessUnitService = userBusinessUnitService;
        this.userBusinessUnitMapper = userBusinessUnitMapper;
        this.jwtHelper = jwtHelper;
    }

    @PostMapping
    
    public ResponseEntity<UserBusinessUnitDTO> assignUserToBusinessUnit(@Valid @RequestBody AssignUserRequest request) {
        log.info("Assigning user {} to business unit {}", request.getUserId(), request.getBusinessUnitId());
        String performedBy = jwtHelper.getCurrentUsername();

        UserBusinessUnit assignment = userBusinessUnitService.assignUserToBusinessUnit(
                request.getUserId(),
                request.getBusinessUnitId(),
                request.getIsPrimary(),
                performedBy
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(userBusinessUnitMapper.toDTO(assignment));
    }

    @DeleteMapping("/{userId}/{businessUnitId}")
    
    public ResponseEntity<Void> removeUserFromBusinessUnit(
            @PathVariable Long userId,
            @PathVariable Long businessUnitId) {
        log.info("Removing user {} from business unit {}", userId, businessUnitId);
        String performedBy = jwtHelper.getCurrentUsername();

        userBusinessUnitService.removeUserFromBusinessUnit(userId, businessUnitId, performedBy);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/set-primary/{businessUnitId}")
    
    public ResponseEntity<Void> setPrimaryBusinessUnit(
            @PathVariable Long userId,
            @PathVariable Long businessUnitId) {
        log.info("Setting primary business unit {} for user {}", businessUnitId, userId);
        String performedBy = jwtHelper.getCurrentUsername();

        userBusinessUnitService.setPrimaryBusinessUnit(userId, businessUnitId, performedBy);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}")
    
    public ResponseEntity<List<UserBusinessUnitDTO>> getUserBusinessUnits(@PathVariable Long userId) {
        log.info("Fetching business units for user: {}", userId);

        List<UserBusinessUnit> assignments = userBusinessUnitService.getUserBusinessUnits(userId);
        return ResponseEntity.ok(userBusinessUnitMapper.toDTOList(assignments));
    }

    @GetMapping("/user/{userId}/primary")
    
    public ResponseEntity<UserBusinessUnitDTO> getPrimaryBusinessUnit(@PathVariable Long userId) {
        log.info("Fetching primary business unit for user: {}", userId);

        return userBusinessUnitService.getPrimaryBusinessUnit(userId)
                .map(userBusinessUnitMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/business-unit/{businessUnitId}")
    
    public ResponseEntity<List<UserBusinessUnitDTO>> getBusinessUnitUsers(@PathVariable Long businessUnitId) {
        log.info("Fetching users for business unit: {}", businessUnitId);

        List<UserBusinessUnit> assignments = userBusinessUnitService.getBusinessUnitUsers(businessUnitId);
        return ResponseEntity.ok(userBusinessUnitMapper.toDTOList(assignments));
    }

    @GetMapping("/{userId}/is-assigned/{businessUnitId}")
    
    public ResponseEntity<Boolean> isUserAssignedToBusinessUnit(
            @PathVariable Long userId,
            @PathVariable Long businessUnitId) {
        log.info("Checking if user {} is assigned to business unit {}", userId, businessUnitId);

        boolean isAssigned = userBusinessUnitService.isUserAssignedToBusinessUnit(userId, businessUnitId);
        return ResponseEntity.ok(isAssigned);
    }
}
