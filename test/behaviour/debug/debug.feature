Feature: Debugging Space

  Background:
    Given connection has been opened
    Given connection delete all keyspaces
    Given connection open sessions for keyspaces:
      | test_undefine |
    Given transaction is initialised
    Given the integrity is validated
    Given graql define
      """
      define
      person sub entity, plays employee, has name, key email;
      employment sub relation, relates employee, relates employer;
      name sub attribute, value string;
      email sub attribute, value string, regex ".+@\w+\..+";
      abstract-type sub entity, abstract;
      """
    Given the integrity is validated

  # Paste any scenarios below for debugging.
  # Do not commit any changes to this file.


  Scenario: undefining the wrong regex from an attribute type does nothing
    When graql undefine
      """
      undefine email regex ".+@\w.com";
      """
    Then the integrity is validated
    When get answers of graql query
      """
      match $x regex ".+@\w+\..+"; get;
      """
    When concept identifiers are
      |     | check | value |
      | EMA | label | email |
    Then uniquely identify answer concepts
      | x   |
      | EMA |
