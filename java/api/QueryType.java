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

package com.typedb.driver.api;

import com.typedb.driver.common.exception.TypeDBDriverException;

import static com.typedb.driver.common.exception.ErrorMessage.Internal.UNEXPECTED_NATIVE_VALUE;

/**
 * Used to specify the type of the executed query.
 *
 * <h3>Examples</h3>
 * <pre>
 * conceptRow.queryType();
 * </pre>
 */
public enum QueryType {
    READ(0, com.typedb.driver.jni.QueryType.ReadQuery),
    WRITE(1, com.typedb.driver.jni.QueryType.WriteQuery),
    SCHEMA(2, com.typedb.driver.jni.QueryType.SchemaQuery);

    private final int id;
    public final com.typedb.driver.jni.QueryType nativeObject;

    QueryType(int id, com.typedb.driver.jni.QueryType nativeObject) {
        this.id = id;
        this.nativeObject = nativeObject;
    }

    public static QueryType of(com.typedb.driver.jni.QueryType nativeType) {
        if (nativeType == com.typedb.driver.jni.QueryType.ReadQuery) return READ;
        else if (nativeType == com.typedb.driver.jni.QueryType.WriteQuery) return WRITE;
        else if (nativeType == com.typedb.driver.jni.QueryType.SchemaQuery) return SCHEMA;
        throw new TypeDBDriverException(UNEXPECTED_NATIVE_VALUE);
    }

    public int id() {
        return id;
    }

    public boolean isRead() {
        return nativeObject == com.typedb.driver.jni.QueryType.ReadQuery;
    }

    public boolean isWrite() {
        return nativeObject == com.typedb.driver.jni.QueryType.WriteQuery;
    }

    public boolean isSchema() {
        return nativeObject == com.typedb.driver.jni.QueryType.SchemaQuery;
    }
}
