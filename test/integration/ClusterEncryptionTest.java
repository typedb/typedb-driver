package com.vaticle.typedb.client.test.integration;

import com.vaticle.typedb.client.TypeDB;
import com.vaticle.typedb.client.api.TypeDBClient;
import com.vaticle.typedb.client.api.TypeDBCredential;
import com.vaticle.typedb.common.test.cluster.TypeDBClusterRunner;
import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ClusterEncryptionTest {

    private static final Logger LOG = LoggerFactory.getLogger(ClusterEncryptionTest.class);
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "password";
    private static final Path CLUSTER_CA_PATH = Path.of("test/integration/resources/dev-root-ca.pem");
    private static final Path FAKE_CA_PATH = Path.of("test/integration/resources/fake-root-ca.pem");

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
    public void cluster_size3_connect_success() {
        createCluster(Collections.singletonMap("--server.encryption.enable", "true"));
        clusterRunner.start();
        try (TypeDBClient.Cluster client = TypeDB.clusterClient(clusterRunner.externalAddresses(), new TypeDBCredential(ADMIN_USERNAME, ADMIN_PASSWORD, CLUSTER_CA_PATH))) {
            LOG.info("Connected successfully");
        } finally {
            shutDownCluster();
        }
    }
}