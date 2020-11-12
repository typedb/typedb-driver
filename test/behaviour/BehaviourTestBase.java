package grakn.core.test.behaviour;

import grakn.common.test.server.GraknCoreRunner;
import grakn.common.test.server.GraknSingleton;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public abstract class BehaviourTestBase {
    // The following code is for running the Grakn Core distribution imported as an artifact.
    // If you wish to debug locally against an instance of Grakn that is already running in the background,
    // comment out all the code in this file that references 'runner'.

    private static GraknCoreRunner runner;

    @BeforeClass
    public static void setupBehaviourTests() throws InterruptedException, IOException, TimeoutException {
        runner = new GraknCoreRunner();
        runner.start();
        GraknSingleton.setGraknRunner(runner);
    }

    @AfterClass
    public static void tearDownBehaviourTests() throws InterruptedException, IOException, TimeoutException {
        runner.stop();
    }
}
