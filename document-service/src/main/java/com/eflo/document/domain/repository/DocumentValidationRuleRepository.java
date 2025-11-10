package com.eflo.document.domain.repository;

import com.eflo.document.domain.entity.DocumentValidationRule;
import com.eflo.document.domain.enums.ValidationRuleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for DocumentValidationRule entity.
 * Provides CRUD operations and custom query methods for validation rule management.
 */
@Repository
public interface DocumentValidationRuleRepository extends JpaRepository<DocumentValidationRule, Long> {

    /**
     * Finds all validation rules for a specific document type.
     *
     * @param documentTypeId the document type identifier
     * @return list of validation rules for the document type
     */
    List<DocumentValidationRule> findByDocumentTypeId(Long documentTypeId);

    /**
     * Finds validation rules by rule type.
     *
     * @param ruleType the validation rule type
     * @return list of validation rules of the specified type
     */
    List<DocumentValidationRule> findByRuleType(ValidationRuleType ruleType);

    /**
     * Finds all active validation rules.
     *
     * @return list of active validation rules
     */
    List<DocumentValidationRule> findByIsActiveTrue();

    /**
     * Finds active validation rules for a document type ordered by execution order.
     *
     * @param documentTypeId the document type identifier
     * @return list of active validation rules ordered by execution order
     */
    List<DocumentValidationRule> findByDocumentTypeIdAndIsActiveTrueOrderByExecutionOrder(Long documentTypeId);

    /**
     * Finds validation rules by document type and rule type.
     *
     * @param documentTypeId the document type identifier
     * @param ruleType the validation rule type
     * @return list of validation rules matching the criteria
     */
    List<DocumentValidationRule> findByDocumentTypeIdAndRuleType(Long documentTypeId, ValidationRuleType ruleType);

    /**
     * Finds active validation rules by document type and rule type.
     *
     * @param documentTypeId the document type identifier
     * @param ruleType the validation rule type
     * @return list of active validation rules matching the criteria
     */
    @Query("SELECT dvr FROM DocumentValidationRule dvr WHERE dvr.documentType.id = :documentTypeId " +
           "AND dvr.ruleType = :ruleType AND dvr.isActive = true ORDER BY dvr.executionOrder")
    List<DocumentValidationRule> findActiveByDocumentTypeAndRuleType(
            @Param("documentTypeId") Long documentTypeId,
            @Param("ruleType") ValidationRuleType ruleType);

    /**
     * Finds validation rules by rule name.
     *
     * @param ruleName the rule name
     * @return Optional containing the validation rule if found
     */
    Optional<DocumentValidationRule> findByRuleName(String ruleName);

    // DISABLED: severityLevel field does not exist in DocumentValidationRule entity
    // /**
    //  * Finds validation rules with a specific severity level.
    //  *
    //  * @param documentTypeId the document type identifier
    //  * @param severityLevel the severity level (ERROR, WARNING, INFO)
    //  * @return list of validation rules with the specified severity
    //  */
    // @Query("SELECT dvr FROM DocumentValidationRule dvr WHERE dvr.documentType.id = :documentTypeId " +
    //        "AND dvr.severityLevel = :severityLevel AND dvr.isActive = true ORDER BY dvr.executionOrder")
    // List<DocumentValidationRule> findByDocumentTypeAndSeverity(
    //         @Param("documentTypeId") Long documentTypeId,
    //         @Param("severityLevel") String severityLevel);

    /**
     * Finds mandatory validation rules (blocking) for a document type.
     *
     * @param documentTypeId the document type identifier
     * @return list of mandatory/blocking validation rules
     */
    @Query("SELECT dvr FROM DocumentValidationRule dvr WHERE dvr.documentType.id = :documentTypeId " +
           "AND dvr.isBlocking = true AND dvr.isActive = true ORDER BY dvr.executionOrder")
    List<DocumentValidationRule> findBlockingRulesByDocumentType(@Param("documentTypeId") Long documentTypeId);

