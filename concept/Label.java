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

import javax.annotation.CheckReturnValue;
import java.io.Serializable;
import java.util.function.Function;

/**
 * A Label
 * A class which represents the unique label of any SchemaConcept
 * Also contains a static method for producing Labels from Strings.
 */
public class Label implements Comparable<Label>, Serializable {
    private static final long serialVersionUID = 2051578406740868932L;

    private final String value;

    private Label(String value) {
        if (value == null) {
            throw new NullPointerException("Null value");
        }
        this.value = value;
    }

    /**
     * @param value The string which potentially represents a Type
     * @return The matching Type Label
     */
    @CheckReturnValue
    public static Label of(String value) {
        return new Label(value);
    }

    public String getValue() {
        return value;
    }

    /**
     * Rename a Label (does not modify the original Label)
     *
     * @param mapper a function to apply to the underlying type label
     * @return the new type label
     */
    @CheckReturnValue
    public Label map(Function<String, String> mapper) {
        return Label.of(mapper.apply(getValue()));
    }

    @Override
    public int compareTo(Label o) {
        return getValue().compareTo(o.getValue());
    }

    @Override
    public final String toString() {
        return getValue();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof Label) {
            Label that = (Label) o;
            return (this.value.equals(that.getValue()));
        }
        return false;
    }

    @Override
    public int hashCode() {
        int h = 1;
        h *= 1000003;
        h ^= this.value.hashCode();
        return h;
    }
}
