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

import java.time.format.DateTimeParseException;
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
     * Parses a <code>Duration</code> object from a string in ISO 8601 format. Throws java.time exceptions
     *
     * <h3>Examples</h3>
     * <pre>
     *     Duration.parse("P1Y10M7DT15H44M5.00394892S");
     *     Duration.parse("P55W");
     * </pre>
     *
     * @param durationString A string representation of the duration. Expected format: PnYnMnDTnHnMnS or PnW.
     * @throws DateTimeParseException if the text cannot be parsed to a <code>Duration</code>.
     */
    public static Duration parse(String durationString) {
        String[] durationParts = durationString.split("T");
        java.time.Period period = java.time.Period.parse(durationParts[0]);
        if (durationParts.length == 1) {
            return new Duration(period, java.time.Duration.ofNanos(0));
        } else {
            return new Duration(period, java.time.Duration.parse("PT" + durationParts[1]));
        }
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
     * Returns the amount of months of this <code>Duration</code> from the date part.
     *
     * <h3>Examples</h3>
     * <pre>
     * duration.getMonths();
     * </pre>
     */
    public int getMonths() {
        return datePart.getMonths();
    }

    /**
     * Returns the amount of days of this <code>Duration</code> from the date part.
     *
     * <h3>Examples</h3>
     * <pre>
     * duration.getMonths();
     * </pre>
     */
    public int getDays() {
        return datePart.getDays();
    }

    /**
     * Returns the amount of seconds of this <code>Duration</code> from the time part.
     *
     * <h3>Examples</h3>
     * <pre>
     * duration.getSeconds();
     * </pre>
     */
    public long getSeconds() {
        return timePart.getSeconds();
    }

    /**
     * Returns the number of nanoseconds within the second in this <code>Duration</code> from the time part.
     *
     * <h3>Examples</h3>
     * <pre>
     * duration.getNano();
     * </pre>
     */
    public long getNano() {
        return timePart.getNano();
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
