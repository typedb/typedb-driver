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

import unittest
from unittest import TestCase

from typedb.driver import *

SCHEMA = SessionType.SCHEMA
DATA = SessionType.DATA
READ = TransactionType.READ
WRITE = TransactionType.WRITE


class TestDriverPython(TestCase):
    """
    Very basic tests to ensure no error occur when performing simple operations with the typedb-driver distribution
    """

    @classmethod
    def setUpClass(cls):
        super(TestDriverPython, cls).setUpClass()
        global driver
        driver = TypeDB.core_driver(TypeDB.DEFAULT_ADDRESS)

    @classmethod
    def tearDownClass(cls):
        super(TestDriverPython, cls).tearDownClass()
        global driver
        driver.close()

    def setUp(self):
        if not driver.databases.contains("typedb"):
            driver.databases.create("typedb")

    def test_database(self):
        if driver.databases.contains("typedb"):
            driver.databases.get("typedb").delete()
        driver.databases.create("typedb")
        self.assertTrue(driver.databases.contains("typedb"))

    def test_session(self):
        session = driver.session("typedb", SCHEMA)
        session.close()

    def test_transaction(self):
        with driver.session("typedb", SCHEMA) as session:
            with session.transaction(WRITE) as tx:
                pass

    def test_define_and_undef_relation_type(self):
        with driver.session("typedb", SCHEMA) as session:
            with session.transaction(WRITE) as tx:
                tx.query.define("define lionfight sub relation, relates victor, relates loser;")
                lionfight_type = tx.concepts.get_relation_type("lionfight").resolve()
                print("define: " + lionfight_type.get_label().name)
                tx.query.undefine("undefine lionfight sub relation;")
                tx.commit()

    def test_insert_some_entities(self):
        with driver.session("typedb", SCHEMA) as session:
            with session.transaction(WRITE) as tx:
                tx.query.define("define lion sub entity;")
                tx.commit()
        with driver.session("typedb", DATA) as session:
            with session.transaction(WRITE) as tx:
                for answer in tx.query.insert("insert $a isa lion; $b isa lion; $c isa lion;"):
                    print("insert: " + str(answer))
                    tx.commit()


if __name__ == "__main__":
    unittest.main(verbosity=2)
