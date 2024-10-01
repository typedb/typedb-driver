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
from datetime import date, datetime, tzinfo

NANOS_IN_SECOND = 1_000_000_000
MICROS_IN_NANO = 1000


class Datetime:
    """
    An extension class for ``datetime.datetime`` class to store additional information about nanoseconds.
    It is split to a timestamp (time zoned or not) based on the number of full seconds and a nanoseconds part.
    """

    def __init__(self, sec_dt: datetime, subsec_nanos: int):
        self._datetime_of_seconds = sec_dt
        self._nanos = subsec_nanos

    @property
    def datetime_of_seconds(self) -> datetime:
        """Return the original datetime.datetime part, containing data up to seconds."""
        return self._datetime_of_seconds

    @property
    def total_seconds(self) -> float:
        """Return the total number of seconds including the nanoseconds part as a float."""
        return self._datetime_of_seconds.timestamp() + self._nanos / NANOS_IN_SECOND

    @property
    def year(self) -> int:
        """Return the datetime's year (1-9999)."""
        return self._datetime_of_seconds.year

    @property
    def month(self) -> int:
        """Return the datetime's month (1-12)."""
        return self._datetime_of_seconds.month

    @property
    def day(self) -> int:
        """Return the datetime's day (1-31)."""
        return self._datetime_of_seconds.day

    @property
    def hour(self) -> int:
        """Return the datetime's hour (0-23)."""
        return self._datetime_of_seconds.hour

    @property
    def minute(self) -> int:
        """Return the datetime's minute (0-59)."""
        return self._datetime_of_seconds.minute

    @property
    def second(self) -> int:
        """Return the datetime's second (0-59)."""
        return self._datetime_of_seconds.second

    @property
    def microsecond(self) -> int:
        """Return the rounded number of microseconds."""
        return self._nanos // MICROS_IN_NANO

    @property
    def nanos(self) -> int:
        """Return the nanoseconds part."""
        return self._nanos

    @property
    def tzinfo(self) -> tzinfo:
        """Return timezone info."""
        return self._datetime_of_seconds.tzinfo

    @property
    def date(self) -> date:
        """Return the date part."""
        return self._datetime_of_seconds.date()

    @property
    def weekday(self) -> int:
        """Return the day of the week as an integer, where Monday == 0 ... Sunday == 6."""
        return self._datetime_of_seconds.weekday()

    @property
    def isoformat(self) -> str:
        """Return the ISO format string of the datetime."""
        return f"{self._datetime_of_seconds.isoformat()}.{self._nanos:09d}"

    def __str__(self):
        return f"{self.isoformat}"

    def __repr__(self):
        return f"Datetime({self._datetime_of_seconds!r}, {self._nanos})"

    def __hash__(self):
        return hash((self._datetime_of_seconds, self._nanos))

    def __eq__(self, other):
        if other is self:
            return True
        if not other or type(self) != type(other):
            return False
        return self.datetime_of_seconds == other.datetime_of_seconds and self.nanos == other.nanos
