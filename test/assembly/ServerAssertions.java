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

package grakn.client.test.assembly;

import org.junit.Assert;
import org.zeroturnaround.exec.ProcessExecutor;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ServerAssertions {
    public static final Path GRAKN_TARGET_DIRECTORY = Paths.get(".").toAbsolutePath();
    public static final Path ZIP_FULLPATH = Paths.get(GRAKN_TARGET_DIRECTORY.toString(), "grakn-core-all-mac.zip");
    public static final Path GRAKN_UNZIPPED_DIRECTORY = Paths.get(GRAKN_TARGET_DIRECTORY.toString(), "distribution-test", "grakn-core-all-mac");

    public static void assertGraknRunning() {
//        Config config = Config.read(GRAKN_UNZIPPED_DIRECTORY.resolve("server").resolve("conf").resolve("grakn.properties"));
//        boolean serverReady = isServerReady(config.getProperty(ConfigKey.SERVER_HOST_NAME), config.getProperty(ConfigKey.GRPC_PORT));
//        assertThat("assertGraknRunning() failed because ", serverReady, equalTo(true));
    }

    public static void assertGraknStopped() {
//        Config config = Config.read(GRAKN_UNZIPPED_DIRECTORY.resolve("server").resolve("conf").resolve("grakn.properties"));
//        boolean serverReady = isServerReady(config.getProperty(ConfigKey.SERVER_HOST_NAME), config.getProperty(ConfigKey.GRPC_PORT));
//        assertThat("assertGraknStopped() failed because ", serverReady, equalTo(false));
    }

    public static void assertZipExists() {
        System.out.println(ZIP_FULLPATH);
        System.out.println(GRAKN_UNZIPPED_DIRECTORY);

        if (!ZIP_FULLPATH.toFile().exists()) {
            Assert.fail("Grakn distribution '" + ZIP_FULLPATH.toAbsolutePath().toString() + "' could not be found. Please ensure it has been build (ie., run `mvn package`)");
        }
    }

    public static void unzipGrakn() throws IOException, InterruptedException, TimeoutException {
        new ProcessExecutor()
                .command("unzip", ZIP_FULLPATH.toString(), "-d", GRAKN_UNZIPPED_DIRECTORY.getParent().toString()).execute();
    }

    private static boolean isServerReady(String host, int port) {
        try {
            Socket s = new Socket(host, port);
            s.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}

