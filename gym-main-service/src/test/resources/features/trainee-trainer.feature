Feature: Trainee/Trainer management

  Scenario: Successfully register trainee
    Given a trainee registration request with first name "John" and last name "Smith"
    When the client sends trainee registration request for "John.Smith"
    Then the response status should be 201
    And the trainee should be stored in database

  Scenario: Register trainee when username already exists
    Given a trainee registration request with first name "John" and last name "Smith"
    And a trainee registration request with first name "John" and last name "Smith"
    When the client sends trainee registration request for "John.Smith"
    And the client sends trainee registration request for "John.Smith"
    Then the trainee "first" should have username "John.Smith"
    And the trainee "second" should have username "John.Smith1"

  Scenario: Register trainee with missing first name fails
    When the client sends trainee registration request with missing first name
    Then the response status should be 400

  Scenario: Register trainer with invalid specialization fails
    Given a trainer registration request with first name "Mike" and last name "Brown" and specialization "999"
    When the client sends trainer registration request for "Mike.Brown"
    Then the response status should be 500

  Scenario: Register trainer when username already exists gets suffix
    Given a trainer registration request with first name "Mike" and last name "Brown" and specialization "1"
    And a trainer registration request with first name "Mike" and last name "Brown" and specialization "1"
    When the client sends trainer registration request for "Mike.Brown"
    And the client sends trainer registration request for "Mike.Brown2"
    Then the trainer "Mike.Brown" should exist in database
    And the trainer "Mike.Brown1" should exist in database

  Scenario: Get trainee profile successfully
    Given a trainee registration request with first name "John" and last name "Smith"
    When the client sends trainee registration request for "John.Smith"
    And the client logs in with username "John.Smith"
    And the client gets trainee profile for "John.Smith"
    Then the response status should be 200
    And the response should contain first name "John" and last name "Smith"

  Scenario: Register trainer successfully and verify persistence in database
    Given a trainer registration request with first name "Mike" and last name "Brown" and specialization "1"
    When the client sends trainer registration request for "Mike.Brown"
    Then the response status should be 201
    And the trainer "Mike.Brown" should exist in database

  Scenario: Get trainer profile successfully
    Given a trainer registration request with first name "Mike" and last name "Brown" and specialization "1"
    When the client sends trainer registration request for "Mike.Brown"
    And the client logs in with username "Mike.Brown"
    And the client gets trainer profile for "Mike.Brown"
    Then the response status should be 200