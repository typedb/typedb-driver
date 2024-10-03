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
from typing import Callable

from typedb.common.datetime import Datetime
from typedb.common.duration import Duration
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


class Kind(Enum):
    ENTITY = 0,
    ATTRIBUTE = 1,
    RELATION = 2


@parse.with_pattern(r"entity|attribute|relation")
def parse_kind(text: str) -> Kind:
    if text == "entity":
        return Kind.ENTITY
    elif text == "attribute":
        return Kind.ATTRIBUTE
    elif text == "relation":
        return Kind.RELATION
    else:
        raise ValueError("Unrecognised kind: " + text)


register_type(Kind=parse_kind)


@parse.with_pattern(r"[a-zA-Z0-9-_]+:([a-zA-Z0-9-_]+)?")
def parse_label(text: str):
    return Label.of(*text.split(":"))


register_type(Label=parse_label)


@parse.with_pattern(r"\$([a-zA-Z0-9]+)")
def parse_var(text: str):
    return text


register_type(Var=parse_var)


@parse.with_pattern("read|write|schema")
def parse_transaction_type(value: str) -> TransactionType:
    return TransactionType.READ if value == "read" \
        else TransactionType.WRITE if value == "write" \
        else TransactionType.SCHEMA


register_type(TransactionType=parse_transaction_type)


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


@parse.with_pattern("; fails|; parsing fails|")
def parse_may_error(value: str) -> MayError:
    if value == "":
        return MayError(False)
    elif value in ("; fails", "; parsing fails"):
        return MayError(True)
    else:
        raise ValueError("Unrecognised MayError: " + value)


register_type(MayError=parse_may_error)
