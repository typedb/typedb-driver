package grakn.client;

import grakn.client.common.exception.GraknClientException;

import java.util.Optional;

import static grakn.client.common.exception.ErrorMessage.Client.NEGATIVE_BATCH_SIZE;
import static grakn.client.common.exception.ErrorMessage.Internal.ILLEGAL_CAST;

public abstract class GraknOptions {
    private Boolean infer = null;
    private Boolean explain = null;
    private Integer batchSize = null;

    public Optional<Boolean> infer() {
        return Optional.ofNullable(infer);
    }

    public GraknOptions infer(boolean infer) {
        this.infer = infer;
        return this;
    }

    public Optional<Boolean> explain() {
        return Optional.ofNullable(explain);
    }

    public GraknOptions explain(boolean explain) {
        this.explain = explain;
        return this;
    }

    public Optional<Integer> batchSize() {
        return Optional.ofNullable(batchSize);
    }

    public GraknOptions batchSize(int batchSize) {
        if (batchSize < 1) {
            throw new GraknClientException(NEGATIVE_BATCH_SIZE.message(batchSize));
        }
        this.batchSize = batchSize;
        return this;
    }

    public static GraknOptions core() {
        return GraknOptions.core();
    }

    public static GraknOptions.Cluster cluster() {
        return GraknOptions.cluster();
    }

    abstract boolean isCluster();

    Cluster asCluster() {
        if (isCluster()) return (Cluster) this;
        else throw new GraknClientException(ILLEGAL_CAST, Cluster.class);
    }

    public class Cluster extends GraknOptions {
        private Boolean primaryReplica = null;

        private Cluster() {}

        public Optional<Boolean> primaryReplica() {
            return Optional.ofNullable(primaryReplica);
        }

        public Cluster primaryReplica(boolean primaryReplica) {
            this.primaryReplica = primaryReplica;
            return this;
        }

        @Override
        public boolean isCluster() {
            return true;
        }
    }
}
