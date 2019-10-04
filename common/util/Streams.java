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

import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Common utility methods used within Grakn.
 *
 * Some of these methods are Grakn-specific, others add important "missing" methods to Java/Guava classes.
 *
 */
public class Streams {

    /**
     * @param optional the optional to change into a stream
     * @param <T> the type in the optional
     * @return a stream of one item if the optional has an element, else an empty stream
     */
    public static <T> Stream<T> optionalToStream(Optional<T> optional) {
        return optional.map(Stream::of).orElseGet(Stream::empty);
    }

    /**
     * Helper which lazily checks if a {@link Stream} contains the number specified
     * WARNING: This consumes the stream rendering it unusable afterwards
     *
     * @param stream the {@link Stream} to check the count against
     * @param size the expected number of elements in the stream
     * @return true if the expected size is found
     */
    public static boolean containsOnly(Stream stream, long size){
        long count = 0L;
        Iterator it = stream.iterator();

        while(it.hasNext()){
            it.next();
            if(++count > size) return false;
        }

        return size == count;
    }

}
