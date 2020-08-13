package grakn.client.connection;

import grakn.client.Grakn.Database;

public class GraknDatabase implements Database {
	private static final long serialVersionUID = 2726154016735929123L;
    public static final String DEFAULT = "grakn";

    private final String name;

    public GraknDatabase(String name) {
        if (name == null) {
            throw new NullPointerException("Null name");
        }
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }

    public final String toString() {
        return name();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final GraknDatabase that = (GraknDatabase) o;
        return this.name.equals(that.name);
    }

    public int hashCode() {
        int h = 1;
        h *= 1000003;
        h ^= this.name.hashCode();
        return h;
    }
}
