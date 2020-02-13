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
