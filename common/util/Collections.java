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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Collections {

    @SafeVarargs
    public static <K, V> Map<K, V> map(Tuple<K, V>... tuples) {
        Map<K, V> map = new HashMap<>();
        for(Tuple<K, V> tuple : tuples) {
            map.put(tuple.first(), tuple.second());
        }
        return java.util.Collections.unmodifiableMap(map);
    }

    @SafeVarargs
    public static <T> Set<T> set(T... elements) {
        return set(Arrays.asList(elements));
    }

    public static <T> Set<T> set(Collection<T> elements) {
        Set<T> set = new HashSet<>(elements);
        return java.util.Collections.unmodifiableSet(set);
    }

    @SafeVarargs
    public static <T> List<T> list(T... elements) {
        return list(Arrays.asList(elements));
    }

    public static <T> List<T> list(Collection<T> elements) {
        List<T> list = new ArrayList<>(elements);
        return java.util.Collections.unmodifiableList(list);
    }

    public static <A, B> Tuple<A, B> tuple(A first, B second) {
        return new Tuple<>(first, second);
    }

    public static <A, B, C> Triple<A, B, C> triple(A first, B second, C third) {
        return new Triple<>(first, second, third);
    }
}
