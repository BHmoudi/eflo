package com.eflo.user.web;

import com.eflo.user.domain.dto.CreateHierarchyRequest;
import com.eflo.user.domain.dto.HierarchyDTO;
import com.eflo.user.domain.dto.UserDTO;
import com.eflo.user.domain.entity.Hierarchy;
import com.eflo.user.domain.entity.User;
import com.eflo.user.mapper.HierarchyMapper;
import com.eflo.user.mapper.UserMapper;
import com.eflo.user.service.HierarchyService;
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
@RequestMapping("/api/v1/hierarchies")
public class HierarchyController {

    private final HierarchyService hierarchyService;
    private final HierarchyMapper hierarchyMapper;
    private final UserMapper userMapper;
    private final JwtAuthenticationHelper jwtHelper;

    public HierarchyController(HierarchyService hierarchyService,
                               HierarchyMapper hierarchyMapper,
                               UserMapper userMapper,
                               JwtAuthenticationHelper jwtHelper) {
        this.hierarchyService = hierarchyService;
        this.hierarchyMapper = hierarchyMapper;
        this.userMapper = userMapper;
        this.jwtHelper = jwtHelper;
    }

    @PostMapping
    
    public ResponseEntity<HierarchyDTO> createHierarchy(@Valid @RequestBody CreateHierarchyRequest request) {
        log.info("Creating hierarchy: employee {} -> manager {}", request.getEmployeeId(), request.getManagerId());
        String performedBy = jwtHelper.getCurrentUsername();

        Hierarchy hierarchy = hierarchyService.createHierarchy(
                request.getEmployeeId(),
                request.getManagerId(),
                request.getBusinessUnitId(),
                request.getLevel(),
                performedBy
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(hierarchyMapper.toDTO(hierarchy));
    }

    @PutMapping("/{id}")
    
    public ResponseEntity<HierarchyDTO> updateHierarchy(
            @PathVariable Long id,
            @Valid @RequestBody CreateHierarchyRequest request) {
        log.info("Updating hierarchy: {}", id);
        String performedBy = jwtHelper.getCurrentUsername();

        Hierarchy hierarchy = hierarchyService.updateHierarchy(
                id,
                request.getManagerId(),
                request.getBusinessUnitId(),
                request.getLevel(),
                performedBy
        );

        return ResponseEntity.ok(hierarchyMapper.toDTO(hierarchy));
    }

    @DeleteMapping("/{id}")
    
    public ResponseEntity<Void> deactivateHierarchy(@PathVariable Long id) {
        log.info("Deactivating hierarchy: {}", id);
        String performedBy = jwtHelper.getCurrentUsername();

        hierarchyService.deactivateHierarchy(id, performedBy);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/employee/{employeeId}")
    
    public ResponseEntity<HierarchyDTO> getEmployeeHierarchy(@PathVariable Long employeeId) {
        log.info("Fetching hierarchy for employee: {}", employeeId);

        return hierarchyService.getActiveHierarchyForEmployee(employeeId)
                .map(hierarchyMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/manager/{managerId}/direct-reports")
    
    public ResponseEntity<List<HierarchyDTO>> getDirectReports(@PathVariable Long managerId) {
        log.info("Fetching direct reports for manager: {}", managerId);

        List<Hierarchy> hierarchies = hierarchyService.getDirectReports(managerId);
        return ResponseEntity.ok(hierarchyMapper.toDTOList(hierarchies));
    }

    @GetMapping("/manager/{managerId}/business-unit/{businessUnitId}")
    
    public ResponseEntity<List<HierarchyDTO>> getDirectReportsInBusinessUnit(
            @PathVariable Long managerId,
            @PathVariable Long businessUnitId) {
        log.info("Fetching direct reports for manager {} in business unit {}", managerId, businessUnitId);

        List<Hierarchy> hierarchies = hierarchyService.getDirectReportsInBusinessUnit(managerId, businessUnitId);
        return ResponseEntity.ok(hierarchyMapper.toDTOList(hierarchies));
    }

    @GetMapping("/employee/{employeeId}/management-chain")
    
    public ResponseEntity<List<UserDTO>> getManagementChain(@PathVariable Long employeeId) {
        log.info("Fetching management chain for employee: {}", employeeId);

        List<User> chain = hierarchyService.getManagementChain(employeeId);
        return ResponseEntity.ok(userMapper.toDTOList(chain));
    }

    @GetMapping("/manager/{managerId}/all-subordinates")
    
    public ResponseEntity<List<UserDTO>> getAllSubordinates(@PathVariable Long managerId) {
        log.info("Fetching all subordinates for manager: {}", managerId);

        List<User> subordinates = hierarchyService.getAllSubordinates(managerId);
        return ResponseEntity.ok(userMapper.toDTOList(subordinates));
    }

    @GetMapping("/business-unit/{businessUnitId}")
    
    public ResponseEntity<List<HierarchyDTO>> getBusinessUnitHierarchies(@PathVariable Long businessUnitId) {
        log.info("Fetching hierarchies for business unit: {}", businessUnitId);

        List<Hierarchy> hierarchies = hierarchyService.getBusinessUnitHierarchies(businessUnitId);
        return ResponseEntity.ok(hierarchyMapper.toDTOList(hierarchies));
    }
}
