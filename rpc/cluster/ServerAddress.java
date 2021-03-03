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

import grakn.client.common.exception.GraknClientException;
import java.util.Objects;

import static grakn.client.common.exception.ErrorMessage.Internal.ILLEGAL_ARGUMENT;
import static java.lang.Integer.parseInt;

public class ServerAddress {
    private final String externalHost;
    private final int externalPort;
    private final String internalHost;
    private final int internalPort;

    public ServerAddress(String externalHost, int externalPort, String internalHost, int internalPort) {
        this.externalHost = externalHost;
        this.externalPort = externalPort;
        this.internalHost = internalHost;
        this.internalPort = internalPort;
    }

    static ServerAddress parse(String address) {
        String[] s1 = address.split(",");
        if (s1.length == 1) {
            String[] s2 = address.split(":");
            return new ServerAddress(s2[0], parseInt(s2[1]), s2[0], parseInt(s2[2]));
        } else if (s1.length == 2) {
            String[] clientAddress = s1[0].split(":");
            String[] serverAddress = s1[1].split(":");
            if (clientAddress.length != 2 || serverAddress.length != 2) throw new GraknClientException(ILLEGAL_ARGUMENT.message(address));
            return new ServerAddress(clientAddress[0], parseInt(clientAddress[1]), serverAddress[0], parseInt(serverAddress[1]));
        } else throw new GraknClientException(ILLEGAL_ARGUMENT.message(address));
    }

    public String external() {
        return externalHost + ":" + externalPort;
    }

    public String externalHost() {
        return externalHost;
    }

    public int externalPort() {
        return externalPort;
    }

    public String internal() {
        return externalHost + ":" + internalPort;
    }

    public String internalHost() {
        return internalHost;
    }

    public int internalPort() {
        return internalPort;
    }

    @Override
    public String toString() {
        return externalHost + ":" + externalPort + "," + internalHost + ":" + internalPort;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerAddress that = (ServerAddress) o;
        return externalPort == that.externalPort &&
                internalPort == that.internalPort &&
                Objects.equals(externalHost, that.externalHost) &&
                Objects.equals(internalHost, that.internalHost);
    }

    @Override
    public int hashCode() {
        return Objects.hash(externalHost, externalPort, internalHost, internalPort);
    }
}
