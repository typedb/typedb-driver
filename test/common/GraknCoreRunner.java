/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package grakn.client.test.common;

import org.apache.commons.io.FileUtils;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class GraknCoreRunner {
    private Path GRAKN_DISTRIBUTION_FILE;
    private Path GRAKN_TARGET_DIRECTORY;
    private String GRAKN_DISTRIBUTION_FORMAT;
    private static final String TAR = ".tar.gz";
    private static final String ZIP = ".zip";

    private ProcessExecutor executor;
    private Properties properties;

    public GraknCoreRunner(String distributionFile) throws InterruptedException, TimeoutException, IOException {
        System.out.println("Constructing a Grakn Core runner");

        distributionFormat(distributionFile);

        GRAKN_DISTRIBUTION_FORMAT = distributionFormat(distributionFile);
        GRAKN_DISTRIBUTION_FILE = Paths.get(distributionFile);
        GRAKN_TARGET_DIRECTORY = distributionTarget(distributionFile);

        this.executor = new ProcessExecutor()
                .directory(Paths.get(".").toAbsolutePath().toFile())
                .redirectError(System.err)
                .readOutput(true);

        this.unzip();
        this.properties = readProperties(
                GRAKN_TARGET_DIRECTORY.resolve("server").resolve("conf").resolve("grakn.properties")
        );

        System.out.println("Grakn Core runner constructed");
    }

    private static String distributionFormat(String distributionFile) {
        if (distributionFile.endsWith(TAR)) {
            return TAR;
        } else if (distributionFile.endsWith(ZIP)) {
            return ZIP;
        } else {
            fail(String.format("Distribution file format should either be %s or %s", TAR, ZIP));
        }
        return "";
    }

    private static Path distributionTarget(String distributionFile) {
        String format = distributionFormat(distributionFile);
        return Paths.get(distributionFile.replaceAll(
                format.replace(".", "\\."), ""
        ));
    }

    public static void checkAndDeleteExistingDistribution(String distributionFile) throws IOException {
        Path target = distributionTarget(distributionFile);
        System.out.println("Checking for existing Grakn distribution at " + target.toAbsolutePath().toString());
        if (target.toFile().exists()) {
            System.out.println("There exists a Grakn Core distribution and will be deleted");
            FileUtils.deleteDirectory(target.toFile());
            System.out.println("Existing Grakn Core distribution deleted");
        } else {
            System.out.println("There is no existing Grakn Core distribution");
        }
    }

    private void unzip() throws IOException, TimeoutException, InterruptedException {
        System.out.println("Unarchiving Grakn Core distribution");
        if (GRAKN_DISTRIBUTION_FORMAT.equals(TAR)) {
            executor.command("tar", "-xf", GRAKN_DISTRIBUTION_FILE.toString(),
                             "-C", GRAKN_TARGET_DIRECTORY.getParent().toString()).execute();
        } else {
            executor.command("unzip", "-q", GRAKN_DISTRIBUTION_FILE.toString(),
                             "-d", GRAKN_TARGET_DIRECTORY.getParent().toString()).execute();
        }
        executor = executor.directory(GRAKN_TARGET_DIRECTORY.toFile());

        // TODO: Remove the following overwriting of grakn-core-ascii.txt, once we fix graknlabs/grakn#5433
        Path ascii = Paths.get(GRAKN_TARGET_DIRECTORY.toString(),
                               "server", "services", "grakn", "grakn-core-ascii.txt");
        Files.delete(ascii);
        Files.write(ascii, "".getBytes());

        System.out.println("Grakn Core distribution unarchived");
    }

    public String host() {
        return properties.getProperty("server.host");
    }

    public String port() {
        return properties.getProperty("grpc.port");
    }

    public String address() {
        return host() + ":" + port();
    }

    public void start() throws InterruptedException, TimeoutException, IOException {
        assertFalse("There is already an instance of Grakn running in the background",
                    isStorageRunning() || isServerRunning());

        System.out.println("Starting Grakn Core database server at " + GRAKN_TARGET_DIRECTORY.toAbsolutePath().toString());

        executor.command("./grakn", "server", "start").execute();
        assertTrue("Grakn did not manage to start", isStorageRunning() && isServerRunning());

        System.out.println("Grakn Core database server started");
    }

    public void stop() throws InterruptedException, IOException, TimeoutException {
        System.out.println("Stopping Grakn Core database server");

        executor.command("./grakn", "server", "stop").execute();
        assertTrue(!isStorageRunning() && !isServerRunning());

        System.out.println("Grakn Core database server stopped");
    }

    public void printLogs() throws InterruptedException, TimeoutException, IOException {
        System.out.println("================");
        System.out.println("Grakn Core Logs:");
        System.out.println("================");
        executor.command("cat", Paths.get(".", "logs", "grakn.log").toString()).execute();

        System.out.println("===============");
        System.out.println("Cassandra Logs:");
        System.out.println("===============");
        executor.command("cat", Paths.get(".", "logs", "cassandra.log").toString()).execute();
    }

    private boolean isStorageRunning() throws InterruptedException, TimeoutException, IOException {
        System.out.println("Checking if Grakn Storage is running");
        ProcessResult output = executor.command("./grakn", "server", "status").execute();
        assertEquals(0, output.getExitValue());
        String[] lines = output.outputString().split("\n");
        boolean isRunning = lines[lines.length - 2].contains(": RUNNING");

        System.out.println("Grakn Storage is running: " + isRunning);
        return isRunning;
    }

    private boolean isServerRunning() throws InterruptedException, TimeoutException, IOException {
        System.out.println("Checking if Grakn Server is running");
        ProcessResult output = executor.command("./grakn", "server", "status").execute();
        assertEquals(0, output.getExitValue());
        String[] lines = output.outputString().split("\n");
        boolean isRunning = lines[lines.length - 1].contains(": RUNNING");

        System.out.println("Grakn Server is running: " + isRunning);
        return isRunning;
    }

    private static Properties readProperties(Path path) throws IOException {
        System.out.println("Reading Grakn Core database configuration properties");
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream(path.toString()));
        } catch (FileNotFoundException e) {
            System.out.printf("Could not load server properties from %s\n", path);
            throw e;
        } catch (IOException e) {
            System.out.println("Could not load server properties from input stream provided");
            throw e;
        }

        System.out.println("Grakn Core database configuration properties read");
        return prop;
    }
}
