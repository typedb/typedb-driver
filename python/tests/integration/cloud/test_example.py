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

# TODO: Rewrite this as a full cloud driver's usage example for README similar to core.

# import os
# import unittest
# from unittest import TestCase
#
# from hamcrest import *
#
# from typedb.driver import *
#
# TYPEDB = "typedb"
# DATA = SessionType.DATA
# WRITE = TransactionType.WRITE
# CREDENTIAL = Credentials("admin", "password", tls_enabled=True, tls_root_ca_path=os.environ["ROOT_CA"])
#
#
# class TestExample(TestCase):
#
#     def test_core_driver_while_running_cloud(self):
#         assert_that(calling(lambda: TypeDB.core_driver("localhost:11729")), raises(TypeDBDriverException))
#
#     def test_open_close_transaction(self):
#         addresses = [
#             "localhost:11729",
#             "localhost:21729",
#             "localhost:31729"
#         ]
#         driver = TypeDB.cloud_driver(addresses, CREDENTIAL)
#         assert_that(driver.is_open(), is_(True))
#         driver.close()
#         assert_that(driver.is_open(), is_(False))
#
#     def test_address_translation(self):
#         address_translation = {
#             "localhost:11729": "localhost:11729",
#             "localhost:21729": "localhost:21729",
#             "localhost:31729": "localhost:31729"
#         }
#         with TypeDB.cloud_driver(address_translation, CREDENTIAL) as driver:
#             if TYPEDB not in [db.name for db in driver.databases.all()]:
#                 driver.databases.create(TYPEDB)
#             with driver.transaction(TYPEDB, WRITE) as tx:
#                 root = tx.getQueryType.get_root_entity_type()
#                 assert_that(len(list(root.get_subtypes(tx))), equal_to(1))
#
#
# if __name__ == "__main__":
#     unittest.main(verbosity=2)
