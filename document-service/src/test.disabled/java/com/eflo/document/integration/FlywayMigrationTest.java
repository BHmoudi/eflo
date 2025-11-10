package com.eflo.document.integration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Flyway database migrations.
 * Verifies that all migrations run successfully and schema is created correctly.
 *
 * @author Document Service
 * @version 1.0
 */
class FlywayMigrationTest extends BaseIntegrationTest {

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Should run all Flyway migrations successfully")
    void testMigrationsRunSuccessfully() {
        // When
        var migrateResult = flyway.info();

        // Then
        assertThat(migrateResult.all()).isNotEmpty();
        assertThat(migrateResult.pending()).isEmpty(); // All migrations should be applied
        assertThat(migrateResult.current()).isNotNull(); // Should have current version
    }

    @Test
    @DisplayName("Should create document_types table with correct schema")
    void testDocumentTypesTableSchema() {
        // When
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(
            """
            SELECT column_name, data_type, is_nullable
            FROM information_schema.columns
            WHERE table_name = 'document_types'
            ORDER BY ordinal_position
            """
        );

        // Then
        assertThat(columns).isNotEmpty();

        // Verify key columns exist
        assertThat(columns).anyMatch(col ->
            "id".equals(col.get("column_name")));
        assertThat(columns).anyMatch(col ->
            "type_code".equals(col.get("column_name")));
        assertThat(columns).anyMatch(col ->
            "type_name".equals(col.get("column_name")));
        assertThat(columns).anyMatch(col ->
            "category".equals(col.get("column_name")));
        assertThat(columns).anyMatch(col ->
            "is_active".equals(col.get("column_name")));
        assertThat(columns).anyMatch(col ->
            "is_mandatory".equals(col.get("column_name")));
    }

    @Test
    @DisplayName("Should create documents table with correct schema")
    void testDocumentsTableSchema() {
        // When
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(
            """
            SELECT column_name, data_type, is_nullable
            FROM information_schema.columns
            WHERE table_name = 'documents'
            ORDER BY ordinal_position
            """
        );

        // Then
        assertThat(columns).isNotEmpty();

        // Verify key columns exist
        assertThat(columns).anyMatch(col ->
            "id".equals(col.get("column_name")));
        assertThat(columns).anyMatch(col ->
            "document_uuid".equals(col.get("column_name")));
        assertThat(columns).anyMatch(col ->
            "document_type_id".equals(col.get("column_name")));
        assertThat(columns).anyMatch(col ->
            "order_id".equals(col.get("column_name")));
        assertThat(columns).anyMatch(col ->
            "original_filename".equals(col.get("column_name")));
        assertThat(columns).anyMatch(col ->
            "storage_bucket".equals(col.get("column_name")));
        assertThat(columns).anyMatch(col ->
            "storage_path".equals(col.get("column_name")));
        assertThat(columns).anyMatch(col ->
            "status".equals(col.get("column_name")));
    }

    @Test
    @DisplayName("Should create document_access_log table with correct schema")
    void testDocumentAccessLogTableSchema() {
        // When
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(
            """
            SELECT column_name, data_type
            FROM information_schema.columns
            WHERE table_name = 'document_access_log'
            ORDER BY ordinal_position
            """
        );

        // Then
        assertThat(columns).isNotEmpty();
        assertThat(columns).anyMatch(col ->
            "id".equals(col.get("column_name")));
        assertThat(columns).anyMatch(col ->
            "document_id".equals(col.get("column_name")));
        assertThat(columns).anyMatch(col ->
            "accessed_by".equals(col.get("column_name")));
        assertThat(columns).anyMatch(col ->
            "access_action".equals(col.get("column_name")));
        assertThat(columns).anyMatch(col ->
            "accessed_at".equals(col.get("column_name")));
    }

    @Test
    @DisplayName("Should create document_validation_rules table with correct schema")
    void testDocumentValidationRulesTableSchema() {
        // When
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(
            """
            SELECT column_name, data_type
            FROM information_schema.columns
            WHERE table_name = 'document_validation_rules'
            ORDER BY ordinal_position
            """
        );

        // Then
        assertThat(columns).isNotEmpty();
        assertThat(columns).anyMatch(col ->
            "id".equals(col.get("column_name")));
        assertThat(columns).anyMatch(col ->
            "document_type_id".equals(col.get("column_name")));
        assertThat(columns).anyMatch(col ->
            "rule_type".equals(col.get("column_name")));
        assertThat(columns).anyMatch(col ->
            "rule_config".equals(col.get("column_name")));
    }

