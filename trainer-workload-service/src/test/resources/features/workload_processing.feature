Feature: Trainer Workload Processing

  Background:
    Given the workload service is running

  Scenario: Successfully add workload for a new trainer
    Given no workload exists for trainer "John.Smith"
    When a workload event is received for trainer "John.Smith" with firstName "John" lastName "Smith" duration 60 date "2025-03-15" action "ADD"
    Then the workload for trainer "John.Smith" in year 2025 month 3 should be 60

  Scenario: Successfully add workload for existing trainer same month
    Given a workload exists for trainer "Jane.Doe" in year 2025 month 3 with duration 60
    When a workload event is received for trainer "Jane.Doe" with firstName "Jane" lastName "Doe" duration 30 date "2025-03-20" action "ADD"
    Then the workload for trainer "Jane.Doe" in year 2025 month 3 should be 90

  Scenario: Successfully add workload for existing trainer different month
    Given a workload exists for trainer "Bob.Smith" in year 2025 month 3 with duration 60
    When a workload event is received for trainer "Bob.Smith" with firstName "Bob" lastName "Smith" duration 45 date "2025-04-10" action "ADD"
    Then the workload for trainer "Bob.Smith" in year 2025 month 3 should be 60
    And the workload for trainer "Bob.Smith" in year 2025 month 4 should be 45

  Scenario: Successfully delete workload for existing trainer
    Given a workload exists for trainer "Mike.Johnson" in year 2025 month 3 with duration 60
    When a workload event is received for trainer "Mike.Johnson" with firstName "Mike" lastName "Johnson" duration 30 date "2025-03-15" action "DELETE"
    Then the workload for trainer "Mike.Johnson" in year 2025 month 3 should be 30

  Scenario: Delete workload does not go below zero
    Given a workload exists for trainer "Tom.Brown" in year 2025 month 3 with duration 30
    When a workload event is received for trainer "Tom.Brown" with firstName "Tom" lastName "Brown" duration 60 date "2025-03-15" action "DELETE"
    Then the workload for trainer "Tom.Brown" in year 2025 month 3 should be 0

  Scenario: Add workload for new year
    Given a workload exists for trainer "Sarah.Jones" in year 2025 month 3 with duration 60
    When a workload event is received for trainer "Sarah.Jones" with firstName "Sarah" lastName "Jones" duration 90 date "2026-03-15" action "ADD"
    Then the workload for trainer "Sarah.Jones" in year 2025 month 3 should be 60
    And the workload for trainer "Sarah.Jones" in year 2026 month 3 should be 90

  Scenario: Workload event with missing username is rejected
    When a workload event is received with missing username and firstName "John" lastName "Smith" duration 60 date "2025-03-15" action "ADD"
    Then the event should be rejected with validation error "Trainer username is missing"

  Scenario: Workload event with missing first name is rejected
    When a workload event is received for trainer "John.Smith" with missing firstName lastName "Smith" duration 60 date "2025-03-15" action "ADD"
    Then the event should be rejected with validation error "Trainer first name is missing"

  Scenario: Workload event with missing last name is rejected
    When a workload event is received for trainer "John.Smith" with firstName "John" missing lastName duration 60 date "2025-03-15" action "ADD"
    Then the event should be rejected with validation error "Trainer last name is missing"

  Scenario: Workload event with invalid duration is rejected
    When a workload event is received for trainer "John.Smith" with firstName "John" lastName "Smith" duration 0 date "2025-03-15" action "ADD" and validation is applied
    Then the event should be rejected with validation error "Training duration is invalid"

  Scenario: Workload event with missing action type is rejected
    When a workload event is received for trainer "John.Smith" with firstName "John" lastName "Smith" duration 60 date "2025-03-15" with missing action
    Then the event should be rejected with validation error "Action type is missing"

  Scenario: Workload event with missing training date is rejected
    When a workload event is received for trainer "John.Smith" with firstName "John" lastName "Smith" duration 60 with missing date action "ADD"
    Then the event should be rejected with validation error "Training date is missing"