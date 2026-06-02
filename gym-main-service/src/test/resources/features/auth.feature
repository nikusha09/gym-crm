Feature: Authentication

  Scenario: Successful login
    Given a trainee registration request with first name "John" and last name "Smith"
    When the client sends trainee registration request for "John.Smith"
    And the client logs in with username "John.Smith"
    Then the response status should be 200

  Scenario: Login with wrong password fails
    Given a trainee registration request with first name "John" and last name "Smith"
    When the client sends trainee registration request for "John.Smith"
    And the client logs in with username "John.Smith" and wrong password "wrongpass"
    Then the response status should be 401

  Scenario: Login with non-existing username fails
    When the client logs in with username "NonExisting.User" and wrong password "somepassword"
    Then the response status should be 401