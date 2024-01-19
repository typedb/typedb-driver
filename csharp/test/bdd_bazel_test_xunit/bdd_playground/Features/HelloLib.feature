Feature: Hello World
  Simple test for BDD feature of the Hello World to test that it works well with bazel!

Scenario: Normally say hello to the world
  Given the name is world
  When they meet Helloer
  Then he says 'Hello, world!'

Scenario: Normally say hello to the world in caps
  Given the name is WORLD
  When they meet Helloer
  Then he says 'Hello, WORLD!'

Scenario: Normally say hello to Jenny
  Given the name is Jenny
  When they meet Helloer
  Then he says 'Hello, Jenny!'

Scenario: Normally say hello to a number
  Given the name is 578
  When they meet Helloer
  Then he says 'Hello, 578!'

Scenario: Say an extravagant what's up to John
  Given the name is John
  When they meet Helloer
  Then he says 'What's up, John!'