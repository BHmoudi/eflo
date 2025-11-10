package com.eflo.document.domain.repository;

import com.eflo.document.domain.entity.DocumentMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for DocumentMetadata entity.
 * Provides CRUD operations and custom query methods for document metadata management.
 */
@Repository
public interface DocumentMetadataRepository extends JpaRepository<DocumentMetadata, Long> {

    /**
     * Finds all metadata entries for a specific document.
     *
     * @param documentId the document identifier
     * @return list of metadata entries for the document
     */
    List<DocumentMetadata> findByDocumentId(Long documentId);

    /**
     * Finds all metadata entries with a specific key.
     *
     * @param metadataKey the metadata key
     * @return list of metadata entries with the key
     */
    List<DocumentMetadata> findByMetadataKey(String metadataKey);

    /**
     * Finds a specific metadata entry by document ID and key.
     *
     * @param documentId the document identifier
     * @param metadataKey the metadata key
     * @return Optional containing the metadata entry if found
     */
    Optional<DocumentMetadata> findByDocumentIdAndMetadataKey(Long documentId, String metadataKey);

    /**
     * Finds metadata entries by key and value.
     *
     * @param metadataKey the metadata key
     * @param metadataValue the metadata value
     * @return list of metadata entries matching key and value
     */
    List<DocumentMetadata> findByMetadataKeyAndMetadataValue(String metadataKey, String metadataValue);

    /**
     * Finds metadata entries by value only.
     *
     * @param metadataValue the metadata value
     * @return list of metadata entries with the value
     */
    List<DocumentMetadata> findByMetadataValue(String metadataValue);

    /**
     * Finds all metadata entries with a specific key prefix.
     *
     * @param prefix the key prefix to search for
     * @return list of metadata entries with keys starting with the prefix
     */
    @Query("SELECT dm FROM DocumentMetadata dm WHERE dm.metadataKey LIKE CONCAT(:prefix, '%')")
    List<DocumentMetadata> findByMetadataKeyStartingWith(@Param("prefix") String prefix);

    /**
     * Finds metadata entries for a document with a specific key prefix.
     *
     * @param documentId the document identifier
     * @param prefix the key prefix
     * @return list of metadata entries matching the criteria
     */
    @Query("SELECT dm FROM DocumentMetadata dm WHERE dm.document.id = :documentId " +
           "AND dm.metadataKey LIKE CONCAT(:prefix, '%')")
    List<DocumentMetadata> findByDocumentIdAndMetadataKeyStartingWith(
            @Param("documentId") Long documentId,
            @Param("prefix") String prefix);

    /**
     * Finds documents with a specific metadata key-value pair.
     *
     * @param metadataKey the metadata key
     * @param metadataValue the metadata value
     * @return list of document IDs with the metadata
     */
    @Query("SELECT DISTINCT dm.document.id FROM DocumentMetadata dm " +
           "WHERE dm.metadataKey = :metadataKey AND dm.metadataValue = :metadataValue")
    List<Long> findDocumentIdsByMetadata(
            @Param("metadataKey") String metadataKey,
            @Param("metadataValue") String metadataValue);