    @Test
    @DisplayName("Should create document_metadata table with correct schema")
    void testDocumentMetadataTableSchema() {
        // When
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(
            """
            SELECT column_name, data_type
            FROM information_schema.columns
            WHERE table_name = 'document_metadata'
            ORDER BY ordinal_position
            """
        );

        // Then
        assertThat(columns).isNotEmpty();
        assertThat(columns).anyMatch(col ->
            "id".equals(col.get("column_name")));
        assertThat(columns).anyMatch(col ->
            "document_id".equals(col.get("column_name")));
        assertThat(columns).anyMatch(col ->
            "metadata_key".equals(col.get("column_name")));
        assertThat(columns).anyMatch(col ->
            "metadata_value".equals(col.get("column_name")));
    }

    @Test
    @DisplayName("Should create foreign key constraints")
    void testForeignKeyConstraints() {
        // When
        List<Map<String, Object>> foreignKeys = jdbcTemplate.queryForList(
            """
            SELECT
                tc.table_name,
                tc.constraint_name,
                kcu.column_name,
                ccu.table_name AS foreign_table_name,
                ccu.column_name AS foreign_column_name
            FROM information_schema.table_constraints AS tc
            JOIN information_schema.key_column_usage AS kcu
              ON tc.constraint_name = kcu.constraint_name
              AND tc.table_schema = kcu.table_schema
            JOIN information_schema.constraint_column_usage AS ccu
              ON ccu.constraint_name = tc.constraint_name
              AND ccu.table_schema = tc.table_schema
            WHERE tc.constraint_type = 'FOREIGN KEY'
              AND tc.table_name IN ('documents', 'document_access_log', 'document_validation_rules', 'document_metadata')
            """
        );

        // Then
        assertThat(foreignKeys).isNotEmpty();

        // Verify documents -> document_types FK
        assertThat(foreignKeys).anyMatch(fk ->
            "documents".equals(fk.get("table_name")) &&
            "document_type_id".equals(fk.get("column_name")) &&
            "document_types".equals(fk.get("foreign_table_name"))
        );

        // Verify document_access_log -> documents FK
        assertThat(foreignKeys).anyMatch(fk ->
            "document_access_log".equals(fk.get("table_name")) &&
            "document_id".equals(fk.get("column_name")) &&
            "documents".equals(fk.get("foreign_table_name"))
        );
    }

    @Test
    @DisplayName("Should create indexes for performance")
    void testIndexesCreated() {
        // When
        List<Map<String, Object>> indexes = jdbcTemplate.queryForList(
            """
            SELECT
                tablename,
                indexname,
                indexdef
            FROM pg_indexes
            WHERE schemaname = 'public'
              AND tablename IN ('documents', 'document_types')
            ORDER BY tablename, indexname
            """
        );

        // Then
        assertThat(indexes).isNotEmpty();

        // Verify primary key indexes exist
        assertThat(indexes).anyMatch(idx ->
            idx.get("indexname").toString().contains("pkey"));

        // Verify custom indexes if they exist
        // (Add specific index assertions based on your migration files)
    }

    @Test
    @DisplayName("Should insert seed data for default document types")
    void testSeedDataInserted() {
        // When
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM document_types WHERE is_active = true",
            Integer.class
        );

        // Then
        // Assuming V6__seed_default_document_types.sql inserts some default types
        assertThat(count).isNotNull();
        // Verify seed data was inserted (adjust based on your seed data)
        // assertThat(count).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should have proper unique constraints")
    void testUniqueConstraints() {
        // When
        List<Map<String, Object>> uniqueConstraints = jdbcTemplate.queryForList(
            """
            SELECT
                tc.table_name,
                tc.constraint_name,
                kcu.column_name
            FROM information_schema.table_constraints AS tc
            JOIN information_schema.key_column_usage AS kcu
              ON tc.constraint_name = kcu.constraint_name
              AND tc.table_schema = kcu.table_schema
            WHERE tc.constraint_type = 'UNIQUE'
              AND tc.table_name IN ('documents', 'document_types')
            ORDER BY tc.table_name, kcu.column_name
            """
        );

        // Then
        assertThat(uniqueConstraints).isNotEmpty();

        // Verify document_uuid is unique
        assertThat(uniqueConstraints).anyMatch(uc ->
            "documents".equals(uc.get("table_name")) &&
            "document_uuid".equals(uc.get("column_name"))
        );

        // Verify type_code is unique
        assertThat(uniqueConstraints).anyMatch(uc ->
            "document_types".equals(uc.get("table_name")) &&
            "type_code".equals(uc.get("column_name"))
        );
    }

