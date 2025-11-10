package com.eflo.user.service;

import com.eflo.user.domain.entity.BusinessUnit;
import com.eflo.user.domain.entity.User;
import com.eflo.user.domain.enums.BusinessUnitType;
import com.eflo.user.domain.repository.BusinessUnitRepository;
import com.eflo.user.domain.repository.UserRepository;
import com.eflo.user.exception.DuplicateBusinessUnitException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing Business Unit entities
 *
 * Provides CRUD operations and business unit-specific queries.
 */
@Slf4j
@Service
public class BusinessUnitService {

    private final BusinessUnitRepository businessUnitRepository;
    private final UserRepository userRepository;

    public BusinessUnitService(BusinessUnitRepository businessUnitRepository,
                               UserRepository userRepository) {
        this.businessUnitRepository = businessUnitRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create a new business unit
     *
     * @param businessUnit Business unit to create
     * @return Created business unit
     */
    @Transactional
    public BusinessUnit createBusinessUnit(BusinessUnit businessUnit) {
        log.info("Creating new business unit: {}", businessUnit.getName());

        // Validate unique constraints
        if (businessUnitRepository.findByCode(businessUnit.getCode()).isPresent()) {
            throw new DuplicateBusinessUnitException(businessUnit.getCode(),
                "Business unit with code already exists: " + businessUnit.getCode());
        }

        BusinessUnit savedBU = businessUnitRepository.save(businessUnit);
        log.info("Successfully created business unit: {}", savedBU.getName());
        return savedBU;
    }

    /**
     * Update an existing business unit
     *
     * @param businessUnitId Business unit ID
     * @param updatedBusinessUnit Updated business unit data
     * @return Updated business unit
     */
    @Transactional
    public BusinessUnit updateBusinessUnit(Long businessUnitId, BusinessUnit updatedBusinessUnit) {
        log.info("Updating business unit: {}", businessUnitId);

        BusinessUnit existingBU = findById(businessUnitId)
                .orElseThrow(() -> new IllegalArgumentException("Business unit not found: " + businessUnitId));

        // Update fields
        existingBU.setName(updatedBusinessUnit.getName());
        existingBU.setLegalName(updatedBusinessUnit.getLegalName());
        existingBU.setType(updatedBusinessUnit.getType());
        existingBU.setRegionCode(updatedBusinessUnit.getRegionCode());
        existingBU.setRegionName(updatedBusinessUnit.getRegionName());
        existingBU.setRrfCode(updatedBusinessUnit.getRrfCode());
        existingBU.setPhoneNumber(updatedBusinessUnit.getPhoneNumber());
        existingBU.setFaxNumber(updatedBusinessUnit.getFaxNumber());
        existingBU.setEmail(updatedBusinessUnit.getEmail());
        existingBU.setWebsite(updatedBusinessUnit.getWebsite());
        existingBU.setAddressLine1(updatedBusinessUnit.getAddressLine1());
        existingBU.setAddressLine2(updatedBusinessUnit.getAddressLine2());
        existingBU.setPostalCode(updatedBusinessUnit.getPostalCode());
        existingBU.setCity(updatedBusinessUnit.getCity());
        existingBU.setCountry(updatedBusinessUnit.getCountry());
        existingBU.setLatitude(updatedBusinessUnit.getLatitude());
        existingBU.setLongitude(updatedBusinessUnit.getLongitude());
        existingBU.setOpeningHours(updatedBusinessUnit.getOpeningHours());
        existingBU.setSiret(updatedBusinessUnit.getSiret());
        existingBU.setVatNumber(updatedBusinessUnit.getVatNumber());
        existingBU.setBrands(updatedBusinessUnit.getBrands());
        existingBU.setOpeningDate(updatedBusinessUnit.getOpeningDate());
        existingBU.setSettings(updatedBusinessUnit.getSettings());

        BusinessUnit savedBU = businessUnitRepository.save(existingBU);
        log.info("Successfully updated business unit: {}", savedBU.getName());
        return savedBU;
    }

    /**
     * Find business unit by ID
     *
     * @param businessUnitId Business unit ID
     * @return Optional business unit
     */
    @Transactional(readOnly = true)
    public Optional<BusinessUnit> findById(Long businessUnitId) {
        return businessUnitRepository.findById(businessUnitId);
    }

    /**
     * Find business unit by code
     *
     * @param code Business unit code
     * @return Optional business unit
     */
    @Transactional(readOnly = true)
    public Optional<BusinessUnit> findByCode(String code) {
        return businessUnitRepository.findByCode(code);
    }

    /**
     * Get all business units
     *
     * @param pageable Pagination parameters
     * @return Page of business units
     */
    @Transactional(readOnly = true)
    public Page<BusinessUnit> findAll(Pageable pageable) {
        return businessUnitRepository.findAll(pageable);
    }

    /**
     * Get all active business units
     *
     * @return List of active business units
     */
    @Transactional(readOnly = true)
    public List<BusinessUnit> findAllActive() {
        return businessUnitRepository.findByIsActiveTrue();
    }

    /**
     * Get business units by type
     *
     * @param type Business unit type as string
     * @return List of business units of the specified type
     */
    @Transactional(readOnly = true)
    public List<BusinessUnit> getByType(String type) {
        BusinessUnitType businessUnitType = BusinessUnitType.valueOf(type.toUpperCase());
        return businessUnitRepository.findByType(businessUnitType);
    }

    /**
     * Find business units by region
     *
     * @param regionCode Region code
     * @return List of business units in region
     */
    @Transactional(readOnly = true)
    public List<BusinessUnit> findByRegion(String regionCode) {
        return businessUnitRepository.findByRegionCodeAndIsActiveTrue(regionCode);
    }

    /**
     * Find business units by region name
     *
     * @param regionName Region name
     * @return List of business units in region
     */
    @Transactional(readOnly = true)
    public List<BusinessUnit> findByRegionName(String regionName) {
        return businessUnitRepository.findByRegionName(regionName);
    }

    /**
     * Search business units
     *
     * @param searchTerm Search term
     * @return List of matching business units
     */
    @Transactional(readOnly = true)
    public List<BusinessUnit> searchBusinessUnits(String searchTerm) {
        return businessUnitRepository.searchBusinessUnits(searchTerm);
    }

    /**
     * Assign manager to business unit
     *
     * @param businessUnitId Business unit ID
     * @param managerId Manager user ID
     * @return Updated business unit
     */
    @Transactional
    public BusinessUnit assignManager(Long businessUnitId, Long managerId) {
        log.info("Assigning manager {} to business unit {}", managerId, businessUnitId);

        BusinessUnit businessUnit = findById(businessUnitId)
                .orElseThrow(() -> new IllegalArgumentException("Business unit not found: " + businessUnitId));

        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("Manager not found: " + managerId));

        businessUnit.setManager(manager);
        BusinessUnit savedBU = businessUnitRepository.save(businessUnit);

        log.info("Successfully assigned manager {} to business unit {}", manager.getFullName(), savedBU.getName());
        return savedBU;
    }

