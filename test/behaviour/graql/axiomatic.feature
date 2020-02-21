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


# ----- INSERT theorems -----

  Scenario: inserting an instance creates instance of that type

  Scenario: inserting a relation is visible from role players

  Scenario: inserting an additional role player is visible in the relation --- USING IDs
    Given the schema
      | define                      |
      |  person sub entity,         |
      |    plays employee;          |
      |  company sub entity,        |
      |    plays employer;          |
      |  employment sub relation,   |
      |    relates employee,        |
      |    relates employee;        |

    And the KB is valid

    When executing query 1
      | insert $p isa person; $r (employee: $p) isa employment; |
    And executing query 2
      | match $r isa employment; insert $r (employer: $c) isa employment; $c isa company; |
    And the KB is valid
    And executing query 3
      | match $r (employer: $c, employee: $p) isa employment; get; |

    Then query 1 has 1 answer
    And query 2 has 1 answer
    And query 3 has 1 answer

    And IDs from answers of query 1 satisfy IDs from answers of query 3
    And IDs from answers of query 2 satisfy IDs from answers of query 3






  Scenario: inserting an additional role player is visible in the relation --- USING KEY
    Given the schema
      | define                      |
      |  person sub entity,         |
      |    plays employee,          |
      |    key ref;                 |
      |  company sub entity,        |
      |    plays employer,          |
      |    key ref;                 |
      |  employment sub relation,   |
      |    relates employee,        |
      |    relates employee,        |
      |    key ref;                 |
      |  ref sub attribute,         |
      |    datatype long;           |


    And the KB is valid

    When query
      | insert $p isa person, has ref 0; $r (employee: $p) isa employment, has ref 1; |
    And query
      | match $r isa employment; insert $r (employer: $c) isa employment; $c isa company, has ref 2; |
    And the KB is valid
    Then answers satisfy
      | match $r (employer: $c, employee: $p) isa employment; $r id <answer.r>; $c id <answer.c>; $p id <answer.p> get; |














  Scenario: inserting a relationship with named role players can be retrieved without role players

  Scenario: inserting an additional role player is visible in the relation --- USING KEY
    Given the schema
      | define                      |
      |  person sub entity,         |
      |    plays employee,          |
      |    key ref;                 |
      |  company sub entity,        |
      |    plays employer,          |
      |    key ref;                 |
      |  employment sub relation,   |
      |    relates employee,        |
      |    relates employee,        |
      |    key ref;                 |
      |  ref sub attribute,         |
      |    datatype long;           |


    And the KB is valid

    When executing query 1
      | insert $p isa person, has ref 0; $r (employee: $p) isa employment, has ref 1; |
    And executing query 2
      | match $r isa employment; insert $r (employer: $c) isa employment; $c isa company, has ref 2; |
    And the KB is valid
    And executing query 3
      | match $r (employer: $c, employee: $p) isa employment; get; |

    Then query 1 has 1 answer
    And query 2 has 1 answer
    And query 3 has 1 answer

    And variables ($p, $r) from query 1 have ref in
      | 0, 1 |
    And variables ($r, $c) from query 2 have ref in
      | 1, 2 |
    And variables ($r, $p, $c) from query 3 have ref in
      | 1, 0, 2 |




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


# ----- MATCH theorems ------

  Scenario: Disjunctions return the union of composing query statements
    Given the schema
      | define                                                        |
      | person sub entity, has name, key email;                       |
      | company sub entity, has name;                                 |
      | name sub attribute, datatype string;                          |
      | email sub attribute, datatype string;                         |
    And the KB is valid
    And the data
      | insert $x isa person, has name "John", has email "abc@xyq.com"; |
      | $c isa company, has name "Grakn";                             |

    When executing query 1
      | match $x isa thing; $a isa attribute;                         |
      | {$x isa person, has name $a;} or {$x isa person, has email $a} or {$x isa company, has name $a;}; get; |

    Then query 1 has 3 answers
    And answers from query 1 satisfy
      | match $x isa thing, has attribute $a; get;                    |




    When executing query 1
      | match $x isa person, has name $a; get; |
    And executing query 2
      | match $x isa person, has email $a; get; |
    And executing query 3
      | match $x isa company, has name $a; get; |

    Then query 1 has 1 answers
    And query 2 has 1 answers
    And query 3 has 1 answers
    And answers from query 1 satisfy
      | match $x isa thing; $a isa attribute;                         |
      | {$x isa person, has name $a;} or {$x isa person, has email $a} or {$x isa company, has name $a;}; get; |
    And answers from query 2 satisfy
      | match $x isa thing; $a isa attribute;                         |
      | {$x isa person, has name $a;} or {$x isa person, has email $a} or {$x isa company, has name $a;}; get; |
    And answers from query 3 satisfy
      | match $x isa thing; $a isa attribute;                         |
      | {$x isa person, has name $a;} or {$x isa person, has email $a} or {$x isa company, has name $a;}; get; |





    When executing query 1
      | match $x isa person, has name $a; get; |
    And executing query 2
      | match $x isa person, has email $a; get; |
    And executing query 3
      | match $x isa company, has name $a; get; |

    And executing query 4
      | match $x isa thing; $a isa attribute;                         |
      | {$x isa person, has name $a;} or {$x isa person, has email $a} or {$x isa company, has name $a;}; get; |

    And executing query 5
      | match $x isa thing, has attribute $a; get;                    |

    Then query 4 is equivalent to query 5 using variables
    And query 5 is equivalent to union(query 1, query 2, query 3)
    And query 4 is equivalent to union(query 1, query 2, query 3)

      Implies

