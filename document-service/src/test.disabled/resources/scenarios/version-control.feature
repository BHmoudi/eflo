Feature: Document Version Control
  As a document management system
  I want to maintain version history of documents
  So that document changes are tracked and previous versions can be referenced

  Scenario: Create new document version
    Given a validated document exists
    When a user uploads a replacement file
    Then a new version should be created
    And the old version should be marked as superseded
    And a document.version.created event should be published

  Scenario: Version number increments correctly
    Given a validated document exists with version 1
    When a user uploads a replacement file
    Then the new version number should be 2
    And the old version should have isLatestVersion set to false
    And the new version should have isLatestVersion set to true

  Scenario: Version chain maintains parent-child relationship
    Given a validated document exists
    When a user uploads a replacement file
    Then the new version should reference the old version as parent
    And the old version should reference the new version as replacedBy

  Scenario: Multiple version chain
    Given a validated document exists with version 1
    When a user uploads a replacement file creating version 2
    And a user uploads another replacement file creating version 3
    Then version 3 should be the latest version
    And version 2 should have version 3 as replacedBy
    And version 1 should have version 2 as replacedBy
    And the version chain should be complete

  Scenario: Version history is queryable
    Given a document with multiple versions exists
    When the system retrieves all versions for the order
    Then all versions should be returned in order
    And each version should have correct parent references
    And only the latest version should be marked as current

  Scenario: New version inherits metadata from parent
    Given a validated document exists with metadata
    When a user uploads a replacement file
    Then the new version should inherit the order information
    And the new version should inherit the document type
    And the new version should maintain the same business context
