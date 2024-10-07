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

package com.typedb.driver.common;

import com.typedb.driver.common.exception.ErrorMessage;
import com.typedb.driver.common.exception.TypeDBDriverException;

import java.util.Objects;

public class Duration {
    private static final int YEARS = 0;
    private static final int MONTHS_IN_YEAR = 12;

    private final java.time.Period datePart;
    private final java.time.Duration timePart;
    private int hash = 0;

    /**
     * @hidden
     */
    public Duration(com.typedb.driver.jni.Duration nativeDuration) {
        if (nativeDuration == null) throw new TypeDBDriverException(ErrorMessage.Internal.NULL_NATIVE_VALUE);
        this.datePart = java.time.Period.of(YEARS, (int) nativeDuration.getMonths(), (int) nativeDuration.getDays());
        this.timePart = java.time.Duration.ofNanos(nativeDuration.getNanos().longValue());
    }

    /**
     * @hidden
     */
    public Duration(java.time.Period datePart, java.time.Duration timePart) {
        this.datePart = java.time.Period.of(YEARS, datePart.getYears() * MONTHS_IN_YEAR + datePart.getMonths(), datePart.getDays());
        this.timePart = timePart;
    }

    /**
     * Returns the date part of this duration.
     *
     * <h3>Examples</h3>
     * <pre>
     * duration.getDatePart();
     * </pre>
     */
    public java.time.Period getDatePart() {
        return datePart;
    }

    /**
     * Returns the time part of this duration.
     *
     * <h3>Examples</h3>
     * <pre>
     * duration.getTimePart();
     * </pre>
     */
    public java.time.Duration getTimePart() {
        return timePart;
    }

    /**
     * Returns the string representation of the duration.
     *
     * <h3>Examples</h3>
     * <pre>
     * duration.toString();
     * </pre>
     */
    @Override
    public String toString() {
        return datePart.toString() + " " + timePart.toString();
    }

    /**
     * Checks if this Duration is equal to another object.
     *
     * <h3>Examples</h3>
     * <pre>
     * label.equals(obj);
     * </pre>
     *
     * @param obj Object to compare with
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Duration that = (Duration) obj;
        return getDatePart().equals(that.getDatePart()) && getTimePart().equals(that.getTimePart());
    }

    /**
     * @hidden
     */
    @Override
    public int hashCode() {
        if (hash == 0) hash = computeHash();
        return hash;
    }

    /**
     * @hidden
     */
    private int computeHash() {
        return Objects.hash(getDatePart(), getTimePart());
    }
}
