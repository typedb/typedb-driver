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
package grakn.client.concept.api;

import javax.annotation.CheckReturnValue;
import java.io.Serializable;

/**
 * A class which represents an id of any Concept.
 * Also contains a static method for producing concept IDs from Strings.
 */
public class ConceptId implements Comparable<ConceptId>, Serializable {

    private static final long serialVersionUID = -1723590529071614152L;
    private final String value;

    /**
     * A non-argument constructor for ConceptID, for serialisation of OLAP queries dependencies
     */
    ConceptId() {
        this.value = null;
    }

    /**
     * The default constructor for ConceptID, which requires String value provided
     *
     * @param value String representation of the Concept ID
     */
    ConceptId(String value) {
        if (value == null) throw new NullPointerException("Provided ConceptId is NULL");

        this.value = value;
    }

    /**
     * @param value The string which potentially represents a Concept
     * @return The matching concept ID
     */
    @CheckReturnValue
    public static ConceptId of(String value) {
        return new ConceptId(value);
    }

    /**
     * @return Used for indexing purposes and for graql traversals
     */
    @CheckReturnValue
    public String getValue() {
        return value;
    }

    @Override
    public int compareTo(ConceptId o) {
        return getValue().compareTo(o.getValue());
    }

    @Override
    public final String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        ConceptId that = (ConceptId) o;
        return (this.value.equals(that.getValue()));
    }

    @Override
    public int hashCode() {
        int result = 31 * this.value.hashCode();
        return result;
    }
}
