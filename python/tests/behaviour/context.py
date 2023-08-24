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

from __future__ import annotations
import behave.runner
from behave.model import Table
from typedb.client import *

from tests.behaviour.config.parameters import RootLabel


class Config:
    """
    Type definitions for Config.

    This class should not be instantiated. The initialisation of the actual Config object occurs in environment.py.
    """
    def __init__(self):
        self.userdata = {}


class Context(behave.runner.Context):
    """
    Type definitions for Context.

    This class should not be instantiated. The initialisation of the actual Context object occurs in environment.py.
    """
    def __init__(self):
        self.table: Optional[Table] = None
        self.THREAD_POOL_SIZE = 0
        self.client: Optional[TypeDBClient] = None
        self.sessions: list[TypeDBSession] = []
        self.sessions_to_transactions: dict[TypeDBSession, list[TypeDBTransaction]] = {}
        self.sessions_parallel: list[TypeDBSession] = []
        self.sessions_parallel_to_transactions_parallel: dict[TypeDBSession, list[TypeDBTransaction]] = {}
        self.session_options: Optional[TypeDBOptions] = None
        self.transaction_options: Optional[TypeDBOptions] = None
        self.things: dict[str, Thing] = {}
        self.answers: Optional[list[ConceptMap]] = None
        self.numeric_answer: Optional[Numeric] = None
        self.answer_groups: Optional[list[ConceptMapGroup]] = None
        self.numeric_answer_groups: Optional[list[NumericGroup]] = None
        self.config = Config()
        self.option_setters = {}

    def tx(self) -> TypeDBTransaction:
        return self.sessions_to_transactions[self.sessions[0]][0]

    def put(self, var: str, thing: Thing) -> None:
        pass

    def get(self, var: str) -> Thing:
        pass

    def get_thing_type(self, root_label: RootLabel, type_label: str) -> ThingType:
        pass

    def clear_answers(self) -> None:
        pass
