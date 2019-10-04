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

package grakn.client.common.util;

import java.util.Objects;

public class Triple<A, B, C> {

    private A first;
    private B second;
    private C third;

    public Triple(A first, B second, C third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public A first() {
        return first;
    }

    public B second() {
        return second;
    }

    public C third() {
        return third;
    }

    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;

        Triple<?, ?, ?> other = (Triple) obj;
        return (Objects.equals(this.first, other.first) &&
                    Objects.equals(this.second, other.second) &&
                    Objects.equals(this.third, other.third));
    }

    public int hashCode() {
        int h = 1;
        h *= 1000003;
        h ^= this.first.hashCode();
        h *= 1000003;
        h ^= this.second.hashCode();
        h *= 1000003;
        h ^= this.third.hashCode();

        return h;
    }
}
