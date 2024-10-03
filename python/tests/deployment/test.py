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


class TestDeployedPythonDriver(TestCase):
    """
    Very basic tests to ensure no error occur when performing simple operations with the typedb-driver distribution
    """

    @classmethod
    def setUpClass(cls):
        super(TestDeployedPythonDriver, cls).setUpClass()
        global driver
        driver = TypeDB.core_driver(TypeDB.DEFAULT_ADDRESS)

    @classmethod
    def tearDownClass(cls):
        super(TestDeployedPythonDriver, cls).tearDownClass()
        global driver
        driver.close()
        print("Driver is tested successfully!")

    def setUp(self):
        if not driver.databases.contains(TYPEDB):
            driver.databases.create(TYPEDB)

    def test_query(self):
        transaction = driver.transaction(TYPEDB, SCHEMA)
        answer = transaction.query("define entity person;").resolve()
        self.assertIsNotNone(answer)
        self.assertTrue(answer.is_ok())
        transaction.close()


if __name__ == "__main__":
    unittest.main(verbosity=2)
