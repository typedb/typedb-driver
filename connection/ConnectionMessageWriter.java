package grakn.client.connection;

import grabl.tracing.client.GrablTracingThreadStatic;
import grakn.client.Grakn.Session;
import grakn.protocol.SessionProto;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static grabl.tracing.client.GrablTracingThreadStatic.currentThreadTrace;
import static grabl.tracing.client.GrablTracingThreadStatic.isTracingEnabled;

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

    static Map<String, String> tracingData() {
        if (isTracingEnabled()) {
            final GrablTracingThreadStatic.ThreadTrace threadTrace = currentThreadTrace();
            if (threadTrace == null) {
                return Collections.emptyMap();
            }

            if (threadTrace.getId() == null || threadTrace.getRootId() == null) {
                return Collections.emptyMap();
            }

            final Map<String, String> metadata = new HashMap<>(2);
            metadata.put("traceParentId", threadTrace.getId().toString());
            metadata.put("traceRootId", threadTrace.getRootId().toString());
            return metadata;
        } else {
            return Collections.emptyMap();
        }
    }
}
