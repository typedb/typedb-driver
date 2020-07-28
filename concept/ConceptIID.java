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
package grakn.client.concept;

import com.google.protobuf.ByteString;

import javax.annotation.CheckReturnValue;
import java.io.Serializable;

/**
 * A class which represents an iid of any Concept.
 * Also contains a static method for producing concept iids from Strings.
 */
public class ConceptIID implements Serializable {

    private static final long serialVersionUID = -1723590529071614152L;
    private final ByteString value;
    private String toStringCached;

    /**
     * A non-argument constructor for ConceptID, for serialisation of OLAP queries dependencies
     */
    ConceptIID() {
        this.value = null;
    }

    /**
     * The default constructor for ConceptID, which requires String value provided
     *
     * @param value String representation of the Concept IID
     */
    ConceptIID(ByteString value) {
        if (value == null) throw new NullPointerException("Provided ConceptId is NULL");

        this.value = value;
    }

    /**
     * @param value The string which potentially represents a Concept
     * @return The matching concept IID
     */
    @CheckReturnValue
    public static ConceptIID of(ByteString value) {
        return new ConceptIID(value);
    }

    /**
     * @return Used for indexing purposes and for graql traversals
     */
    @CheckReturnValue
    public ByteString getValue() {
        return value;
    }

    @Override
    public final String toString() {
        if (toStringCached == null) {
            toStringCached = value != null ? bytesToHex(value) : "null";
        }
        return toStringCached;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        ConceptIID that = (ConceptIID) o;
        return (this.value.equals(that.getValue()));
    }

    @Override
    public int hashCode() {
        int result = 31 * this.value.hashCode();
        return result;
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private static String bytesToHex(ByteString bytes) {
        final int bytesLength = bytes.size();
        char[] hexChars = new char[bytesLength * 2];
        for (int j = 0; j < bytesLength; j++) {
            int v = bytes.byteAt(j) & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}
