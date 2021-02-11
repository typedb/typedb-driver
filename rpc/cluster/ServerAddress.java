package grakn.client.rpc.cluster;

import java.util.Objects;

import static java.lang.Integer.parseInt;

public class ServerAddress {
    private final String host;
    private final int clientPort;
    private final int serverPort;

    public static ServerAddress parse(String address) {
        String[] split = address.split(":");
        return new ServerAddress(split[0], parseInt(split[1]), parseInt(split[2]));
    }

    public ServerAddress(String host, int clientPort, int serverPort) {
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
        ServerAddress serverAddress = (ServerAddress) o;
        return clientPort == serverAddress.clientPort &&
                serverPort == serverAddress.serverPort &&
                Objects.equals(host, serverAddress.host);
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
