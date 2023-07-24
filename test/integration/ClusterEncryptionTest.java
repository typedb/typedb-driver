package com.vaticle.typedb.client.test.integration;

import com.vaticle.typedb.client.TypeDB;
import com.vaticle.typedb.client.api.TypeDBClient;
import com.vaticle.typedb.client.api.TypeDBCredential;
import com.vaticle.typedb.client.common.exception.TypeDBClientException;
import com.vaticle.typedb.common.test.cluster.TypeDBClusterRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

import static com.vaticle.typedb.client.common.exception.ErrorMessage.Client.CLUSTER_CONNECTION_CLOSED_UKNOWN;
import static com.vaticle.typedb.client.common.exception.ErrorMessage.Client.CLUSTER_SERVER_NOT_ENCRYPTED;
import static com.vaticle.typedb.client.common.exception.ErrorMessage.Client.CLUSTER_SSL_HANDSHAKE_FAILED;
import static com.vaticle.typedb.client.common.exception.ErrorMessage.Client.CLUSTER_UNABLE_TO_CONNECT;
import static org.junit.Assert.fail;

public class ClusterEncryptionTest {

    private static final Logger LOG = LoggerFactory.getLogger(ClusterEncryptionTest.class);
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "password";
    private static final Path CLUSTER_CA = Path.of("test/integration/resources/dev-root-ca.pem");
    private static final Path WRONG_CA = Path.of("test/integration/resources/wrong-root-ca.pem");

    private TypeDBClusterRunner clusterRunner;

    @After
    public void shutDownCluster() {
        if (clusterRunner != null) {
            clusterRunner.stop();
            clusterRunner.deleteFiles();
            clusterRunner = null;
        }
    }

    private void createCluster(Map<String, String> serverOpts) {
        if (clusterRunner == null) shutDownCluster();
        clusterRunner = TypeDBClusterRunner.create(Paths.get("."), 1, serverOpts);
    }

    @Test
    public void encrypted_server_tests() {
        createCluster(Collections.singletonMap("--server.encryption.enable", "true"));
        clusterRunner.start();
        try {
            try (TypeDBClient.Cluster client = TypeDB.clusterClient(clusterRunner.externalAddresses(), new TypeDBCredential(ADMIN_USERNAME, ADMIN_PASSWORD, CLUSTER_CA))) {
                LOG.info("Encrypted client connected successfully");
            } catch (Exception e) {
                fail("The encrypted client with correct CA failed to connect.");
            }

            try (TypeDBClient.Cluster client = TypeDB.clusterClient(clusterRunner.externalAddresses(), new TypeDBCredential(ADMIN_USERNAME, ADMIN_PASSWORD, WRONG_CA))) {
                fail("The encrypted client with wrong CA connected (should have failed).");
            } catch (TypeDBClientException e) {
                Assert.assertEquals(e.getErrorMessage(), CLUSTER_SSL_HANDSHAKE_FAILED);
                LOG.info("Expected exception was thrown: {}", e.getMessage());
            }

            try (TypeDBClient.Cluster client = TypeDB.clusterClient(clusterRunner.externalAddresses(), new TypeDBCredential(ADMIN_USERNAME, ADMIN_PASSWORD, false))) {
                fail("The encrypted client with wrong CA connected (should have failed).");
            } catch (TypeDBClientException e) {
                // Best we can do given the grpc message
                Assert.assertEquals(e.getErrorMessage(), CLUSTER_UNABLE_TO_CONNECT);
            }
        } finally {
            shutDownCluster();
        }
    }

    @Test
    public void unencrypted_server_tests() {
        createCluster(Collections.singletonMap("--server.encryption.enable", "false"));
        clusterRunner.start();
        try {
            try (TypeDBClient.Cluster client = TypeDB.clusterClient(clusterRunner.externalAddresses(), new TypeDBCredential(ADMIN_USERNAME, ADMIN_PASSWORD, false))) {
                LOG.info("Unencrypted client connected successfully!");
            } catch (Exception e) {
                fail("The unencrypted client failed to connect to unencrypted server.");
            }

            try (TypeDBClient.Cluster client = TypeDB.clusterClient(clusterRunner.externalAddresses(), new TypeDBCredential(ADMIN_USERNAME, ADMIN_PASSWORD, CLUSTER_CA))) {
                fail("The encrypted client connected to an unencrypted server (should have failed).");
            } catch (TypeDBClientException e) {
                Assert.assertEquals(e.getErrorMessage(), CLUSTER_SERVER_NOT_ENCRYPTED);
                LOG.info("Expected exception was thrown: {}", e.getMessage());
            }
        } finally {
            shutDownCluster();
        }
    }
}