package grakn.client.concept.answer;

import grakn.client.common.exception.GraknClientException;
import grakn.protocol.AnswerProto;

import javax.annotation.Nullable;

public class Numeric {
    @Nullable
    private final Long longValue;
    @Nullable
    private final Double doubleValue;

    private Numeric(@Nullable Long longValue, @Nullable Double doubleValue) {
        this.longValue = longValue;
        this.doubleValue = doubleValue;
    }

    public static Numeric of(AnswerProto.Numeric numeric) {
        switch (numeric.getValueCase()) {
            case LONG_VALUE:
                return Numeric.ofLong(numeric.getLongValue());
            case DOUBLE_VALUE:
                return Numeric.ofDouble(numeric.getDoubleValue());
            case NAN_VALUE:
                return Numeric.ofNan();
            default:
                throw new GraknClientException("TODO");
        }
    }

    private static Numeric ofLong(long value) {
        return new Numeric(value, null);
    }

    private static Numeric ofDouble(double value) {
        return new Numeric(null, value);
    }

    private static Numeric ofNan() {
        return new Numeric(null, null);
    }

    public boolean isLong() {
        return longValue != null;
    }

    public boolean isDouble() {
        return doubleValue != null;
    }

    public boolean isNan() {
        return !isLong() && !isDouble();
    }

    public long asLong() {
        if (isLong()) return longValue;
        else throw new GraknClientException("TODO");
    }

    public Double asDouble() {
        if (isDouble()) return doubleValue;
        else throw new GraknClientException("TODO");
    }

    public Number asNumber() {
        if (isLong()) return longValue;
        else if (isDouble()) return doubleValue;
        else throw new GraknClientException("TODO");
    }
}
