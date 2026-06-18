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

from collections.abc import Mapping as ABCMapping
from math import floor
from datetime import date, datetime, timezone
from decimal import Decimal
from typing import Any, Dict, Iterable, List, Mapping, Optional, Union

from typedb.api.answer.concept_document_iterator import *  # noqa # pylint: disable=unused-import
from typedb.api.answer.concept_row import *  # noqa # pylint: disable=unused-import
from typedb.api.answer.concept_row_iterator import *  # noqa # pylint: disable=unused-import
from typedb.api.answer.query_answer import *  # noqa # pylint: disable=unused-import
from typedb.api.answer.query_type import *  # noqa # pylint: disable=unused-import
from typedb.api.concept.concept import *  # noqa # pylint: disable=unused-import
from typedb.api.concept.given_rows import GivenRows
from typedb.api.concept.instance.attribute import *  # noqa # pylint: disable=unused-import
from typedb.api.concept.instance.entity import *  # noqa # pylint: disable=unused-import
from typedb.api.concept.instance.instance import *  # noqa # pylint: disable=unused-import
from typedb.api.concept.instance.relation import *  # noqa # pylint: disable=unused-import
from typedb.api.concept.type.attribute_type import *  # noqa # pylint: disable=unused-import
from typedb.api.concept.type.entity_type import *  # noqa # pylint: disable=unused-import
from typedb.api.concept.type.relation_type import *  # noqa # pylint: disable=unused-import
from typedb.api.concept.type.role_type import *  # noqa # pylint: disable=unused-import
from typedb.api.concept.type.type import *  # noqa # pylint: disable=unused-import
from typedb.api.concept.value import *  # noqa # pylint: disable=unused-import
from typedb.api.connection.server_routing import *  # noqa # pylint: disable=unused-import
from typedb.api.connection.credentials import *  # noqa # pylint: disable=unused-import
from typedb.api.connection.driver import *  # noqa # pylint: disable=unused-import
from typedb.api.connection.driver_options import *  # noqa # pylint: disable=unused-import
from typedb.api.connection.driver_tls_config import *  # noqa # pylint: disable=unused-import
from typedb.api.connection.query_options import *  # noqa # pylint: disable=unused-import
from typedb.api.connection.transaction import *  # noqa # pylint: disable=unused-import
from typedb.api.connection.transaction_options import *  # noqa # pylint: disable=unused-import
from typedb.api.database.database import *  # noqa # pylint: disable=unused-import
from typedb.api.database.database_manager import *  # noqa # pylint: disable=unused-import
from typedb.api.user.user import *  # noqa # pylint: disable=unused-import
from typedb.api.user.user_manager import *  # noqa # pylint: disable=unused-import
from typedb.common.datetime import *  # noqa # pylint: disable=unused-import
from typedb.common.duration import *  # noqa # pylint: disable=unused-import
from typedb.common.exception import *  # noqa # pylint: disable=unused-import
from typedb.concept.concept import _Concept
from typedb.concept.concept_factory import wrap_value
from typedb.concept.given_rows import _GivenRows
from typedb.connection.driver import _Driver
from typedb.native_driver_wrapper import (
    concept_new_boolean, concept_new_integer, concept_new_double, concept_new_decimal,
    concept_new_string, concept_new_date_from_seconds, concept_new_datetime,
    concept_new_datetime_tz_iana, concept_new_datetime_tz_offset, concept_new_duration,
    given_rows_header_builder_new, given_rows_header_builder_push, given_rows_header_builder_finish,
    given_rows_builder_new, given_rows_builder_start_new_row, given_rows_builder_commit_row,
    given_rows_builder_finish,
    given_rows_builder_set_index_to_concept, given_rows_builder_set_index_to_empty,
    given_rows_builder_set_variable_to_concept, given_rows_builder_set_variable_to_empty,
)


# Repackaging these symbols allows them to be imported from "typedb.driver"


