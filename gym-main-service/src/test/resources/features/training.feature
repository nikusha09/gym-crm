Feature: Training management

  Scenario: Successfully add training
    Given a trainee registration request with first name "John" and last name "Smith"
    And a trainer registration request with first name "Mike" and last name "Brown" and specialization "1"
    When the client sends trainee registration request for "John.Smith"
    And the client sends trainer registration request for "Mike.Brown"
    And the client logs in with username "John.Smith"
    And the client adds training with trainee "John.Smith", trainer "Mike.Brown", name "Yoga", duration 60
    Then the response status should be 200

  Scenario: Successfully add training and verify persistence
    Given a trainee registration request with first name "John" and last name "Smith"
    And a trainer registration request with first name "Mike" and last name "Brown" and specialization "1"
    When the client sends trainee registration request for "John.Smith"
    And the client sends trainer registration request for "Mike.Brown"
    And the client logs in with username "John.Smith"
    And the client adds training with trainee "John.Smith", trainer "Mike.Brown", name "Yoga", duration 60
    Then the training should be stored in database

  Scenario: Add training without authentication fails
    Given a trainee registration request with first name "John" and last name "Smith"
    And a trainer registration request with first name "Mike" and last name "Brown" and specialization "1"
    When the client sends trainee registration request for "John.Smith"
    And the client sends trainer registration request for "Mike.Brown"
    And the client adds training without token with trainee "John.Smith", trainer "Mike.Brown", name "Yoga", duration 60
    Then the response status should be 403

  Scenario: Add training with non-existing trainee fails
    Given a trainer registration request with first name "Mike" and last name "Brown" and specialization "1"
    When the client sends trainer registration request for "Mike.Brown"
    And the client logs in with username "Mike.Brown"
    And the client adds training with trainee "NonExisting.Trainee", trainer "Mike.Brown", name "Yoga", duration 60
    Then the response status should be 404

  Scenario: Add training with negative duration fails
    Given a trainee registration request with first name "John" and last name "Smith"
    And a trainer registration request with first name "Mike" and last name "Brown" and specialization "1"
    When the client sends trainee registration request for "John.Smith"
    And the client sends trainer registration request for "Mike.Brown"
    And the client logs in with username "John.Smith"
    And the client adds training with trainee "John.Smith", trainer "Mike.Brown", name "Yoga", duration -1
    Then the response status should be 400