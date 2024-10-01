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

    def __str__(self):
        return f"months: {self.months}, days: {self.days}, nanos: {self.nanos}"

    def __repr__(self):
        return f"Duration('{str(self)}')"

    def __hash__(self):
        return hash((self.months, self.days, self.nanos))

    def __eq__(self, other):
        if other is self:
            return True
        if not other or type(self) != type(other):
            return False
        return self.months == other.months and self.days == other.days and self.nanos == other.nanos

