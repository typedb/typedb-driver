#
# Copyright (C) 2022 Vaticle
#
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
#

from abc import ABC, abstractmethod


class Numeric(ABC):
    """
    Stores an aggregate query answer.
    """

    @abstractmethod
    def is_int(self) -> bool:
        """
        Checks if the type of an aggregate answer is an integer.

        :return:

        Examples
        --------
        ::

           numeric.is_int()
        """
        pass

    @abstractmethod
    def is_float(self) -> bool:
        """
        Checks if the type of an aggregate answer is a float.

        :return:

        Examples
        --------
        ::

           numeric.is_float()
        """
        pass

    @abstractmethod
    def is_nan(self) -> bool:
        """
        Checks if the aggregate answer is not a number.

        :return:

        Examples
        --------
        ::

           numeric.is_nan()
        """
        pass

    @abstractmethod
    def as_int(self) -> int:
        """
        Retrieves numeric value of an aggregate answer as an integer.

        :return:

        Examples
        --------
        ::

           numeric.as_int()
        """
        pass

    @abstractmethod
    def as_float(self) -> float:
        """
        Retrieves numeric value of an aggregate answer as a float.

        :return:

        Examples
        --------
        ::

           numeric.as_float()
        """
        pass
