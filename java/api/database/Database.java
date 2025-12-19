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

package com.typedb.driver.api.database;

import com.typedb.driver.api.ConsistencyLevel;
import com.typedb.driver.common.exception.TypeDBDriverException;

import javax.annotation.CheckReturnValue;

public interface Database {

    /**
     * The database name as a string.
     *
     * <h3>Examples</h3>
     * <pre>
     * database.name()
     * </pre>
     */
    @CheckReturnValue
    String name();

    /**
     * A full schema text as a valid TypeQL define query string, using default strong consistency.
     * See {@link #schema(ConsistencyLevel)} for more details and options.
     *
     * <h3>Examples</h3>
     * <pre>
     * database.schema()
     * </pre>
     */
    @CheckReturnValue
    default String schema() throws TypeDBDriverException {
        return schema(null);
    }

    /**
     * A full schema text as a valid TypeQL define query string.
     *
     * <h3>Examples</h3>
     * <pre>
     * database.schema(ConsistencyLevel.Strong)
     * </pre>
     *
     * @param consistencyLevel The consistency level to use for the operation
     */
    @CheckReturnValue
    String schema(ConsistencyLevel consistencyLevel) throws TypeDBDriverException;

    /**
     * The types in the schema as a valid TypeQL define query string, using default strong consistency.
     * See {@link #typeSchema(ConsistencyLevel)} for more details and options.
     *
     * <h3>Examples</h3>
     * <pre>
     * database.typeSchema()
     * </pre>
     */
    @CheckReturnValue
    default String typeSchema() throws TypeDBDriverException {
        return typeSchema(null);
    }

    /**
     * The types in the schema as a valid TypeQL define query string.
     *
     * <h3>Examples</h3>
     * <pre>
     * database.typeSchema(ConsistencyLevel.Strong)
     * </pre>
     *
     * @param consistencyLevel The consistency level to use for the operation
     */
    @CheckReturnValue
    String typeSchema(ConsistencyLevel consistencyLevel) throws TypeDBDriverException;

    /**
     * Export a database into a schema definition and a data files saved to the disk, using default strong consistency.
     * This is a blocking operation and may take a significant amount of time depending on the database size.
     * See {@link #exportToFile(String, String, ConsistencyLevel)} for more details and options.
     *
     * <h3>Examples</h3>
     * <pre>
     * database.exportToFile("schema.typeql", "data.typedb")
     * </pre>
     *
     * @param schemaFilePath The path to the schema definition file to be created
     * @param dataFilePath   The path to the data file to be created
     */
    default void exportToFile(String schemaFilePath, String dataFilePath) throws TypeDBDriverException {
        exportToFile(schemaFilePath, dataFilePath, null);
    }

    /**
     * Export a database into a schema definition and a data files saved to the disk.
     * This is a blocking operation and may take a significant amount of time depending on the database size.
     *
     * <h3>Examples</h3>
     * <pre>
     * database.exportToFile("schema.typeql", "data.typedb", ConsistencyLevel.Strong)
     * </pre>
     *
     * @param schemaFilePath   The path to the schema definition file to be created
     * @param dataFilePath     The path to the data file to be created
     * @param consistencyLevel The consistency level to use for the operation
     */
    void exportToFile(String schemaFilePath, String dataFilePath, ConsistencyLevel consistencyLevel) throws TypeDBDriverException;

    /**
     * Deletes this database, using default strong consistency.
     * See {@link #delete(ConsistencyLevel)} for more details and options.
     *
     * <h3>Examples</h3>
     * <pre>
     * database.delete()
     * </pre>
     */
    default void delete() throws TypeDBDriverException {
        delete(null);
    }

    /**
     * Deletes this database.
     *
     * <h3>Examples</h3>
     * <pre>
     * database.delete(ConsistencyLevel.Strong)
     * </pre>
     *
     * @param consistencyLevel The consistency level to use for the operation
     */
    void delete(ConsistencyLevel consistencyLevel) throws TypeDBDriverException;
}
