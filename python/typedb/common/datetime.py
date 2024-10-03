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
from typing import Optional
from zoneinfo import ZoneInfo

NANOS_DIGITS = 9
NANOS_IN_SECOND = 10 ** NANOS_DIGITS
MICROS_IN_NANO = 1000


class Datetime:
    """
    An extension class for ``datetime.datetime`` class to store additional information about nanoseconds.
    It is split to a timestamp (time zoned or not) based on the number of full seconds and a nanoseconds part.
    """

    """
    Initialise a new ``Datetime``.

    :param timestamp_seconds: Amount of full seconds since the epoch.
    :param subsec_nanos: A number of nanoseconds since the last seconds boundary. Should be between 0 and 999,999,999.
    :param tz_name: A timezone name. Accepts any format suitable for ``ZoneInfo``, e.g. IANA.
    :raises ValueError: If subsec_nanos is not within the valid range.
    :raises ZoneInfoNotFoundError: If the tz_name is invalid.
    """

    def __init__(self, timestamp_seconds: int, subsec_nanos: int, tz_name: Optional[str] = None,
                 is_tz_aware_timestamp: bool = False):
        if not (0 <= subsec_nanos < NANOS_IN_SECOND):
            raise ValueError("subsec_nanos must be between 0 and 999,999,999")
        if not isinstance(timestamp_seconds, int) and \
                not (isinstance(timestamp_seconds, float) and timestamp_seconds.is_integer()):
            raise ValueError("timestamp_seconds must be integer")

        self._tz_name = tz_name
        if self._tz_name is None:
            self._datetime_of_seconds = datetime.utcfromtimestamp(timestamp_seconds)
        else:
            if is_tz_aware_timestamp:
                self._datetime_of_seconds = datetime.utcfromtimestamp(timestamp_seconds).replace(
                    tzinfo=ZoneInfo(self._tz_name))
            else:
                self._datetime_of_seconds = datetime.fromtimestamp(timestamp_seconds, ZoneInfo(self._tz_name))
        self._nanos = subsec_nanos

    @property
    def datetime_without_nanos(self) -> datetime:
        """Return the standard library's datetime, containing data up to microseconds."""
        return datetime(year=self.year, month=self.month, day=self.day, hour=self.hour, minute=self.minute,
                        second=self.second, microsecond=self.microsecond, tzinfo=self.tzinfo)

    @property
    def tz_name(self) -> Optional[str]:
        """Return the timezone name."""
        return self._tz_name

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
    def tz_id(self) -> Optional[str]:
        """Return the string representation of tz_id."""
        return self._tz_id

    @property
    def date(self) -> date:
        """Return the date part."""
        return self._datetime_of_seconds.date()

    @property
    def weekday(self) -> int:
        """Return the day of the week as an integer, where Monday == 0 ... Sunday == 6."""
        return self._datetime_of_seconds.weekday()

    @classmethod
    def from_string(cls, datetime_str: str, tz_name: Optional[str] = None,
                    datetime_fmt: str = "%Y-%m-%dT%H:%M:%S") -> Datetime:
        """
        Parses a Datetime object from a string with an optional nanoseconds part.

        :param datetime_str: The timezone-aware datetime string to parse. Should either be "{datetime_fmt}" or
                             "{datetime_fmt}.{nanos}". All digits of {nanos} after the 9th one are truncated!
        :param tz_name: A timezone name. Accepts any format suitable for ``ZoneInfo``, e.g.
        :param datetime_fmt: The format of the datetime string without the fractional (.%f) part. Default
                             is "%Y-%m-%dT%H:%M:%S".
        :return: A Datetime object.

        Examples
        --------

        ::

            Datetime.from_string("2024-09-21T18:34:22")
            Datetime.from_string("2024-09-21T18:34:22.009257123")
            Datetime.from_string("2024-09-21T18:34:22.009257123", "Europe/London")
            Datetime.from_string("2024-09-21", datetime_fmt="%Y-%m-%d")
            Datetime.from_string("21/09/24 18:34", "Europe/London", "%d/%m/%y %H:%M")
        """
        if '.' in datetime_str:
            datetime_part, subsec_part = datetime_str.split('.')
        else:
            datetime_part, subsec_part = datetime_str, "0"

        timestamp_sec = int(datetime.strptime(datetime_part, datetime_fmt).timestamp())
        subsec_part = (subsec_part + "0" * NANOS_DIGITS)[:NANOS_DIGITS]
        nanos = int(subsec_part)

        return cls(timestamp_seconds=timestamp_sec, subsec_nanos=nanos, tz_name=tz_name, is_tz_aware_timestamp=True)

    ISO_TZ_LEN = 6

    def isoformat(self) -> str:
        """Return the time formatted according to ISO."""
        datetime_part = self._datetime_of_seconds.isoformat()
        tz_part = ""
        if self._tz_name is not None:
            tz_part = datetime_part[-self.ISO_TZ_LEN:]
            datetime_part = datetime_part[:-self.ISO_TZ_LEN]
        return f"{datetime_part}.{self._nanos:09d}{tz_part}"

    def __str__(self):
        return self.isoformat()

    def __repr__(self):
        return f"Datetime({self._datetime_of_seconds!r}, {self._nanos}, {self._tz_name})"

    def __hash__(self):
        return hash((self._datetime_of_seconds, self._nanos, self._tz_name))

    def __eq__(self, other):
        if other is self:
            return True
        if other is None or not isinstance(other, self.__class__):
            return False
        return (self._datetime_of_seconds == other._datetime_of_seconds
                and self._nanos == other._nanos
                and self._tz_name == other._tz_name)
