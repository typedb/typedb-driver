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
import os
import subprocess
import unittest
from time import sleep
from unittest import TestCase

from typedb.client import *

SCHEMA = SessionType.SCHEMA
WRITE = TransactionType.WRITE
READ = TransactionType.READ


class TestClusterFailover(TestCase):

    def setUp(self):
        root_ca_path = os.environ["ROOT_CA"]
        credential = TypeDBCredential("admin", "password", tls_root_ca_path=root_ca_path)
        print("SetUp", flush=True)
        with TypeDB.cluster_client(["localhost:11729", "localhost:21729", "localhost:31729"], credential) as client:
            if client.databases.contains("typedb"):
                client.databases.get("typedb").delete()
            client.databases.create("typedb")

    @staticmethod
    def server_start(index):
        subprocess.Popen([
            "../%s/typedb" % index, "cluster",
            "--storage.data", "server/data",
            "--server.address", "localhost:%s1729" % index,
            "--server.internal-address.zeromq", "localhost:%s1730" % index,
            "--server.internal-address.grpc", "localhost:%s1731" % index,
            "--server.peers.peer-1.address", "localhost:11729",
            "--server.peers.peer-1.internal-address.zeromq", "localhost:11730",
            "--server.peers.peer-1.internal-address.grpc", "localhost:11731",
            "--server.peers.peer-2.address", "localhost:21729",
            "--server.peers.peer-2.internal-address.zeromq", "localhost:21730",
            "--server.peers.peer-2.internal-address.grpc", "localhost:21731",
            "--server.peers.peer-3.address", "localhost:31729",
            "--server.peers.peer-3.internal-address.zeromq", "localhost:31730",
            "--server.peers.peer-3.internal-address.grpc", "localhost:31731",
            "--server.encryption.enable", "true"
        ])

    def get_primary_replica(self, database_manager: DatabaseManager):
        retry_num = 0
        while retry_num < 10:
            print("Discovering replicas for database 'typedb'...")
            db = database_manager.get("typedb")
            print("Discovered " + str([str(replica) for replica in db.replicas()]))
            if db.primary_replica():
                return db.primary_replica()
            else:
                retry_num += 1
                print("There is no primary replica yet. Retrying in 2s...")
                sleep(2)
                return self.get_primary_replica(database_manager)
        assert False, "Retry limit exceeded while seeking a primary replica."

    def test_put_entity_type_to_crashed_primary_replica(self):
        root_ca_path = os.environ["ROOT_CA"]
        credential = TypeDBCredential("admin", "password", tls_root_ca_path=root_ca_path)
        with TypeDB.cluster_client(["localhost:11729", "localhost:21729", "localhost:31729"], credential) as client:
            assert client.databases.contains("typedb")
            primary_replica = self.get_primary_replica(client.databases)
            print("Performing operations against the primary replica " + str(primary_replica))
            with client.session("typedb", SCHEMA) as session, session.transaction(WRITE) as tx:
                tx.concepts.put_entity_type("person")
                print("Put the entity type 'person'.")
                tx.commit()
            with client.session("typedb", SCHEMA) as session, session.transaction(READ) as tx:
                person = tx.concepts.get_entity_type("person")
                print("Retrieved entity type with label '%s' from primary replica." % person.get_label())
                assert person.get_label().name == "person"
            iteration = 0
            while iteration < 10:
                iteration += 1
                primary_replica = self.get_primary_replica(client.databases)
                print("Stopping primary replica (test %d/10)..." % iteration)
                port = primary_replica.address()[10:15]
                lsof = subprocess.check_output(["lsof", "-i", ":%s" % port])
                primary_replica_server_pid = [conn.split()[1] for conn in lsof.decode("utf-8").split("\n") if "LISTEN" in conn][0]
                print("Primary replica is hosted by server with PID %s" % primary_replica_server_pid)
                subprocess.check_call(["kill", "-9", primary_replica_server_pid])
                print("Primary replica stopped successfully.")
                sleep(5)  # TODO: This ensures the server is actually shut down, but it's odd that it needs to be so long
                with client.session("typedb", SCHEMA) as session, session.transaction(READ) as tx:
                    person = tx.concepts.get_entity_type("person")
                    print("Retrieved entity type with label '%s' from new primary replica." % person.get_label())
                    assert person.get_label().name == "person"
                idx = str(primary_replica.address())[10]
                self.server_start(idx)
                lsof = None
                live_check_iteration = 0
                while not lsof and live_check_iteration < 60:
                    live_check_iteration += 1
                    try:
                        lsof = subprocess.check_output(["lsof", "-i", ":%s" % port])
                    except subprocess.CalledProcessError:
                        pass


if __name__ == "__main__":
    unittest.main(verbosity=2)
