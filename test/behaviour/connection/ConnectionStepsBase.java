package grakn.client.test.behaviour.connection;

import grakn.client.GraknClient;
import grakn.common.test.server.GraknSingleton;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public abstract class ConnectionStepsBase {
    public static int THREAD_POOL_SIZE = 32;
    public static ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    public static GraknClient client;
    public static List<GraknClient.Session> sessions = new ArrayList<>();
    public static List<CompletableFuture<GraknClient.Session>> sessionsParallel = new ArrayList<>();
    public static Map<GraknClient.Session, List<GraknClient.Transaction>> sessionsToTransactions = new HashMap<>();
    public static Map<GraknClient.Session, List<CompletableFuture<GraknClient.Transaction>>> sessionsToTransactionsParallel = new HashMap<>();
    public static Map<CompletableFuture<GraknClient.Session>, List<CompletableFuture<GraknClient.Transaction>>> sessionsParallelToTransactionsParallel = new HashMap<>();
    private static boolean isBeforeAllRan = false;

    public static GraknClient.Transaction tx() {
        return sessionsToTransactions.get(sessions.get(0)).get(0);
    }

    abstract void beforeAll();

    void beforeImpl() {
        if (!isBeforeAllRan) {
            beforeAll();
            isBeforeAllRan = true;
        }
        assertNull(client);
        String address = GraknSingleton.getGraknRunner().address();
        assertNotNull(address);
        client = createGraknClient(address);
        client.databases().all().forEach(database -> client.databases().delete(database));
        System.out.println("ConnectionSteps.before");
    }

    void afterImpl() {
        sessions.parallelStream().forEach(GraknClient.Session::close);
        sessions.clear();

        Stream<CompletableFuture<Void>> closures = sessionsParallel
                .stream().map(futureSession -> futureSession.thenApplyAsync(session -> {
                    session.close();
                    return null;
                }));
        CompletableFuture.allOf(closures.toArray(CompletableFuture[]::new)).join();
        sessionsParallel.clear();

        sessionsToTransactions.clear();
        sessionsToTransactionsParallel.clear();
        sessionsParallelToTransactionsParallel.clear();
        client.databases().all().forEach(database -> client.databases().delete(database));
        client.close();
        assertFalse(client.isOpen());
        client = null;
        System.out.println("ConnectionSteps.after");
    }

    abstract GraknClient createGraknClient(String address);

    void connectionHasBeenOpenedImpl() {
        assertNotNull(client);
        assertTrue(client.isOpen());
    }

    void connectionDoesNotHaveAnyDatabaseImpl() {
        assertTrue(client.databases().all().isEmpty());
    }
}
