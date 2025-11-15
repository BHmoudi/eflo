Feature: Document Upload and Validation
  As a vehicle order processing system
  I want to handle document uploads securely
  So that customer documents are stored and validated properly

  Background:
    Given a new vehicle order exists
    And the order requires identity document validation

  Scenario: Upload identity document for new vehicle order
    When a user uploads a valid identity card PDF
    Then the document should be stored in MinIO
    And the document status should be PENDING
    And a validation task should be created
    And a document.uploaded event should be published

  Scenario: Successful document upload with all validations
    When a user uploads a valid identity card PDF
    Then the document should be uploaded successfully
    And the document should be stored in MinIO
    And the virus scan status should not be null
    And the document status should be PENDING
    And a document.uploaded event should be published

  Scenario: Upload multiple document types for an order
    Given a document type "IDENTITY_CARD" exists
    And a document type "DRIVING_LICENSE" exists
    And an order "ORD-MULTI-001" exists with ID 5100
    When the user uploads a "application/pdf" file named "identity-card.pdf"
    And the user uploads a "application/pdf" file named "driving-license.pdf"
    Then the document should be uploaded successfully
    And a document.uploaded event should be published

  Scenario: Document upload creates proper metadata
    When a user uploads a valid identity card PDF
    Then the document should be uploaded successfully
    And the document filename should be "identity-card.pdf"
    And the document status should be PENDING
    And the virus scan status should not be null

  Scenario: Document stored with correct order association
    When a user uploads a valid identity card PDF
    Then the document should be uploaded successfully
    And the document should be stored in MinIO
    And the document status should be PENDING
