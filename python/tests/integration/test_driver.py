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
import time

from hamcrest import *
from typedb.driver import *


class TestExample(TestCase):

    def setUp(self):
        with TypeDB.driver(TypeDB.DEFAULT_ADDRESS, Credentials("admin", "password"), DriverOptions(is_tls_enabled=False)) as driver:
            if driver.databases.contains("typedb"):
                driver.databases.get("typedb").delete()


    def test_on_close_callback(self):
        with TypeDB.driver(TypeDB.DEFAULT_ADDRESS, Credentials("admin", "password"), DriverOptions(is_tls_enabled=False)) as driver:
            driver.databases.create("typedb")
            database = driver.databases.get("typedb")
            assert_that(database.name, is_("typedb"))

            tx = driver.transaction(database.name, TransactionType.READ)

            transaction_closed = {"closed": False}
            def callback(_error):
                transaction_closed.update({"closed": True})
            tx.on_close(callback)

            tx.close()

            assert_that(transaction_closed["closed"], is_(True))


if __name__ == "__main__":
    unittest.main(verbosity=2)
