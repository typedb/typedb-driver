package grakn.client.connection;

import grakn.client.Grakn.Session;
import grakn.protocol.SessionProto;

abstract class ConnectionMessageWriter {

    static SessionProto.Session.Type sessionType(final Session.Type type) {
        switch (type) {
            case DATA:
                return SessionProto.Session.Type.DATA;
            case SCHEMA:
                return SessionProto.Session.Type.SCHEMA;
            default:
                return SessionProto.Session.Type.UNRECOGNIZED;
        }
    }
}