    /**
     * Remove manager from business unit
     *
     * @param businessUnitId Business unit ID
     * @return Updated business unit
     */
    @Transactional
    public BusinessUnit removeManager(Long businessUnitId) {
        log.info("Removing manager from business unit {}", businessUnitId);

        BusinessUnit businessUnit = findById(businessUnitId)
                .orElseThrow(() -> new IllegalArgumentException("Business unit not found: " + businessUnitId));

        businessUnit.setManager(null);
        BusinessUnit savedBU = businessUnitRepository.save(businessUnit);

        log.info("Successfully removed manager from business unit {}", savedBU.getName());
        return savedBU;
    }

    /**
     * Deactivate business unit
     *
     * @param businessUnitId Business unit ID
     */
    @Transactional
    public void deactivateBusinessUnit(Long businessUnitId) {
        log.info("Deactivating business unit: {}", businessUnitId);

        BusinessUnit businessUnit = findById(businessUnitId)
                .orElseThrow(() -> new IllegalArgumentException("Business unit not found: " + businessUnitId));

        businessUnit.deactivate();
        businessUnitRepository.save(businessUnit);

        log.info("Successfully deactivated business unit: {}", businessUnit.getName());
    }

    /**
     * Reactivate business unit
     *
     * @param businessUnitId Business unit ID
     */
    @Transactional
    public void reactivateBusinessUnit(Long businessUnitId) {
        log.info("Reactivating business unit: {}", businessUnitId);

        BusinessUnit businessUnit = findById(businessUnitId)
                .orElseThrow(() -> new IllegalArgumentException("Business unit not found: " + businessUnitId));

        businessUnit.reactivate();
        businessUnitRepository.save(businessUnit);

        log.info("Successfully reactivated business unit: {}", businessUnit.getName());
    }

    /**
     * Delete business unit
     *
     * @param businessUnitId Business unit ID
     */
    @Transactional
    public void deleteBusinessUnit(Long businessUnitId) {
        log.info("Deleting business unit: {}", businessUnitId);

        BusinessUnit businessUnit = findById(businessUnitId)
                .orElseThrow(() -> new IllegalArgumentException("Business unit not found: " + businessUnitId));

        // Soft delete by deactivating
        businessUnit.deactivate();
        businessUnitRepository.save(businessUnit);

        log.info("Successfully deleted business unit: {}", businessUnit.getName());
    }

    /**
     * Count total business units
     *
     * @return Total business unit count
     */
    @Transactional(readOnly = true)
    public long count() {
        return businessUnitRepository.count();
    }

    /**
     * Count active business units
     *
     * @return Active business unit count
     */
    @Transactional(readOnly = true)
    public long countActive() {
        return businessUnitRepository.findByIsActiveTrue().size();
    }
}