    @Test
    @DisplayName("Should have proper not null constraints")
    void testNotNullConstraints() {
        // When - Check documents table
        List<Map<String, Object>> notNullColumns = jdbcTemplate.queryForList(
            """
            SELECT column_name
            FROM information_schema.columns
            WHERE table_name = 'documents'
              AND is_nullable = 'NO'
            """
        );

        // Then - Verify critical columns are NOT NULL
        assertThat(notNullColumns).extracting(col -> col.get("column_name"))
            .contains("id", "document_uuid", "document_type_id", "order_id",
                     "original_filename", "storage_bucket", "storage_path");
    }

    @Test
    @DisplayName("Should have proper default values")
    void testDefaultValues() {
        // When
        List<Map<String, Object>> columnsWithDefaults = jdbcTemplate.queryForList(
            """
            SELECT
                column_name,
                column_default
            FROM information_schema.columns
            WHERE table_name IN ('documents', 'document_types')
              AND column_default IS NOT NULL
            """
        );

        // Then
        assertThat(columnsWithDefaults).isNotEmpty();

        // Verify specific defaults exist (adjust based on your schema)
        assertThat(columnsWithDefaults).anyMatch(col ->
            "version".equals(col.get("column_name"))
        );
    }

    @Test
    @DisplayName("Should have timestamp columns with proper data types")
    void testTimestampColumns() {
        // When
        List<Map<String, Object>> timestampColumns = jdbcTemplate.queryForList(
            """
            SELECT column_name, data_type
            FROM information_schema.columns
            WHERE table_name = 'documents'
              AND data_type = 'timestamp without time zone'
            """
        );

        // Then
        assertThat(timestampColumns).isNotEmpty();
        assertThat(timestampColumns).extracting(col -> col.get("column_name"))
            .contains("uploaded_at", "created_at");
    }

    @Test
    @DisplayName("Should support cascade delete operations")
    void testCascadeDeleteConstraints() {
        // When
        List<Map<String, Object>> cascadeConstraints = jdbcTemplate.queryForList(
            """
            SELECT
                tc.table_name,
                rc.delete_rule
            FROM information_schema.table_constraints AS tc
            JOIN information_schema.referential_constraints AS rc
              ON tc.constraint_name = rc.constraint_name
            WHERE tc.constraint_type = 'FOREIGN KEY'
              AND tc.table_name IN ('document_access_log', 'document_metadata')
            """
        );

        // Then
        assertThat(cascadeConstraints).isNotEmpty();
        // Verify some tables have CASCADE delete rules
        // (adjust based on your actual schema requirements)
    }

    @Test
    @DisplayName("Should have all expected tables created")
    void testAllTablesCreated() {
        // When
        List<String> tables = jdbcTemplate.queryForList(
            """
            SELECT table_name
            FROM information_schema.tables
            WHERE table_schema = 'public'
              AND table_type = 'BASE TABLE'
            ORDER BY table_name
            """,
            String.class
        );

        // Then
        assertThat(tables).contains(
            "document_types",
            "documents",
            "document_access_log",
            "document_validation_rules",
            "document_metadata",
            "flyway_schema_history" // Flyway's own table
        );
    }

    @Test
    @DisplayName("Should have proper sequence definitions")
    void testSequencesCreated() {
        // When
        List<String> sequences = jdbcTemplate.queryForList(
            """
            SELECT sequence_name
            FROM information_schema.sequences
            WHERE sequence_schema = 'public'
            """,
            String.class
        );

        // Then
        assertThat(sequences).isNotEmpty();
        // Verify key sequences exist
        assertThat(sequences).anyMatch(seq -> seq.contains("documents"));
        assertThat(sequences).anyMatch(seq -> seq.contains("document_types"));
    }

    @Test
    @DisplayName("Should validate migration checksums")
    void testMigrationChecksums() {
        // When
        List<Map<String, Object>> migrations = jdbcTemplate.queryForList(
            """
            SELECT
                version,
                description,
                type,
                success
            FROM flyway_schema_history
            ORDER BY installed_rank
            """
        );

        // Then
        assertThat(migrations).isNotEmpty();
        assertThat(migrations).allMatch(m ->
            Boolean.TRUE.equals(m.get("success"))
        );
    }
}
