Feature: Trainer Workload Retrieval

  Background:
    Given the workload service is running

  Scenario: Successfully retrieve workload for existing trainer
    Given a workload exists for trainer "John.Smith" in year 2025 month 3 with duration 60
    When the workload is requested for trainer "John.Smith"
    Then the response should contain trainer username "John.Smith"
    And the response should contain firstName "John"
    And the response should contain lastName "Smith"
    And the workload for trainer "John.Smith" in year 2025 month 3 should be 60

  Scenario: Successfully retrieve workload with multiple months
    Given a workload exists for trainer "Jane.Doe" in year 2025 month 3 with duration 60
    And a workload exists for trainer "Jane.Doe" in year 2025 month 4 with duration 45
    When the workload is requested for trainer "Jane.Doe"
    Then the response should contain trainer username "Jane.Doe"
    And the workload for trainer "Jane.Doe" in year 2025 month 3 should be 60
    And the workload for trainer "Jane.Doe" in year 2025 month 4 should be 45

  Scenario: Successfully retrieve workload with multiple years
    Given a workload exists for trainer "Bob.Smith" in year 2025 month 3 with duration 60
    And a workload exists for trainer "Bob.Smith" in year 2026 month 3 with duration 90
    When the workload is requested for trainer "Bob.Smith"
    Then the response should contain trainer username "Bob.Smith"
    And the workload for trainer "Bob.Smith" in year 2025 month 3 should be 60
    And the workload for trainer "Bob.Smith" in year 2026 month 3 should be 90

  Scenario: Retrieve workload for non-existing trainer throws exception
    Given no workload exists for trainer "Unknown.Trainer"
    When the workload is requested for trainer "Unknown.Trainer"
    Then a runtime exception should be thrown with message "No workload found for trainer: Unknown.Trainer"