package grakn.client.connection;

import grakn.client.common.exception.GraknClientException;
import grakn.common.parameters.Options;
import grakn.protocol.OptionsProto;
import grakn.protocol.TransactionProto;

import static grakn.client.common.exception.ErrorMessage.Connection.NEGATIVE_BATCH_SIZE;

public abstract class ConnectionMessageWriter {

    static OptionsProto.Options options(Options<?, ?> options) {
        final OptionsProto.Options.Builder builder = OptionsProto.Options.newBuilder();
        if (options.explain() != null) {
            builder.setExplain(options.explain());
        }
        if (options.infer() != null) {
            builder.setInfer(options.infer());
        }
        if (options.batchSize() != null) {
            builder.setBatchSize(options.batchSize());
        }
        return builder.build();
    }

    static TransactionProto.Transaction.Iter.Req.Options batchSize(int size) {
        if (size < 1) {
            throw new GraknClientException(NEGATIVE_BATCH_SIZE.message(size));
        }
        return TransactionProto.Transaction.Iter.Req.Options.newBuilder()
                .setNumber(size).build();
    }

    static TransactionProto.Transaction.Iter.Req.Options batchSizeAll() {
        return TransactionProto.Transaction.Iter.Req.Options.newBuilder()
                .setAll(true).build();
    }
}
