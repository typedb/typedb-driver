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

import unittest
from unittest import TestCase

from typedb.driver import *

TYPEDB = "typedb"
READ = TransactionType.READ
WRITE = TransactionType.WRITE
SCHEMA = TransactionType.SCHEMA


class TestDebug(TestCase):

    def setUp(self):
        with TypeDB.driver(TypeDB.DEFAULT_ADDRESS, Credentials("admin", "password"),
                           DriverOptions(is_tls_enabled=False)) as driver:
            if TYPEDB not in [db.name for db in driver.databases.all()]:
                driver.databases.create(TYPEDB)

    def test_debug(self):
        # Use this test for isolated debugging.
        pass


if __name__ == "__main__":
    unittest.main(verbosity=2)
