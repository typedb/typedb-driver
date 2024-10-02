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

from typedb.common.datetime import NANOS_IN_SECOND

DAYS_IN_WEEK = 7
MONTHS_IN_YEAR = 12
SECONDS_IN_MINUTE = 60
MINUTES_IN_HOUR = 60


class Duration:
    """
    A relative duration, which contains months, days, and nanoseconds.
    Can be used for calendar-relative durations (eg 7 days forward), or for absolute durations using the nanosecond
    component.
    Not convertible to datetime.timedelta due to the lack of months and nanos alternatives.
    """

    def __init__(self, months: int, days: int, nanos: int):
        self._months = months
        self._days = days
        self._nanos = nanos

    @property
    def months(self) -> int:
        """ The months part of the duration """
        return self._months

    @property
    def days(self) -> int:
        """ The days part of the duration """
        return self._days

    @property
    def nanos(self) -> int:
        """ The nanoseconds part of the duration """
        return self._nanos

    @classmethod
    def from_string(cls, duration_str: str) -> Duration:
        """
        Parses a Duration object from a string in ISO 8601 format.

        :param duration_str: A string representation of the duration. Expected format: PnYnMnDTnHnMnS / PnW
        :return: A Duration object.
        :raises ValueError: If duration_str is of an incorrect format.

        Examples
        --------

        ::

            Duration.from_string("P1Y10M7DT15H44M5.00394892S")
            Duration.from_string("P55W")
        """

        match = cls.DATETIME_REGEX.fullmatch(duration_str)
        if match:
            years = int(match.group(1) or 0)
            months = int(match.group(2) or 0)
            days = int(match.group(3) or 0)
            hours = int(match.group(4) or 0)
            minutes = int(match.group(5) or 0)
            seconds = int(match.group(6) or 0.0)
            nanos = int((match.group(7) or '0').ljust(9, '0')[:9])

            months += years * MONTHS_IN_YEAR
            minutes += hours * MINUTES_IN_HOUR
            seconds += minutes * SECONDS_IN_MINUTE
            nanos += seconds * NANOS_IN_SECOND
            return cls(months, days, nanos)

        match = cls.WEEK_REGEX.fullmatch(duration_str)
        if match:
            weeks = int(match.group(1) or 0)
            return cls(0, weeks * DAYS_IN_WEEK, 0)

        raise ValueError(f"Incorrect format for duration string: '{duration_str}'")

    def __str__(self):
        return f"months: {self.months}, days: {self.days}, nanos: {self.nanos}"

    def __repr__(self):
        return f"Duration('{str(self)}')"

    def __hash__(self):
        return hash((self.months, self.days, self.nanos))

    def __eq__(self, other):
        if other is self:
            return True
        if other is None or not isinstance(other, self.__class__):
            return False
        return self.months == other.months and self.days == other.days and self.nanos == other.nanos

    DATETIME_REGEX = re.compile(r'''
        P                              # Literal 'P'
        (?:(\d+)Y)?                    # Years
        (?:(\d+)M)?                    # Months
        (?:(\d+)D)?                    # Days
        T                              # Literal 'T'
        (?:(\d+)H)?                    # Hours
        (?:(\d+)M)?                    # Minutes
        (?:(\d+)(?:\.(\d+))?S)?        # Seconds
    ''', re.VERBOSE)

    WEEK_REGEX = re.compile(r'''
        P                              # Literal 'P'
        (?:(\d+)W)                     # Weeks
    ''', re.VERBOSE)
