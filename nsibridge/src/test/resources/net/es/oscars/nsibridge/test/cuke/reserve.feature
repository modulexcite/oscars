Feature: new reservation

  I want to verify I can create a new reservation

  Scenario: Submit new reservation internally through Java
    Given I have set up Spring
    Given I have started the scheduler
    Given that I know the count of all pending reservation requests
    When I submit reserve() with connectionId: "reserve-connid"
    Then the count of ConnectionRecords with connectionId: "reserve-connid" is 1
    Then the count of pending reservation requests has changed by 1
    Then the ReserveStateMachine state for connectionId: "reserve-connid" is: "ReserveChecking"
    Then the ResvRequest for connectionId: "reserve-connid" has OscarsOp: "RESERVE"

    When I wait until the LocalResvTask for connectionId: "reserve-connid" has completed
    Then the ReserveStateMachine state for connectionId: "reserve-connid" is: "ReserveHeld"




  Scenario: Submit new reservation internally through Java (without a connectionId)
    Given I have set up Spring
    Given that I know the count of all ConnectionRecords
    Given that I know the count of all pending reservation requests
    When I submit reserve() with connectionId: ""
    Then the count of all ConnectionRecords has changed by 1
    Then the count of pending reservation requests has changed by 1

