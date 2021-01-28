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

package grakn.client.rpc.cluster;

import java.util.Objects;

import static java.lang.Integer.parseInt;

public class Address {
    public static class Server {
        private final String host;
        private final int clientPort;
        private final int serverPort;

        public static Server parse(String address) {
            String[] split = address.split(":");
            return new Server(split[0], parseInt(split[1]), parseInt(split[2]));
        }

        public Server(String host, int clientPort, int serverPort) {
            this.host = host;
            this.clientPort = clientPort;
            this.serverPort = serverPort;
        }

        public String host() {
            return host;
        }

        public int clientPort() {
            return clientPort;
        }

        public int serverPort() {
            return serverPort;
        }

        public String server() {
            return host + ":" + serverPort;
        }

        public String client() {
            return host + ":" + clientPort;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Server server = (Server) o;
            return clientPort == server.clientPort &&
                    serverPort == server.serverPort &&
                    Objects.equals(host, server.host);
        }

        @Override
        public int hashCode() {
            return Objects.hash(host, clientPort, serverPort);
        }

        @Override
        public String toString() {
            return host + ":" + clientPort + ":" + serverPort;
        }
    }
}
