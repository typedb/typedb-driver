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

package grakn.client.connection;

import grakn.client.Grakn.Database;

public class GraknDatabase implements Database {
    private static final long serialVersionUID = 2726154016735929123L;
    public static final String DEFAULT = "grakn";

    private final String name;

    public GraknDatabase(String name) {
        if (name == null) {
            throw new NullPointerException("Null name");
        }
        this.name = name;
    }

    @Override
    public String name() {
        return name;
    }

    public final String toString() {
        return name();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final GraknDatabase that = (GraknDatabase) o;
        return this.name.equals(that.name);
    }

    public int hashCode() {
        int h = 1;
        h *= 1000003;
        h ^= this.name.hashCode();
        return h;
    }
}