    /**
     * Searches for metadata entries with value containing a search term.
     *
     * @param searchTerm the term to search for
     * @return list of metadata entries with matching values
     */
    @Query("SELECT dm FROM DocumentMetadata dm WHERE LOWER(dm.metadataValue) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<DocumentMetadata> searchByMetadataValue(@Param("searchTerm") String searchTerm);

    /**
     * Finds metadata entries for a document containing a search term in value.
     *
     * @param documentId the document identifier
     * @param searchTerm the term to search for
     * @return list of metadata entries matching the criteria
     */
    @Query("SELECT dm FROM DocumentMetadata dm WHERE dm.document.id = :documentId " +
           "AND LOWER(dm.metadataValue) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<DocumentMetadata> searchByDocumentAndValue(
            @Param("documentId") Long documentId,
            @Param("searchTerm") String searchTerm);

    /**
     * Counts metadata entries for a document.
     *
     * @param documentId the document identifier
     * @return count of metadata entries
     */
    long countByDocumentId(Long documentId);

    /**
     * Counts metadata entries with a specific key.
     *
     * @param metadataKey the metadata key
     * @return count of metadata entries with the key
     */
    long countByMetadataKey(String metadataKey);

    /**
     * Checks if a metadata key exists for a document.
     *
     * @param documentId the document identifier
     * @param metadataKey the metadata key
     * @return true if exists, false otherwise
     */
    boolean existsByDocumentIdAndMetadataKey(Long documentId, String metadataKey);

    /**
     * Gets all unique metadata keys across all documents.
     *
     * @return list of unique metadata keys
     */
    @Query("SELECT DISTINCT dm.metadataKey FROM DocumentMetadata dm ORDER BY dm.metadataKey")
    List<String> findAllUniqueKeys();

    /**
     * Gets all unique values for a specific metadata key.
     *
     * @param metadataKey the metadata key
     * @return list of unique values for the key
     */
    @Query("SELECT DISTINCT dm.metadataValue FROM DocumentMetadata dm WHERE dm.metadataKey = :metadataKey " +
           "ORDER BY dm.metadataValue")
    List<String> findUniqueValuesByKey(@Param("metadataKey") String metadataKey);

    /**
     * Gets metadata statistics by key.
     *
     * @return list of key usage counts
     */
    @Query("SELECT dm.metadataKey, COUNT(dm) FROM DocumentMetadata dm GROUP BY dm.metadataKey " +
           "ORDER BY COUNT(dm) DESC")
    List<Object[]> getMetadataStatsByKey();

    /**
     * Finds required metadata keys from document type schema.
     *
     * @param documentId the document identifier
     * @return list of required metadata keys for the document type
     */
    @Query("SELECT DISTINCT dm.metadataKey FROM DocumentMetadata dm WHERE dm.document.id = :documentId " +
           "AND dm.isRequired = true")
    List<String> findRequiredKeysByDocument(@Param("documentId") Long documentId);

    /**
     * Finds metadata entries that are marked as searchable.
     *
     * @param documentId the document identifier
     * @return list of searchable metadata entries
     */
    @Query("SELECT dm FROM DocumentMetadata dm WHERE dm.document.id = :documentId " +
           "AND dm.isSearchable = true")
    List<DocumentMetadata> findSearchableByDocument(@Param("documentId") Long documentId);

    /**
     * Deletes all metadata for a document.
     *
     * @param documentId the document identifier
     */
    void deleteByDocumentId(Long documentId);

    /**
     * Deletes a specific metadata entry.
     *
     * @param documentId the document identifier
     * @param metadataKey the metadata key
     */
    void deleteByDocumentIdAndMetadataKey(Long documentId, String metadataKey);

    /**
     * Finds metadata entries grouped by document for multiple documents.
     *
     * @param documentIds list of document identifiers
     * @return list of metadata entries for the documents
     */
    @Query("SELECT dm FROM DocumentMetadata dm WHERE dm.document.id IN :documentIds " +
           "ORDER BY dm.document.id, dm.metadataKey")
    List<DocumentMetadata> findByDocumentIdIn(@Param("documentIds") List<Long> documentIds);

    /**
     * Finds metadata entries with null or empty values.
     *
     * @param documentId the document identifier
     * @return list of metadata entries with missing values
     */
    @Query("SELECT dm FROM DocumentMetadata dm WHERE dm.document.id = :documentId " +
           "AND (dm.metadataValue IS NULL OR dm.metadataValue = '')")
    List<DocumentMetadata> findEmptyValuesByDocument(@Param("documentId") Long documentId);

    // DISABLED: documentTypeId field reference issue - Document entity uses documentType relation
    // /**
    //  * Finds metadata entries for documents of a specific type.
    //  *
    //  * @param documentTypeId the document type identifier
    //  * @return list of metadata entries for documents of the type
    //  */
    // @Query("SELECT dm FROM DocumentMetadata dm JOIN Document d ON dm.document.id = d.id " +
    //        "WHERE d.documentType.id = :documentTypeId")
    // List<DocumentMetadata> findByDocumentTypeId(@Param("documentTypeId") Long documentTypeId);

    /**
     * Gets most common metadata values for a key.
     *
     * @param metadataKey the metadata key
     * @param limit maximum number of results
     * @return list of most common values with counts
     */
    @Query(value = "SELECT dm.metadataValue, COUNT(*) as count FROM DocumentMetadata dm " +
           "WHERE dm.metadataKey = :metadataKey GROUP BY dm.metadataValue " +
           "ORDER BY count DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> findMostCommonValuesByKey(
            @Param("metadataKey") String metadataKey,
            @Param("limit") int limit);
}
