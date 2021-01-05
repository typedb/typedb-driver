/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package grakn.client.concept.answer;

import grakn.client.common.exception.GraknClientException;
import grakn.protocol.AnswerProto;

import javax.annotation.Nullable;

import static grakn.client.common.exception.ErrorMessage.Query.ILLEGAL_CAST;

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
            case NAN:
                return Numeric.ofNaN();
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

    private static Numeric ofNaN() {
        return new Numeric(null, null);
    }

    public boolean isLong() {
        return longValue != null;
    }

    public boolean isDouble() {
        return doubleValue != null;
    }

    public boolean isNaN() {
        return !isLong() && !isDouble();
    }

    public long asLong() {
        if (isLong()) return longValue;
        else throw new GraknClientException(ILLEGAL_CAST.message(Long.class));
    }

    public Double asDouble() {
        if (isDouble()) return doubleValue;
        else throw new GraknClientException(ILLEGAL_CAST.message(Double.class));
    }

    public Number asNumber() {
        if (isLong()) return longValue;
        else if (isDouble()) return doubleValue;
        else throw new GraknClientException(ILLEGAL_CAST.message(Number.class));
    }
}
