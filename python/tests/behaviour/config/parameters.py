# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

from __future__ import annotations


from behave import register_type
from behave.model import Table
from enum import Enum
from datetime import date, datetime
from decimal import Decimal
from hamcrest import *
import parse
from typing import Callable, Optional

from typedb.api.answer.query_type import QueryType
from typedb.api.connection.transaction import TransactionType
from typedb.common.datetime import Datetime
from typedb.common.duration import Duration
from typedb.common.exception import TypeDBDriverException
from typedb.driver import *


@parse.with_pattern(r"true|false")
def parse_bool(value: str) -> bool:
    return value == "true"


register_type(Bool=parse_bool)


def parse_int(text: str) -> int:
    return int(text)


register_type(Int=parse_int)


def parse_float(text: str) -> float:
    return float(text)


register_type(Float=parse_float)


def parse_decimal(text: str) -> Decimal:
    return Decimal(text)


register_type(Decimal=parse_decimal)


@parse.with_pattern("[\w_-]+")
def parse_words(text):
    return text


register_type(Words=parse_words)


def parse_date(text: str) -> date:
    datetime.strptime(text, "%Y-%m-%d").date()
    return Datetime.from_string(text)


register_type(Date=parse_date)


def parse_datetime(text: str) -> Datetime:
    return Datetime.from_string(text)


register_type(Datetime=parse_datetime)


def parse_datetime_tz(text: str) -> Datetime:
    expected_dt, expected_tz = text.split(" ")
    return Datetime.from_string(expected_dt, expected_tz)


register_type(DatetimeTZ=parse_datetime_tz)


def parse_duration(text: str) -> Duration:
    return Duration.from_string(text)


register_type(Duration=parse_duration)


class ConceptKind(Enum):
    CONCEPT = 0,
    TYPE = 1,
    THING_TYPE = 2,
    THING = 3,
    ENTITY_TYPE = 4,
    RELATION_TYPE = 5,
    ATTRIBUTE_TYPE = 6,
    ROLE_TYPE = 7,
    ENTITY = 8,
    RELATION = 9,
    ATTRIBUTE = 10,
    VALUE = 11,


@parse.with_pattern(r"concept|variable|type|thing type|thing|entity type|relation type|attribute type|role type|entity|relation|attribute|value")
def parse_concept_kind(text: str) -> ConceptKind:
    if text == "concept" or text == "variable":
        return ConceptKind.CONCEPT
    elif text == "type":
        return ConceptKind.TYPE
    elif text == "thing type":
        return ConceptKind.THING_TYPE
    elif text == "thing":
        return ConceptKind.THING
    elif text == "entity type":
        return ConceptKind.ENTITY_TYPE
    elif text == "relation type":
        return ConceptKind.RELATION_TYPE
    elif text == "attribute type":
        return ConceptKind.ATTRIBUTE_TYPE
    elif text == "role type":
        return ConceptKind.ROLE_TYPE
    elif text == "entity":
        return ConceptKind.ENTITY
    elif text == "relation":
        return ConceptKind.RELATION
    elif text == "attribute":
        return ConceptKind.ATTRIBUTE
    elif text == "value":
        return ConceptKind.VALUE
    else:
        raise ValueError("Unrecognised ConceptKind: " + text)


register_type(ConceptKind=parse_concept_kind)


class PredefinedValueType(Enum):
    BOOLEAN = 0,
    LONG = 1,
    DOUBLE = 2,
    DECIMAL = 3,
    STRING = 4,
    DATE = 5,
    DATETIME = 6,
    DATETIME_TZ = 7,
    DURATION = 8,


@parse.with_pattern(r"boolean|long|double|decimal|string|date|datetime|datetime_tz|duration")
def parse_predefined_value_type_opt(text: str) -> Optional[PredefinedValueType]:
    if text == "boolean":
        return PredefinedValueType.BOOLEAN
    elif text == "long":
        return PredefinedValueType.LONG
    elif text == "double":
        return PredefinedValueType.DOUBLE
    elif text == "decimal":
        return PredefinedValueType.DECIMAL
    elif text == "string":
        return PredefinedValueType.STRING
    elif text == "date":
        return PredefinedValueType.DATE
    elif text == "datetime":
        return PredefinedValueType.DATETIME
    elif text == "datetime-tz":
        return PredefinedValueType.DATETIME_TZ
    elif text == "duration":
        return PredefinedValueType.DURATION
    else:
        raise ValueError("Unrecognised PredefinedValueType: " + text)


@parse.with_pattern(r"([a-zA-Z0-9]+)")
def parse_var(text: str):
    return text


register_type(Var=parse_var)


@parse.with_pattern("read|write|schema")
def parse_transaction_type(value: str) -> TransactionType:
    return TransactionType.READ if value == "read" \
        else TransactionType.WRITE if value == "write" \
        else TransactionType.SCHEMA


register_type(TransactionType=parse_transaction_type)


@parse.with_pattern("read|write|schema")
def parse_query_type(value: str) -> QueryType:
    return QueryType.READ if value == "read" \
        else QueryType.WRITE if value == "write" \
        else QueryType.SCHEMA


register_type(QueryType=parse_query_type)


def parse_list(table: Table) -> list[str]:
    return [table.headings[0]] + list(map(lambda row: row[0], table.rows))


def parse_dict(table: Table) -> dict[str, str]:
    result = {table.headings[0]: table.headings[1]}
    for row in table.rows:
        result[row[0]] = row[1]
    return result


def parse_table(table: Table) -> list[list[tuple[str, str]]]:
    """
    Extracts the rows of a Table as lists of Tuples, where each tuple contains the column header and the cell value.

    For example, the table::

        | x         | type         |
        | key:ref:0 | label:person |
        | key:ref:2 | label:dog    |

    is converted to::

        [
            [('x', 'key:ref:0'), ('type', 'label:person')],
            [('x', 'key:ref:2'), ('type', 'label:dog')]
        ]
    """
    return [[(table.headings[idx], row[idx]) for idx in range(len(row))] for row in table.rows]


class MayError:

    def __init__(self, may_error: bool):
        self.may_error = may_error

    def check(self, func: Callable):
        if self.may_error:
            assert_that(func, raises(TypeDBDriverException))
        else:
            func()

    def __repr__(self):
        return f"MayError({self.may_error})"


@parse.with_pattern("; fails|; parsing fails|")
def parse_may_error(value: str) -> MayError:
    if value == "":
        return MayError(False)
    elif value in ("; fails", "; parsing fails"):
        return MayError(True)
    else:
        raise ValueError("Unrecognised MayError: " + value)


register_type(MayError=parse_may_error)


@parse.with_pattern("is|is not")
def parse_is_or_not(value: str) -> bool:
    return value == "is"


register_type(IsOrNot=parse_is_or_not)


@parse.with_pattern("| by index of variable")
def parse_by_index_of_variable_or_not(value: str) -> bool:
    return value == " by index of variable"


register_type(ByIndexOfVarOrNot=parse_by_index_of_variable_or_not)
