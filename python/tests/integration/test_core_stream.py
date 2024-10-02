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


class TestStream(TestCase):

    def setUp(self):
        with TypeDB.core_driver(TypeDB.DEFAULT_ADDRESS) as driver:
            if TYPEDB not in [db.name for db in driver.databases.all()]:
                driver.databases.create(TYPEDB)

    def test_multiple_done_response_handling(self):
        with TypeDB.core_driver(TypeDB.DEFAULT_ADDRESS) as driver:
            with driver.transaction(TYPEDB, SCHEMA) as tx:
                promises = [
                    tx.query(f"define entity person, owns name{i}; attribute name{i}, value string;")
                    for i in range(51)
                ]
                for promise in promises:
                    promise.resolve()
                tx.commit()
            # With these options (the default in TypeDB at time of writing), the server may respond with:
            # 50 answers -> CONTINUE -> 1 answer [compensating for latency] -> DONE. The driver will respond to
            # CONTINUE with STREAM to keep iterating, and the server responds to STREAM with a 2nd DONE message.
            # This is expected and should be handled correctly (ie: ignored) by the driver.
            # tx_options = Options(prefetch=True, prefetch_size=50) # TODO: Return when Options are back
            for i in range(50):
                with driver.transaction(TYPEDB, READ) as tx: # driver.transaction(TYPEDB, READ, tx_options) as tx:
                    person_type = list(tx.query("match $p sub person;").resolve().as_concept_rows())[0].get("p")
                    # TODO: Rewrite
                    # _attrs = list(person_type.get_owns(tx, annotations={Annotation.key()}))
                    # next(tx.query.get("match $x sub thing; get; limit 1;"))


if __name__ == "__main__":
    unittest.main(verbosity=2)
