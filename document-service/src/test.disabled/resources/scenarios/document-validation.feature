Feature: Document Validation Workflow
  As a document manager
  I want to validate or reject uploaded documents
  So that only approved documents are used in order processing

  Scenario: Document manager validates document
    Given a document is pending validation
    And a document manager is assigned to validate
    When the document manager validates the document
    Then the document status should be VALIDATED
    And the order document status should be updated
    And a document.validated event should be published

  Scenario: Document manager rejects document
    Given a document is pending validation
    And a document manager is assigned to validate
    When the document manager rejects the document with reason "Invalid format"
    Then the document status should be REJECTED
    And a document.rejected event should be published

  Scenario: Validated document has complete audit trail
    Given a document is pending validation
    And a document manager is assigned to validate
    When the document manager validates the document
    Then the document status should be VALIDATED
    And the document has validation comments
    And the validation timestamp should be recorded
    And a document.validated event should be published

  Scenario: Reject document with detailed reason
    Given a document is pending validation
    And a document manager is assigned to validate
    When the document manager rejects the document with reason "Document quality is insufficient - text is not readable"
    Then the document status should be REJECTED
    And a document.rejected event should be published

  Scenario: Batch validate all documents for an order
    Given multiple documents are pending validation for order "ORD-BATCH-001"
    And a document manager is assigned to validate
    When the document manager validates all documents for the order
    Then all documents should be validated
    And an all.documents.validated event should be published

  Scenario: Document validation workflow completes successfully
    Given a document is pending validation
    And a document manager is assigned to validate
    When the document manager validates the document
    Then the document status should be VALIDATED
    And the order document status should be updated
    And the document has validation comments
    And the validation timestamp should be recorded
