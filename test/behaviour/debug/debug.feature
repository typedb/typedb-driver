# Paste any scenarios below for debugging.
# Do not commit any changes to this file.

Feature: Keyspace

  Background:
    Given connection has been opened
    Given connection delete all keyspaces
    Given connection does not have any keyspace

  Scenario: connection can create one keyspace
    When  connection create one keyspace: alice
x   `x`    Then  connection has one keyspace: alice

  Scenario: connection can create multiple keyspaces
    When  connection create multiple keyspaces:
      | alice   |
      | bob     |
      | charlie |
      | dylan   |
    Then  connection has multiple keyspaces:
      | alice   |
      | bob     |
      | charlie |
      | dylan   |