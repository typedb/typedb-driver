#
# GRAKN.AI - THE KNOWLEDGE GRAPH
# Copyright (C) 2020 Grakn Labs Ltd
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <https://www.gnu.org/licenses/>.
#

Feature: Theorems over Writes


# for all of these, we check the newly created schema concept exists first!


# ------ DEFINE theorems -----

  Scenario: Empty graph is Valid
    Given KB is valid

  Scenario: define subtype creates child of supertype
    Given "define person sub entity;"
    Given KB is valid

    Given "define child sub person;"
    Given KB is valid

    Then query 2: "match $x type child; get;" has 1 answer
    Then query 1: "match $x sub person; get;" has 2 answers

  Scenario: define entity inherits plays, has, key from supertypes
    Given "define
      person sub entity,
        plays employee,
        has name,
        key email;
      employment sub relation, relates employee;
      name sub attribute, datatype string;
      email sub attribute, datatype string;"

    Given KB is valid

    Given "define child sub person;"

    Given KB is valid

    Then query 1: "match $x plays employee; get;" has 2 answers
    Then query 2: "match $x has name; get;" has 2 answers
    Then query 3: "match $x key email; get;" has 2 answers

    Then query 4: "match $x type child, plays employee; get;" has 1 answer
    Then query 5: "match $x type child, has name; get;" has 1 answer
    Then query 6: "match $x type child, key email; get;" has 1 answer

    Then answers of query 1, query 4 contain the same $x combination 1 time
    Then answers of query 2, query 5 contain the same $x combination 1 time
    Then answers of query 3, query 6 contain the same $x combination 1 time


  Scenario: define relation subtype inherits relates, plays, has, key from supertypes

  Scenario: define attribute subtype inherits ownerships, plays, has, key and same datatype as supertype

  Scenario: define additional `plays` is visible from all children

  Scenario: define additional `has` is visible from all children

  Scenario: define additional `key` is visible from all children

  Scenario: define additional `relates` is visible from all children

  Scenario: define a type as abstract errors if has non-abstract parent types (?)

  Scenario: define a type as abstract creates an abstract type

  Scenario: define a regex on an attribute type, attribute type queryable by regex value

  Scenario: define a rule creates a rule (?)

  Scenario: define a sub-role using as is visible from children (?)

# ----- INSERT theorems -----

  Scenario: inserting an instance creates instance of that type

  Scenario: inserting a relation is visible from role players

  Scenario: inserting an additional role player is visible in the relation
    Given "define
        person sub entity,
          plays employee;
        company sub entity,
          plays employer;
        employment sub relation,
          relates employee,
          relates employee;"

    Given KB is valid

    Given query 1: "insert $p isa person; $r (employee: $p) isa employment;" has 1 answer
    Given query 2: "match $r isa employment; insert $r (employer: $c) isa employment; $c isa company;" has 1 answer

    Given KB is valid

    Then query 3: "match $r (employer: $c, employee: $p) isa employment; get;" has 1 answer

    Then answers of query 1, query 3 contain the same $r,$p combinations 1 time
    Then answers of query 2, query 3 contain the same $r,$c combinations 1 time

  Scenario: xxx xxx
    Given define
    | person sub entity, plays employee |
    | person sub entity, plays employee |
    Given KB is valid
    When insert $r isa marriage, has license "abc";
    When answers = match ($x, $y) isa locates; ($y, $z) isa locates; get;
    Then for each answer = match ($x, $z) isa locates; $x id 123; $y id 234; get;
    When for each answer = match insert

  Scenario: xxx xxx
    Given define
      | person sub entity, plays employee |
      | person sub entity, plays employee |
    Given KB is valid
    When insert $r isa marriage, has license "abc";
    When answers = match $r isa marriage, has license "abc";
    Then answer has size: 1

    Then for each ans in answer = match $r id <ans.r.id>; insert $r (wife: $alice); $alice isa person, has name "alice";
    Then for each ans in answer = match $r id <ans.r.id>; insert $r (husband: $bob); $bob isa person, has name "bob";
    When for each ans in answer = match (wife: $alice, husband: $bob) isa marriage, id {ans.r.id}; $alice isa ... returns 1 answer


  Scenario: inserting an attribute with a value is retrievable by the value
    Given define
    | name sub attribute, datatype string; |
    Given insert
    | "haikal" isa name; |
    When answers = match $x "haikal"; get;
    Then answers has size 1;
    Then for each answer: match $x isa name; get;


  Scenario: inserting an attribute that already retrieves existing instance

  Scenario: inserting two owners of the same attribute separately are linked to same attribute

  Scenario: inserting attribute ownership visible from attribute and owner

  Scenario: inserting a regex attribute errors if not conforming to regex

# ----- DELETE theorems -----

  Scenario: deleted instance no longer visible as instance of its type

  Scenario: deleted role player is not visible in relation

  Scenario: deleted attribute not visible from any owner of attribute

  Scenario: deleted attribute owning instance not visible from attributes

  Scenario: deleting attribute ownership retains attribute and owner but no mutual visibility

# ----- UNDEFINE theorems -----

  Scenario: undefine subtype removes child of supertype

  Scenario: undefine supertype errors as it has children

  Scenario: undefine `plays` removes from all children

  Scenario: undefine `has` removes from all children

  Scenario: undefine `key` removes from all children

  Scenario: undefine `relates` removes from all children

  Scenario: undefine a type as abstract errors if has non-abstract children (?)

  Scenario: undefine a type as abstract, type no longer queryable as abstract and can create types

  Scenario: undefine a regex on an attribute type, attribute type not queryable by regex value

  Scenario: undefine a rule removes the rule (?)

  Scenario: undefine a sub-role using `as` no longer visible to children (?)








