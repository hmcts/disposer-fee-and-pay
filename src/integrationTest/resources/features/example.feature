Feature: Fee and Pay disposer
  Test scenarios for the Fee and Pay disposer

  Scenario: Example scenario
    Given WireMock is running
    When We make a request to WireMock
    Then We receive a response from WireMock

  Scenario: Retrieve closed cases from CCD
    Given CCD returns closed cases for the eligible date
    And S2S returns a service token
    When the payment disposer runs
    Then the closed case references are returned
    And CCD was called with user and service authorization headers

  Scenario: Retrieve empty list when CCD has no closed cases
    Given CCD returns not found for the eligible date
    And S2S returns a service token
    When the payment disposer runs
    Then no closed case references are returned
