package grakn.client.test.behaviour.connection;

import grakn.client.GraknClient;
import grakn.common.test.server.GraknClusterRunner;
import grakn.common.test.server.GraknSingleton;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class ConnectionStepsCluster extends ConnectionStepsBase {

    // The following code is for running the Grakn Core distribution imported as an artifact.
    // If you wish to debug locally against an instance of Grakn that is already running in
    // the background, comment out all the code in this file that references 'runner'
    // and update ConnectionSteps to connect to GraknClient.DEFAULT_URI.
    private static GraknClusterRunner server;

    @BeforeClass
    public static void beforeClass() throws InterruptedException, IOException, TimeoutException {
        server = new GraknClusterRunner(true);
        server.start();
        GraknSingleton.setGraknRunner(server);
    }

    @AfterClass
    public static void afterClass() throws InterruptedException, IOException, TimeoutException {
        server.stop();
    }


    @Override
    GraknClient createGraknClient(String address) {
        return GraknClient.cluster(address);
    }
}
