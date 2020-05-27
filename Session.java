package grakn.client;

public interface Session extends AutoCloseable {
    Transaction.Builder transaction();

    Transaction transaction(Transaction.Type type);

    boolean isOpen();

    void close();

    Keyspace keyspace();
}