    // DISABLED: executeAsync field does not exist in DocumentValidationRule entity
    // /**
    //  * Finds validation rules that should execute asynchronously.
    //  *
    //  * @param documentTypeId the document type identifier
    //  * @return list of async validation rules
    //  */
    // @Query("SELECT dvr FROM DocumentValidationRule dvr WHERE dvr.documentType.id = :documentTypeId " +
    //        "AND dvr.executeAsync = true AND dvr.isActive = true ORDER BY dvr.executionOrder")
    // List<DocumentValidationRule> findAsyncRulesByDocumentType(@Param("documentTypeId") Long documentTypeId);

    /**
     * Counts validation rules for a document type.
     *
     * @param documentTypeId the document type identifier
     * @return count of validation rules
     */
    long countByDocumentTypeId(Long documentTypeId);

    /**
     * Counts active validation rules for a document type.
     *
     * @param documentTypeId the document type identifier
     * @return count of active validation rules
     */
    @Query("SELECT COUNT(dvr) FROM DocumentValidationRule dvr WHERE dvr.documentType.id = :documentTypeId " +
           "AND dvr.isActive = true")
    long countActiveByDocumentType(@Param("documentTypeId") Long documentTypeId);

    /**
     * Counts validation rules by type.
     *
     * @param ruleType the validation rule type
     * @return count of validation rules of the type
     */
    long countByRuleType(ValidationRuleType ruleType);

    /**
     * Gets validation rule statistics by type.
     *
     * @return list of rule type counts
     */
    @Query("SELECT dvr.ruleType, COUNT(dvr) FROM DocumentValidationRule dvr WHERE dvr.isActive = true " +
           "GROUP BY dvr.ruleType")
    List<Object[]> getValidationRuleStatsByType();

    /**
     * Finds validation rules with custom scripts.
     *
     * @return list of custom script validation rules
     */
    @Query("SELECT dvr FROM DocumentValidationRule dvr WHERE dvr.ruleType = 'CUSTOM_SCRIPT' " +
           "AND dvr.isActive = true ORDER BY dvr.documentType.id, dvr.executionOrder")
    List<DocumentValidationRule> findCustomScriptRules();

    /**
     * Finds the maximum execution order for a document type.
     *
     * @param documentTypeId the document type identifier
     * @return the maximum execution order value
     */
    @Query("SELECT COALESCE(MAX(dvr.executionOrder), 0) FROM DocumentValidationRule dvr " +
           "WHERE dvr.documentType.id = :documentTypeId")
    Integer findMaxExecutionOrderByDocumentType(@Param("documentTypeId") Long documentTypeId);

    /**
     * Checks if a rule name already exists.
     *
     * @param ruleName the rule name to check
     * @return true if exists, false otherwise
     */
    boolean existsByRuleName(String ruleName);

    /**
     * Finds validation rules by document type ordered by execution order.
     *
     * @param documentTypeId the document type identifier
     * @return list of validation rules ordered by execution order
     */
    List<DocumentValidationRule> findByDocumentTypeIdOrderByExecutionOrder(Long documentTypeId);

    // DISABLED: dependsOnRules field does not exist in DocumentValidationRule entity
    // /**
    //  * Finds all validation rules that depend on other rules.
    //  *
    //  * @param documentTypeId the document type identifier
    //  * @return list of dependent validation rules
    //  */
    // @Query("SELECT dvr FROM DocumentValidationRule dvr WHERE dvr.documentType.id = :documentTypeId " +
    //        "AND dvr.dependsOnRules IS NOT NULL AND SIZE(dvr.dependsOnRules) > 0 " +
    //        "AND dvr.isActive = true ORDER BY dvr.executionOrder")
    // List<DocumentValidationRule> findDependentRulesByDocumentType(@Param("documentTypeId") Long documentTypeId);

    /**
     * Finds validation rules that have been updated recently.
     *
     * @param documentTypeId the document type identifier
     * @return list of recently updated validation rules
     */
    @Query("SELECT dvr FROM DocumentValidationRule dvr WHERE dvr.documentType.id = :documentTypeId " +
           "AND dvr.updatedAt IS NOT NULL ORDER BY dvr.updatedAt DESC")
    List<DocumentValidationRule> findRecentlyUpdatedByDocumentType(@Param("documentTypeId") Long documentTypeId);
}
