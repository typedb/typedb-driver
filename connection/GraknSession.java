package grakn.client.connection;

import com.google.protobuf.ByteString;
import grakn.client.Grakn.Database;
import grakn.client.Grakn.Session;
import grakn.client.Grakn.Transaction;
import grakn.client.rpc.RequestBuilder;
import grakn.protocol.GraknGrpc;
import grakn.protocol.SessionProto;
import io.grpc.ManagedChannel;

public class GraknSession implements Session {

    protected ManagedChannel channel;
    private String username; // TODO: Do we need to save this? It's not used.
    private String password; // TODO: Do we need to save this? It's not used.
    protected String databaseName;
    protected GraknGrpc.GraknBlockingStub sessionStub;
    protected ByteString sessionId;
    protected boolean isOpen;

    GraknSession(ManagedChannel channel, String username, String password, String databaseName, Session.Type type) {
        this.username = username;
        this.password = password;
        this.databaseName = databaseName;
        this.channel = channel;
        this.sessionStub = GraknGrpc.newBlockingStub(channel);

        SessionProto.Session.Open.Req.Builder openReq = RequestBuilder.Session.open(databaseName).newBuilderForType();
        openReq.setDatabase(databaseName);

        switch (type) {
            case DATA:
                openReq.setType(SessionProto.Session.Type.DATA);
                break;
            case SCHEMA:
                openReq.setType(SessionProto.Session.Type.SCHEMA);
                break;
            default:
                openReq.setType(SessionProto.Session.Type.UNRECOGNIZED);
        }

        SessionProto.Session.Open.Res response = sessionStub.sessionOpen(openReq.build());
        sessionId = response.getSessionID();
        isOpen = true;
    }

    @Override
    public Transaction.Builder transaction() {
        return new GraknTransaction.Builder(channel, this, sessionId);
    }

    @Override
    public Transaction transaction(Transaction.Type type) {
        return new GraknTransaction(channel, this, sessionId, type);
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    public void close() {
        if (!isOpen) return;
        sessionStub.sessionClose(RequestBuilder.Session.close(sessionId));
        isOpen = false;
    }

    @Override
    public Database database() {
        return Database.of(databaseName);
    }
}
