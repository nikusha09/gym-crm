Feature: Integration

  Scenario: Adding a training sends workload message to queue
    Given a trainee registration request with first name "John" and last name "Smith"
    And a trainer registration request with first name "Mike" and last name "Brown" and specialization "1"
    When the client sends trainee registration request for "John.Smith"
    And the client sends trainer registration request for "Mike.Brown"
    And the client logs in with username "John.Smith"
    And the client adds training with trainee "John.Smith", trainer "Mike.Brown", name "Yoga", duration 60
    Then a message should be sent to the workload queue

  Scenario: Deleting a training sends DELETE workload message to queue
    Given a trainee registration request with first name "John" and last name "Smith"
    And a trainer registration request with first name "Mike" and last name "Brown" and specialization "1"
    When the client sends trainee registration request for "John.Smith"
    And the client sends trainer registration request for "Mike.Brown"
    And the client logs in with username "John.Smith"
    And the client adds training with trainee "John.Smith", trainer "Mike.Brown", name "Yoga", duration 60
    And the client deletes the training
    Then a message should be sent to the workload queue

  Scenario: Adding a training sends message with ADD action type
    Given a trainee registration request with first name "John" and last name "Smith"
    And a trainer registration request with first name "Mike" and last name "Brown" and specialization "1"
    When the client sends trainee registration request for "John.Smith"
    And the client sends trainer registration request for "Mike.Brown"
    And the client logs in with username "John.Smith"
    And the client adds training with trainee "John.Smith", trainer "Mike.Brown", name "Yoga", duration 60
    Then the workload message should contain action type "ADD"

  Scenario: Adding a training sends message with correct trainer details
    Given a trainee registration request with first name "John" and last name "Smith"
    And a trainer registration request with first name "Mike" and last name "Brown" and specialization "1"
    When the client sends trainee registration request for "John.Smith"
    And the client sends trainer registration request for "Mike.Brown"
    And the client logs in with username "John.Smith"
    And the client adds training with trainee "John.Smith", trainer "Mike.Brown", name "Yoga", duration 60
    Then the workload message should contain trainer username "Mike.Brown" and duration 60

  Scenario: Adding a training persists in database and sends message to queue
    Given a trainee registration request with first name "John" and last name "Smith"
    And a trainer registration request with first name "Mike" and last name "Brown" and specialization "1"
    When the client sends trainee registration request for "John.Smith"
    And the client sends trainer registration request for "Mike.Brown"
    And the client logs in with username "John.Smith"
    And the client adds training with trainee "John.Smith", trainer "Mike.Brown", name "Yoga", duration 60
    Then the training should be stored in database
    And a message should be sent to the workload queue