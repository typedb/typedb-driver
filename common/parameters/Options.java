package grakn.client.common.parameters;

public abstract class Options {

    public static final boolean DEFAULT_INFER = true;
    public static final boolean DEFAULT_EXPLAIN = false;
    public static final int DEFAULT_BATCH_SIZE = 50;

    private Boolean infer = null;
    private Boolean explain = null;
    private Integer batchSize = null;

    public Boolean infer() {
        if (infer != null) {
            return infer;
        } else {
            return DEFAULT_INFER;
        }
    }

    public Options infer(boolean infer) {
        this.infer = infer;
        return this;
    }

    public Boolean explain() {
        if (explain != null) {
            return explain;
        } else {
            return DEFAULT_EXPLAIN;
        }
    }

    public Options explain(boolean explain) {
        this.explain = explain;
        return this;
    }

    public Integer batchSize() {
        if (batchSize != null) {
            return batchSize;
        } else {
            return DEFAULT_BATCH_SIZE;
        }
    }

    public Options batchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public static class Session extends Options {
    }

    public static class Transaction extends Options {
    }

    public static class Query extends Options {
    }
}
