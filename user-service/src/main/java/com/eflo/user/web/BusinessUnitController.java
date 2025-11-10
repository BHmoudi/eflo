package com.eflo.user.web;

import com.eflo.user.domain.dto.BusinessUnitDTO;
import com.eflo.user.domain.dto.CreateBusinessUnitRequest;
import com.eflo.user.domain.entity.BusinessUnit;
import com.eflo.user.mapper.BusinessUnitMapper;
import com.eflo.user.service.BusinessUnitService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/business-units")
public class BusinessUnitController {

    private final BusinessUnitService businessUnitService;
    private final BusinessUnitMapper businessUnitMapper;

    public BusinessUnitController(BusinessUnitService businessUnitService, BusinessUnitMapper businessUnitMapper) {
        this.businessUnitService = businessUnitService;
        this.businessUnitMapper = businessUnitMapper;
    }

    @PostMapping
    
    public ResponseEntity<BusinessUnitDTO> createBusinessUnit(@Valid @RequestBody CreateBusinessUnitRequest request) {
        log.info("Creating new business unit: {}", request.getName());

        BusinessUnit businessUnit = businessUnitMapper.toEntity(request);
        BusinessUnit created = businessUnitService.createBusinessUnit(businessUnit);

        return ResponseEntity.status(HttpStatus.CREATED).body(businessUnitMapper.toDTO(created));
    }

    @GetMapping("/{id}")
    
    public ResponseEntity<BusinessUnitDTO> getBusinessUnitById(@PathVariable Long id) {
        log.info("Fetching business unit by ID: {}", id);

        return businessUnitService.findById(id)
                .map(businessUnitMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{code}")
    
    public ResponseEntity<BusinessUnitDTO> getBusinessUnitByCode(@PathVariable String code) {
        log.info("Fetching business unit by code: {}", code);

        return businessUnitService.findByCode(code)
                .map(businessUnitMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    
    public ResponseEntity<Page<BusinessUnitDTO>> getAllBusinessUnits(Pageable pageable) {
        log.info("Fetching all business units with pagination: {}", pageable);

        Page<BusinessUnit> page = businessUnitService.findAll(pageable);
        return ResponseEntity.ok(page.map(businessUnitMapper::toDTO));
    }

    @GetMapping("/active")
    
    public ResponseEntity<List<BusinessUnitDTO>> getActiveBusinessUnits() {
        log.info("Fetching all active business units");

        List<BusinessUnit> businessUnits = businessUnitService.findAllActive();
        return ResponseEntity.ok(businessUnitMapper.toDTOList(businessUnits));
    }

    @GetMapping("/type/{type}")
    
    public ResponseEntity<List<BusinessUnitDTO>> getBusinessUnitsByType(@PathVariable String type) {
        log.info("Fetching business units by type: {}", type);

        List<BusinessUnit> businessUnits = businessUnitService.getByType(type);
        return ResponseEntity.ok(businessUnitMapper.toDTOList(businessUnits));
    }

    @GetMapping("/region/{regionCode}")
    
    public ResponseEntity<List<BusinessUnitDTO>> getBusinessUnitsByRegion(@PathVariable String regionCode) {
        log.info("Fetching business units by region: {}", regionCode);

        List<BusinessUnit> businessUnits = businessUnitService.findByRegion(regionCode);
        return ResponseEntity.ok(businessUnitMapper.toDTOList(businessUnits));
    }

    @GetMapping("/search")
    
    public ResponseEntity<List<BusinessUnitDTO>> searchBusinessUnits(@RequestParam String q) {
        log.info("Searching business units with term: {}", q);

        List<BusinessUnit> businessUnits = businessUnitService.searchBusinessUnits(q);
        return ResponseEntity.ok(businessUnitMapper.toDTOList(businessUnits));
    }

    @PutMapping("/{id}")
    
    public ResponseEntity<BusinessUnitDTO> updateBusinessUnit(
            @PathVariable Long id,
            @Valid @RequestBody CreateBusinessUnitRequest request) {
        log.info("Updating business unit: {}", id);

        BusinessUnit businessUnit = businessUnitMapper.toEntity(request);
        BusinessUnit updated = businessUnitService.updateBusinessUnit(id, businessUnit);

        return ResponseEntity.ok(businessUnitMapper.toDTO(updated));
    }

    @PostMapping("/{id}/assign-manager/{managerId}")
    
    public ResponseEntity<BusinessUnitDTO> assignManager(@PathVariable Long id, @PathVariable Long managerId) {
        log.info("Assigning manager {} to business unit {}", managerId, id);

        BusinessUnit updated = businessUnitService.assignManager(id, managerId);
        return ResponseEntity.ok(businessUnitMapper.toDTO(updated));
    }

    @PostMapping("/{id}/remove-manager")
    
    public ResponseEntity<BusinessUnitDTO> removeManager(@PathVariable Long id) {
        log.info("Removing manager from business unit {}", id);

        BusinessUnit updated = businessUnitService.removeManager(id);
        return ResponseEntity.ok(businessUnitMapper.toDTO(updated));
    }

    @PostMapping("/{id}/deactivate")
    
    public ResponseEntity<Void> deactivateBusinessUnit(@PathVariable Long id) {
        log.info("Deactivating business unit: {}", id);

        businessUnitService.deactivateBusinessUnit(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reactivate")
    
    public ResponseEntity<Void> reactivateBusinessUnit(@PathVariable Long id) {
        log.info("Reactivating business unit: {}", id);

        businessUnitService.reactivateBusinessUnit(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    
    public ResponseEntity<Void> deleteBusinessUnit(@PathVariable Long id) {
        log.info("Deleting business unit: {}", id);

        businessUnitService.deleteBusinessUnit(id);
        return ResponseEntity.noContent().build();
    }
}
