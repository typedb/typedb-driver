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

package grakn.client.common.exception;

public class GraknConceptException extends RuntimeException {
    final static String INVALID_OBJECT_TYPE = "The concept [%s] is not of type [%s]";
    final static String VARIABLE_DOES_NOT_EXIST = "the variable [%s] does not exist";

    private GraknConceptException(String error) {
        super(error);
    }

    public String getName() {
        return this.getClass().getName();
    }

    public static GraknConceptException create(String error) {
        return new GraknConceptException(error);
    }

    /**
     * Thrown when casting Grakn concepts/answers incorrectly.
     */
    public static GraknConceptException invalidCasting(Object concept, Class type) {
        return GraknConceptException.create(String.format(INVALID_OBJECT_TYPE,concept, type));
    }

    public static GraknConceptException variableDoesNotExist(String var) {
        return new GraknConceptException(String.format(VARIABLE_DOES_NOT_EXIST, var));
    }
}
