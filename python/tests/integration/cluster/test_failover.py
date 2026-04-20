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

import subprocess
import unittest
from time import sleep

from typedb.driver import TypeDB, Credentials, DriverOptions, DriverTlsConfig, TransactionType

ADDRESSES = ["127.0.0.1:11729", "127.0.0.1:21729", "127.0.0.1:31729"]
USERNAME = "admin"
PASSWORD = "password"
DATABASE_NAME = "test-failover"
FAILOVER_ITERATIONS = 10
POST_KILL_WAIT_SECS = 5
PRIMARY_POLL_RETRIES = 20
PRIMARY_POLL_INTERVAL_SECS = 2
CLUSTER_NODE_SCRIPT = "tool/test/cluster-server.sh"


def cluster_node(command, node_id):
    result = subprocess.run(
        [CLUSTER_NODE_SCRIPT, command, str(node_id)],
        capture_output=True, text=True
    )
    assert result.returncode == 0, (
        f"{CLUSTER_NODE_SCRIPT} {command} {node_id} failed: {result.stderr}"
    )


def node_id_from_address(address):
    port = address.rsplit(":", 1)[1]
    return port[0]


def create_driver():
    return TypeDB.driver(
        ADDRESSES,
        Credentials(USERNAME, PASSWORD),
        DriverOptions(DriverTlsConfig.disabled()),
    )


def get_primary_server(driver):
    for attempt in range(PRIMARY_POLL_RETRIES):
        primary = driver.primary_server()
        if primary is not None:
            return primary
        if attempt < PRIMARY_POLL_RETRIES - 1:
            print(f"  No primary server found (attempt {attempt + 1}/{PRIMARY_POLL_RETRIES}). "
                  f"Retrying in {PRIMARY_POLL_INTERVAL_SECS}s...")
            sleep(PRIMARY_POLL_INTERVAL_SECS)
    raise AssertionError("Retry limit exceeded while seeking a primary server.")


class TestClusterFailover(unittest.TestCase):

    def setUp(self):
        driver = create_driver()
        if driver.databases.contains(DATABASE_NAME):
            driver.databases.get(DATABASE_NAME).delete()
        driver.close()

    def tearDown(self):
        try:
            driver = create_driver()
            if driver.databases.contains(DATABASE_NAME):
                driver.databases.get(DATABASE_NAME).delete()
            driver.close()
        except Exception:
            pass

    def test_primary_failover(self):
        print("=== Cluster Failover Test ===")

        # Connect driver (cluster must already be running via start-cluster-servers.sh)
        print("Connecting driver...")
        driver = create_driver()

        # Setup database with schema
        print("Setting up database and schema...")
        driver.databases.create(DATABASE_NAME)

        tx = driver.transaction(DATABASE_NAME, TransactionType.SCHEMA)
        tx.query("define entity person;").resolve()
        tx.commit()

        self._verify_read_query(driver)
        print("Initial setup verified.")

        # Failover loop
        for iteration in range(1, FAILOVER_ITERATIONS + 1):
            print(f"\n--- Failover iteration {iteration}/{FAILOVER_ITERATIONS} ---")

            primary = get_primary_server(driver)
            primary_address = primary.address
            node_id = node_id_from_address(primary_address)
            print(f"  Primary server: {primary_address} (node {node_id})")

            print(f"  Killing node {node_id}...")
            cluster_node("kill", node_id)

            print(f"  Waiting {POST_KILL_WAIT_SECS}s for re-election...")
            sleep(POST_KILL_WAIT_SECS)

            print("  Verifying read query on new primary...")
            self._verify_read_query(driver)
            print("  Read query succeeded.")

            print(f"  Restarting node {node_id}...")
            cluster_node("start", node_id)
            cluster_node("await", node_id)
            print(f"  Node {node_id} restarted.")

        print(f"\n=== All {FAILOVER_ITERATIONS} failover iterations passed! ===")
        driver.close()

    def _verify_read_query(self, driver):
        tx = driver.transaction(DATABASE_NAME, TransactionType.READ)
        answer = tx.query("match entity $t;").resolve()
        rows = [row for row in answer.as_concept_rows()]
        self.assertTrue(len(rows) > 0, "Expected at least one entity type in read query results")


if __name__ == "__main__":
    unittest.main(verbosity=2)
