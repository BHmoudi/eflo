Feature: Document Expiration Management
  As a document management system
  I want to track document expiration dates
  So that users are notified before documents expire and expired documents are handled appropriately

  Scenario: Expiration notification for expiring document
    Given a document with expiration date in 30 days
    When the expiration check scheduler runs
    Then an expiration notification should be sent
    And the document should be flagged for review

  Scenario: Auto-archive expired document
    Given a document that expired 1 day ago
    And auto-archive is enabled
    When the expiration check scheduler runs
    Then the document should be archived
    And a document.archived event should be published

  Scenario: Identify documents expiring within warning period
    Given documents with various expiration dates exist
    When the system checks for documents expiring within 30 days
    Then documents expiring within the warning period should be identified
    And expiration notifications should be prepared

  Scenario: Extend document expiration date
    Given a document has an expiration date
    When the expiration date is extended by 90 days
    Then the new expiration date should be calculated correctly
    And the expiration notification flag should be reset

  Scenario: Batch process expired documents
    Given expired documents exist in the system
    When the batch expiration process runs
    Then all expired documents should be processed
    And appropriate actions should be taken based on configuration

  Scenario: Notification for document expiring soon
    Given a document with expiration date in 15 days
    When the expiration check scheduler runs
    Then an expiration notification should be sent
    And the document should be flagged for review

  Scenario: Multiple expiration warnings
    Given documents with various expiration dates exist
    When the system checks for documents expiring within 45 days
    Then documents expiring within the warning period should be identified
    And expiration notifications should be prepared

  Scenario: Expired document auto-archive workflow
    Given a document that expired 1 day ago
    And auto-archive is enabled
    When the expiration check scheduler runs
    Then the document should be archived
    And a document.archived event should be published
