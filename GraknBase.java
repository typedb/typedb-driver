package grakn.client;

interface GraknBase {

    interface Client<TOptions extends GraknOptions> extends AutoCloseable {

        Grakn.Session session(String database, Grakn.Session.Type type);

        Grakn.Session session(String database, Grakn.Session.Type type, TOptions options);

        Grakn.DatabaseManager databases();

        boolean isOpen();

        void close();
    }
}