class TypeDB:
    DEFAULT_ADDRESS = "127.0.0.1:1729"
    ADDRESSES = Union[Mapping[str, str], Iterable[str], str]

    @staticmethod
    def driver(addresses: ADDRESSES, credentials: Credentials, driver_options: DriverOptions) -> Driver:
        """
        Creates a connection to TypeDB.

        :param addresses: Address(-es) of the TypeDB server(-s).
        Can be a single string, multiple strings, or multiple pairs of strings.
        :param credentials: The credentials to connect with.
        :param driver_options: The driver connection options to connect with.
        """
        if isinstance(addresses, str):
            return _Driver(addresses, credentials, driver_options)
        elif isinstance(addresses, ABCMapping):
            return _Driver(dict(addresses), credentials, driver_options)
        else:
            return _Driver(list(addresses), credentials, driver_options)

    class Concept:
        """Factory class to instantiate new ``Value`` concepts and build ``GivenRows`` for use as query inputs."""

        @staticmethod
        def build_given_rows_from(variables: List[str], rows: List[List[Optional["Concept"]]]) -> GivenRows:
            """Constructs a ``GivenRows`` instance for use as inputs to queries.

            :param variables: The variables describing the columns of the rows.
            :param rows: Input rows; each inner list is one row indexed by variables, ``None`` entries represent empty variables.
            """
            header_builder = given_rows_header_builder_new(len(variables))
            for variable in variables:
                given_rows_header_builder_push(header_builder, variable)
            header_builder.thisown = 0
            header = given_rows_header_builder_finish(header_builder)
            rows_builder = given_rows_builder_new(header, len(rows))
            for row in rows:
                given_rows_builder_start_new_row(rows_builder)
                for i, concept in enumerate(row):
                    if concept is None:
                        given_rows_builder_set_index_to_empty(rows_builder, i)
                    else:
                        given_rows_builder_set_index_to_concept(rows_builder, i, concept._native_object)
                given_rows_builder_commit_row(rows_builder)
            rows_builder.thisown = 0
            return _GivenRows(given_rows_builder_finish(rows_builder))

        @staticmethod
        def build_given_rows_from_map(rows) -> GivenRows:
            """Constructs a ``GivenRows`` instance from dict-based rows for use as inputs to queries.

            :param rows: A list of input rows. Each row is a dict mapping a variable name to a concept (or ``None`` for empty).
            """
            all_variables: set = set()
            for row in rows:
                all_variables.update(row.keys())
            variables = list(all_variables)
            header_builder = given_rows_header_builder_new(len(variables))
            for variable in variables:
                given_rows_header_builder_push(header_builder, variable)
            header_builder.thisown = 0
            header = given_rows_header_builder_finish(header_builder)
            rows_builder = given_rows_builder_new(header, len(rows))
            for row in rows:
                given_rows_builder_start_new_row(rows_builder)
                for variable, concept in row.items():
                    if concept is None:
                        given_rows_builder_set_variable_to_empty(rows_builder, variable)
                    else:
                        given_rows_builder_set_variable_to_concept(rows_builder, variable, concept._native_object)
                given_rows_builder_commit_row(rows_builder)
            rows_builder.thisown = 0
            return _GivenRows(given_rows_builder_finish(rows_builder))

        @staticmethod
        def build_given_rows_from_objects(rows: List[Dict[str, Any]]) -> GivenRows:
            """Constructs a ``GivenRows`` instance from dict rows containing raw Python values or concepts.

            Values that are already ``Concept`` instances are used directly. Other values are converted
            via :meth:`try_convert_to_value`. ``None`` values represent empty variables.

            :param rows: A list of input rows; each row maps variable names to concepts or raw values.
            :raises TypeError: if a value's type cannot be converted to a ``Value``.
            """
            from typedb.api.concept.concept import Concept as _ConceptApi
            converted = [
                {
                    var: (None if val is None else (val if isinstance(val, _ConceptApi) else TypeDB.Concept.try_convert_to_value(val)))
                    for var, val in row.items()
                }
                for row in rows
            ]
            return TypeDB.Concept.build_given_rows_from_map(converted)

        @staticmethod
        def try_convert_to_value(value: Any):
            """Converts a raw Python value to a ``Value`` concept.

            Accepted types: ``bool``, ``int``, ``float``, ``Decimal``, ``str``, ``date``, ``Datetime``, ``Duration``.

            :raises TypeDBDriverException: if the type is not supported.
            """
            if isinstance(value, bool):  # must precede int: bool is a subclass of int
                return TypeDB.Concept.new_boolean(value)
            if isinstance(value, int):
                return TypeDB.Concept.new_integer(value)
            if isinstance(value, float):
                return TypeDB.Concept.new_double(value)
            if isinstance(value, Decimal):
                return TypeDB.Concept.new_decimal(value)
            if isinstance(value, str):
                return TypeDB.Concept.new_string(value)
            if isinstance(value, date) and not isinstance(value, datetime):
                return TypeDB.Concept.new_date(value)
            if isinstance(value, Datetime):
                if value.tz_name is not None or value.offset_seconds is not None:
                    return TypeDB.Concept.new_datetime_tz(value)
                return TypeDB.Concept.new_datetime(value)
            if isinstance(value, Duration):
                return TypeDB.Concept.new_duration(value)
            raise TypeDBDriverException(UNSUPPORTED_VALUE_CONVERSION, type(value).__name__)

        @staticmethod
        def new_boolean(value: bool):
            """Creates a new ``Value`` wrapping the specified ``bool`` value."""
            return wrap_value(concept_new_boolean(value))

        @staticmethod
        def new_integer(value: int):
            """Creates a new ``Value`` wrapping the specified ``int`` value."""
            return wrap_value(concept_new_integer(value))

        @staticmethod
        def new_double(value: float):
            """Creates a new ``Value`` wrapping the specified ``float`` value."""
            return wrap_value(concept_new_double(value))

        @staticmethod
        def new_decimal(value: Decimal):
            """Creates a new ``Value`` wrapping the specified ``Decimal`` value."""
            integer_part = floor(value)
            fractional_part = int((value - Decimal(integer_part)) * Decimal(10 ** _Concept.DECIMAL_SCALE))
            return wrap_value(concept_new_decimal(integer_part, fractional_part))

        @staticmethod
        def new_string(value: str):
            """Creates a new ``Value`` wrapping the specified ``str`` value."""
            return wrap_value(concept_new_string(value))

        @staticmethod
        def new_date(value: date):
            """Creates a new ``Value`` wrapping the specified ``date`` value."""
            epoch_seconds = int(datetime(value.year, value.month, value.day, tzinfo=timezone.utc).timestamp())
            return wrap_value(concept_new_date_from_seconds(epoch_seconds))

        @staticmethod
        def new_datetime(value: Datetime):
            """Creates a new ``Value`` wrapping the specified naive ``Datetime`` value."""
            seconds, nanos = value._to_seconds_and_nanos()
            return wrap_value(concept_new_datetime(seconds, nanos))

        @staticmethod
        def new_datetime_tz(value: Datetime):
            """Creates a new ``Value`` wrapping the specified timezone-aware ``Datetime`` value."""
            seconds, nanos = value._to_seconds_and_nanos()
            if value.tz_name is not None:
                return wrap_value(concept_new_datetime_tz_iana(seconds, nanos, value.tz_name))
            else:
                return wrap_value(concept_new_datetime_tz_offset(seconds, nanos, value.offset_seconds))

        @staticmethod
        def new_duration(value: Duration):
            """Creates a new ``Value`` wrapping the specified ``Duration`` value."""
            return wrap_value(concept_new_duration(value.months, value.days, value.nanos))
