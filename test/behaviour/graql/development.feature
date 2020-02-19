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

Feature: Theorems over Match Clauses

  Background:
    Given connection has been opened
    Given connection delete all keyspaces
    Given connection open 1 session for one keyspace: match
    Given session open 1 transaction of type: write
    Given load file schema.gql

  Scenario:
    Given session open 1 transaction of type: read
    When match $x sub $y; $y sub $z; get;
    Then answers satisfy match $x sub $x; get;


Feature: Graql Axioms

  Scenario: Transitive Sub
    Given "define person sub entity; child sub person;"
    Given Q1 "match $x sub $y; $y sub entity; get;" with 6 answers

    Given Q2 "match $x type entity; $y type child; get;" with 1 answer

    Given Q3 "match $x type entity; $y type child; $y sub $x; get;" with 1 answer

    Given Q4 "match $x type entity; $y sub child; get;" with 1 answer

    Given Q5 "match $x type entity; $y sub $x; get;" with 3 answers



    Then Q1 overlap Q2 on x,y with 1 answer
    Then Q1 overlap Q3 on x,y with 1 answer
    Then Q1 overlap Q4 on x,y with 1 answer
    Then Q1 overlap Q5 on x,y with 3 answers






    Then query overlap answers on x "match $x type ..." with 2 answers



    Given "match $x type entity; $y type child; get;"
    Given "match $x type entity; $y type child; $y sub $x; get;"

    Given "match $x sub $y; $y sub entity; get;"

    Given "match $x type entity; $y type entity; get;"
    Given "match $x type child; $y type child; get;"
    Given "match $x type entity; $y type child; get;"
    Given "match $x type thing; $y type child; get;"
    Given "match $x type thing; $y type entity; get;"

    Assert the last match has an answer contained in the first match





  Scenario: Transitive Sub
    Given "define person sub entity; child sub person;"
    Given Q1 "match $x sub entity; $y sub $x; get;"
    Given Q2 "match $x type entity; $y type child; get;"
    Given Q3 "match $x type entity; $y type child; $y sub $x; get;"

    Entity, Entity
    Entity, Person
    Entity, Child
    Person, Person
    Person, Child
    Child,  Child




    Assert size(Q1 intersect Q2) = 1
    Assert size(Q1 intersect Q3) = 1


  Scenario: Transitive Sub
    Given "define person sub entity; child sub person;"
    Given Q1 "match $x type entity; $y type child; $y sub $x; get;"

    Assert Q1 has 1 answer


  Scenario: Transitive Schema Has
    Given "define person sub entity, has name; child sub person; name sub attribute, datatype string;"
    Given "match $x type child, has name; get;"
    Given "match $x has name; get;"

    Assert that the first match has 1 answer, where $x has the label child
    Assert the second match has 1 answer and $x has label person or $x has label child

    There an answer to the second match containing $x having the label child


  Scenario: Transitive Schema Has
    Given "define person sub entity, has name; child sub person; name sub attribute, datatype string;"
    Given Q1 "match $x type child, has name; get;" with 1 answer
    Given Q2 "match $x has name; get;" with 2 answers

    Then Q1 overlap Q2 on x with 1 answer

  Scenario: Transitive Schema Has
    Given "define person sub entity, has name; child sub person; name sub attribute, datatype string;"
    Given "match $x1 type child; get;"
    Given "match $x2 type parent; get;"
    Given "match $x3 has name; get;"

    x3 contains only x1, x2


  Scenario: Key Errors when Reused
    Given "define person sub entity, key email; email sub attribute, datatype string;"
    Given "insert $x isa person, has email 'johndoe' ;"
    Then Error "insert $y isa person, has email 'johndoe' ;"

  Scenario: Keys Subset of Has
    Given "define person sub entity, key email; email sub attribute, datatype string;"
    Given "insert $x isa person, has email 'johndoe' ;"
    Given "match $x isa person, has email $a; get;"
    Given "match $x isa person, key email $a; get;"

    Assert that the last match is a subset of the first match

  Scenario: Attributes are deduplicated and shared
    Given "define person sub entity, has name; name sub attribute, datatype string; "
    Given Q1 "insert $x isa person, has name $n; $n 'John';" with 1 answer
    Given Q2 "insert $y isa person, has name $n; $n 'John';" with 1 answer
    Given Q3 "match $x isa person, has name $n; $y isa person, has name $n; $x != $y; get;" with 1 answer

    Then Q1 overlap Q2 on n with 1 answer
    Then Q3 overlap Q1 on x,n with 1 answer
    Then Q3 overlap Q2 on y,n with 1 answer

    Assert that $n in both inserts is identical
    Assert that the final $x, $y and $n are the same as the first insert statements


  Scenario: Piecewise Schema Construction
    Given "define employment sub relation, relates employee;"
    Given "define employment sub relation, relates employer;"
    Given "match $x relates employee, relates employer;"

    Assert that the label of x is employment, and is exactly one answer



  Scenario: Piecewise Relation construction

    Given "define
      employment sub relation, relates employee, relates employer;
      person sub entity, plays employee;
      company sub entity, plays employer;"

    Given Q1 "insert $x isa person; $r (employee: $x) isa employment;" with 1 answer
       [ {x: V1, r: V2 ]
    Given Q2 "match $r isa employment; insert $y isa company; $r (employer: $y) isa employment; get;" with 1 answer
      [ {r: V2, y: V3} ]
    Given Q3 "match $r (employee: $x, employer: $y); get;" with 1 answer
      [ {r : V2, y: V3, x: V1} ]

    Then Q1 overlap Q3 on x,r with 1 answer
    Then Q2 overlap Q3 on y,r with 1 answer



    Then assert that $r, $x is the same in both Q1 and Q3 with 1 answer
    Assert that the Q3 has exactly one answer and $x and $y match prior insert/match-insert



  Scenario: Deleting all role players of a relation errors (or deletes relation??)

  Scenario: Delete a Role Playing Entity

    Given "define
      employment sub relation, relates employee, relates employer;
      person sub entity, plays employee;
      company sub entity, plays employer;"

    Given Q1 "insert $p isa person; $c isa company; $r (employee: $p, employer: $c) isa employment;" with 1 answer

    Given "match $p isa person; delete $p;"

    Then Q2 "match $p isa person; get;" has 0 answers
    Then Q3 "match $r (employee: $p, employer: $c) isa employment; get;" with 0 answers
    Then Q4 "match $r "

    Then assert KG is valid




    Then Q2 "match $r ($p, $c) isa employment; get;" with 0 answers
    Then Q3 "match $r ($c) isa employment; get;" with 1 answer
    Then "match $r isa employment; get;" with 1 answer
    Then "match $r ($c) isa employment; $p isa company; get;" with 1 answer
    Then "match $r (employer: $c) isa employment; $p isa company; get;" with 1 answer
    Then "match (employer: $c) isa employment; $p isa company; get;" with 1 answer

    Then Q3 overlap Q1 on r,c with 1 answer



  Then asseert KG is valid


  Scenario: ISA is unique

    Given "define
      employment sub relation, relates employee, relates employer;
      person sub entity, plays employee;
      company sub entity, plays employer;"

    Given Q1 "insert $p isa person; $c isa company; $r (employee: $p, employer: $c) isa employment;" with 1 answer

    Given Q2 "match $p isa person; $p isa $t; get;"

    Given Q3 "match $p isa thing; get;"
    Given Q4 "match $t isa type; get;"

    Given Q5 "match $p isa thing; $t isa type; get;"

    Assert for each p in Q3, for each t in Q4 exactly one (p isa t) isa true







