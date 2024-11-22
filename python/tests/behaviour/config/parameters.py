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

import re
from enum import Enum
from typing import Callable, Optional

import parse
from behave import register_type
from behave.model import Table
from hamcrest import *
from typedb.api.answer.query_type import QueryType
from typedb.api.connection.transaction import TransactionType
from typedb.common.exception import TypeDBDriverException
from typedb.driver import *


@parse.with_pattern(r"true|false")
def parse_bool(value: str) -> bool:
    if value == "true":
        return True
    elif value == "false":
        return False
    else:
        raise ValueError(f"Unrecognised bool: {value}")


register_type(Bool=parse_bool)


def parse_int(text: str) -> int:
    return int(text)


register_type(Int=parse_int)


@parse.with_pattern("[\w_-]+")
def parse_words(text):
    return text


register_type(Words=parse_words)


@parse.with_pattern("[^;]+")
def parse_non_semicolon(text):
    return text


register_type(NonSemicolon=parse_non_semicolon)


class ConceptKind(Enum):
    CONCEPT = 0,
    TYPE = 1,
    INSTANCE = 2,
    ENTITY_TYPE = 3,
    RELATION_TYPE = 4,
    ATTRIBUTE_TYPE = 5,
    ROLE_TYPE = 6,
    ENTITY = 7,
    RELATION = 8,
    ATTRIBUTE = 9,
    VALUE = 10,


@parse.with_pattern(
    r"concept|variable|type|instance|entity type|relation type|attribute type|role type|entity|relation|attribute|value")
def parse_concept_kind(text: str) -> ConceptKind:
    if text == "concept" or text == "variable":
        return ConceptKind.CONCEPT
    elif text == "type":
        return ConceptKind.TYPE
    elif text == "instance":
        return ConceptKind.INSTANCE
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
        raise ValueError(f"Unrecognised ConceptKind: {text}")


register_type(ConceptKind=parse_concept_kind)


class ValueType(Enum):
    BOOLEAN = 0,
    LONG = 1,
    DOUBLE = 2,
    DECIMAL = 3,
    STRING = 4,
    DATE = 5,
    DATETIME = 6,
    DATETIME_TZ = 7,
    DURATION = 8,
    STRUCT = 9,


@parse.with_pattern(r"boolean|long|double|decimal|string|date|datetime|datetime-tz|duration|struct")
def parse_value_type(text: str) -> ValueType:
    value_type_opt = try_parse_value_type(text)
    if value_type_opt is None:
        raise ValueError(f"Unrecognised ValueType: {text}")
    return value_type_opt


def try_parse_value_type(text: str) -> Optional[ValueType]:
    if text == "boolean":
        return ValueType.BOOLEAN
    elif text == "long":
        return ValueType.LONG
    elif text == "double":
        return ValueType.DOUBLE
    elif text == "decimal":
        return ValueType.DECIMAL
    elif text == "string":
        return ValueType.STRING
    elif text == "date":
        return ValueType.DATE
    elif text == "datetime":
        return ValueType.DATETIME
    elif text == "datetime-tz":
        return ValueType.DATETIME_TZ
    elif text == "duration":
        return ValueType.DURATION
    elif text == "struct":
        return ValueType.STRUCT
    else:
        return None


register_type(ValueType=parse_value_type)


@parse.with_pattern(r"([a-zA-Z0-9]*)")
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

    def __init__(self, may_error: bool, message: str = ""):
        self.may_error = may_error
        self.message = message

    def check(self, func: Callable, exception=TypeDBDriverException):
        if self.may_error:
            assert_that(func, raises(exception, self.message))
        else:
            func()

    def __repr__(self):
        return f"MayError({self.may_error})"


@parse.with_pattern("|; fails|; parsing fails|; fails with a message containing: \"(?P<message>.*)\"")
def parse_may_error(value: str) -> MayError:
    if value == "":
        return MayError(False)
    elif value in ("; fails", "; parsing fails"):
        return MayError(True)
    else:
        match = re.match(r'; fails with a message containing: "(?P<message>.*)"', value)
        if match:
            return MayError(True, match.group("message"))
        else:
            raise ValueError(f"Unrecognised MayError: {value}")


register_type(MayError=parse_may_error)


@parse.with_pattern("is|is not")
def parse_is_or_not(value: str) -> bool:
    if value == "is not":
        return False
    elif value == "is":
        return True
    else:
        raise ValueError(f"Unrecognised IsOrNot: {value}")


register_type(IsOrNot=parse_is_or_not)


@parse.with_pattern("contains|does not contain")
def parse_contains_or_doesnt(value: str) -> bool:
    if value == "contains":
        return True
    elif value == "does not contain":
        return False
    else:
        raise ValueError(f"Unrecognised ContainsOrDoesnt: {value}")


register_type(ContainsOrDoesnt=parse_contains_or_doesnt)


def is_or_not_reason(is_or_not: bool, real, expected) -> str:
    intro_str = "Expected that real value"
    is_or_not_str = "is" if is_or_not else "is not"
    spaces_num = len(intro_str)
    return f"\nExpected that real value <{real}>\n{is_or_not_str: >{spaces_num}} <{expected}>"


def is_none_or_not_reason(is_none_or_not: bool, real) -> str:
    intro_str = "Expected that real value"
    is_or_not_str = "is none" if is_none_or_not else "is not none"
    spaces_num = len(intro_str)
    return f"\nExpected that real value <{real}>\n{is_or_not_str: >{spaces_num}}"


def check_is_none(value, is_none_or_not: bool):
    assert_that(value, is_(None) if is_none_or_not else is_not(None), is_none_or_not_reason(is_none_or_not, real=value))


@parse.with_pattern("| by index of variable")
def parse_by_index_of_variable_or_not(value: str) -> bool:
    return value == " by index of variable"


register_type(IsByVarIndex=parse_by_index_of_variable_or_not)
