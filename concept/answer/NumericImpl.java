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

import grakn.client.api.answer.Numeric;
import grakn.client.common.GraknClientException;
import grakn.protocol.AnswerProto;

import javax.annotation.Nullable;

import static grakn.client.common.ErrorMessage.Internal.ILLEGAL_CAST;
import static grakn.client.common.ErrorMessage.Query.BAD_ANSWER_TYPE;

public class NumericImpl implements Numeric {

    @Nullable
    private final Long longValue;
    @Nullable
    private final Double doubleValue;

    private NumericImpl(@Nullable Long longValue, @Nullable Double doubleValue) {
        this.longValue = longValue;
        this.doubleValue = doubleValue;
    }

    public static NumericImpl of(AnswerProto.Numeric numeric) {
        switch (numeric.getValueCase()) {
            case LONG_VALUE:
                return NumericImpl.ofLong(numeric.getLongValue());
            case DOUBLE_VALUE:
                return NumericImpl.ofDouble(numeric.getDoubleValue());
            case NAN:
                return NumericImpl.ofNaN();
            default:
                throw new GraknClientException(BAD_ANSWER_TYPE.message(numeric.getValueCase()));
        }
    }

    private static NumericImpl ofLong(long value) {
        return new NumericImpl(value, null);
    }

    private static NumericImpl ofDouble(double value) {
        return new NumericImpl(null, value);
    }

    private static NumericImpl ofNaN() {
        return new NumericImpl(null, null);
    }

    @Override
    public boolean isLong() {
        return longValue != null;
    }

    @Override
    public boolean isDouble() {
        return doubleValue != null;
    }

    @Override
    public boolean isNaN() {
        return !isLong() && !isDouble();
    }

    @Override
    public long asLong() {
        if (isLong()) return longValue;
        else throw new GraknClientException(ILLEGAL_CAST.message(Long.class));
    }

    @Override
    public Double asDouble() {
        if (isDouble()) return doubleValue;
        else throw new GraknClientException(ILLEGAL_CAST.message(Double.class));
    }

    @Override
    public Number asNumber() {
        if (isLong()) return longValue;
        else if (isDouble()) return doubleValue;
        else throw new GraknClientException(ILLEGAL_CAST.message(Number.class));
    }
}
