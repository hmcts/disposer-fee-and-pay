Feature: Fee and Pay disposer
  Test scenarios for the Fee and Pay disposer

  Scenario: Example scenario
    Given WireMock is running
    When We make a request to WireMock
    Then We receive a response from WireMock

  Scenario: Retrieve closed cases from CCD and delete payments
    Given CCD returns closed cases for the eligible date
    And Fee and Pay returns payments for the closed cases
    And S2S returns a service token
    And IdAM returns a user access token
    When the payment disposer runs
    Then the closed case references are returned
    And all payment deletions succeed
    And CCD was called with user and service authorization headers
    And Fee and Pay delete endpoints were called

  Scenario: Retrieve empty list when CCD has no closed cases
    Given CCD returns not found for the eligible date
    And S2S returns a service token
    And IdAM returns a user access token
    When the payment disposer runs
    Then no closed case references are returned

  Scenario: Mixed Fee and Pay deletion outcomes are reported as multi-status
    Given CCD returns closed cases for the eligible date
    And Fee and Pay fails deletion for one payment after retries
    And S2S returns a service token
    And IdAM returns a user access token
    When the payment disposer runs
    Then the disposer reports a multi-status partial success
