Feature: VN Workflow Process
  As a workflow user
  I want to execute a complete VN workflow
  So that orders can be processed through all required states

  Background:
    Given a VN workflow process exists with code "VN_STANDARD"
    And the workflow has the following states:
      | State Code | State Name | State Type |
      | START      | Initial    | START      |
      | REVIEW     | Review     | NORMAL     |
      | APPROVAL   | Approval   | DECISION   |
      | COMPLETED  | Completed  | FINAL      |

  Scenario: Successfully complete a VN workflow instance
    Given an order exists with ID 100
    When I create a workflow instance for order 100 using process "VN_STANDARD"
    Then the instance should be in "CREATED" status
    And the instance should not have a current state

    When I start the workflow instance
    Then the instance should be in "RUNNING" status
    And the current state should be "Initial"
    And tasks should be created for the "Initial" state

    When I complete all mandatory tasks for the current state
    And I transition to state "Review"
    Then the current state should be "Review"
    And tasks should be created for the "Review" state

    When I complete all mandatory tasks for the current state
    And I transition to state "Approval"
    Then the current state should be "Approval"

    When I approve the workflow
    And I transition to state "Completed"
    Then the instance should be in "COMPLETED" status
    And the current state should be "Completed"

  Scenario: Handle task overdue escalation
    Given a workflow instance is running
    And a task has been assigned for 48 hours
    And the task has not been completed
    When the escalation scheduler runs
    Then the task should be escalated
    And an escalation event should be published
    And an escalation notification should be sent

  Scenario: Pause and resume workflow
    Given a running workflow instance exists
    When I pause the instance
    Then the instance should be in "PAUSED" status
    And tasks should not be actionable

    When I resume the instance
    Then the instance should be in "RUNNING" status
    And tasks should become actionable again

  Scenario: Cancel workflow instance
    Given a running workflow instance exists
    When I cancel the instance with reason "Business requirement changed"
    Then the instance should be in "CANCELLED" status
    And all pending tasks should be cancelled
    And a cancellation event should be published
