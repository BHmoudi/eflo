package com.eflo.document.domain.repository;

import com.eflo.document.domain.entity.DocumentType;
import com.eflo.document.domain.enums.DocumentCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for DocumentType entity.
 * Provides CRUD operations and custom query methods for document type management.
 */
@Repository
public interface DocumentTypeRepository extends JpaRepository<DocumentType, Long> {

    /**
     * Finds a document type by its unique type code.
     *
     * @param typeCode the type code to search for
     * @return Optional containing the DocumentType if found
     */
    Optional<DocumentType> findByTypeCode(String typeCode);

    /**
     * Finds all document types belonging to a specific category.
     *
     * @param category the document category
     * @return list of document types in the category
     */
    List<DocumentType> findByCategory(DocumentCategory category);

    /**
     * Finds all mandatory document types.
     *
     * @return list of mandatory document types
     */
    List<DocumentType> findByIsMandatoryTrue();

    /**
     * Finds all document types for a specific business unit.
     *
     * @param businessUnitId the business unit identifier
     * @return list of document types for the business unit
     */
    List<DocumentType> findByBusinessUnitId(Long businessUnitId);

    /**
     * Finds all active document types.
     *
     * @return list of active document types
     */
    List<DocumentType> findByIsActiveTrue();

    /**
     * Finds all document types for a business unit ordered by display order.
     *
     * @param businessUnitId the business unit identifier
     * @return list of document types ordered by display order
     */
    List<DocumentType> findByBusinessUnitIdOrderByDisplayOrder(Long businessUnitId);

    /**
     * Finds active document types for a specific business unit and category.
     *
     * @param businessUnitId the business unit identifier
     * @param category the document category
     * @return list of active document types matching the criteria
     */
    @Query("SELECT dt FROM DocumentType dt WHERE dt.businessUnitId = :businessUnitId " +
           "AND dt.category = :category AND dt.isActive = true ORDER BY dt.displayOrder")
    List<DocumentType> findActiveByBusinessUnitAndCategory(
            @Param("businessUnitId") Long businessUnitId,
            @Param("category") DocumentCategory category);

    /**
     * Finds all mandatory document types for a specific business unit.
     *
     * @param businessUnitId the business unit identifier
     * @return list of mandatory document types for the business unit
     */
    @Query("SELECT dt FROM DocumentType dt WHERE dt.businessUnitId = :businessUnitId " +
           "AND dt.isMandatory = true AND dt.isActive = true ORDER BY dt.displayOrder")
    List<DocumentType> findMandatoryByBusinessUnit(@Param("businessUnitId") Long businessUnitId);

    /**
     * Finds document types that require validation.
     *
     * @return list of document types requiring validation
     */
    @Query("SELECT dt FROM DocumentType dt WHERE dt.requiresValidation = true AND dt.isActive = true")
    List<DocumentType> findRequiringValidation();

    /**
     * Finds document types with expiration settings.
     *
     * @return list of document types that have expiration enabled
     */
    @Query("SELECT dt FROM DocumentType dt WHERE dt.hasExpiration = true AND dt.isActive = true")
    List<DocumentType> findWithExpiration();

    /**
     * Checks if a type code already exists.
     *
     * @param typeCode the type code to check
     * @return true if exists, false otherwise
     */
    boolean existsByTypeCode(String typeCode);

    /**
     * Counts document types by category.
     *
     * @param category the document category
     * @return count of document types in the category
     */
    long countByCategory(DocumentCategory category);

    /**
     * Counts active document types for a business unit.
     *
     * @param businessUnitId the business unit identifier
     * @return count of active document types
     */
    @Query("SELECT COUNT(dt) FROM DocumentType dt WHERE dt.businessUnitId = :businessUnitId AND dt.isActive = true")
    long countActiveByBusinessUnit(@Param("businessUnitId") Long businessUnitId);
}
