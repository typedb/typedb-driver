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

import com.typedb.driver.common.exception.TypeDBDriverException;

import javax.annotation.CheckReturnValue;

public interface Database {

    /**
     * The database name as a string.
     */
    @CheckReturnValue
    String name();

    /**
     * A full schema text as a valid TypeQL define query string.
     *
     * <h3>Examples</h3>
     * <pre>
     * database.schema()
     * </pre>
     */
    @CheckReturnValue
    String schema() throws TypeDBDriverException;

    /**
     * The types in the schema as a valid TypeQL define query string.
     *
     * <h3>Examples</h3>
     * <pre>
     * database.typeSchema()
     * </pre>
     */
    @CheckReturnValue
    String typeSchema() throws TypeDBDriverException;

    /**
     * Export a database into a schema definition and a data files saved to the disk.
     * This is a blocking operation and may take a significant amount of time depending on the database size.
     *
     * <h3>Examples</h3>
     * <pre>
     * database.exportToFile("schema.typeql", "data.typedb")
     * </pre>
     *
     * @param schemaFilePath The path to the schema definition file to be created
     * @param dataFilePath   The path to the data file to be created
     */
    void exportToFile(String schemaFilePath, String dataFilePath) throws TypeDBDriverException;

    /**
     * Deletes this database.
     *
     * <h3>Examples</h3>
     * <pre>
     * database.delete()
     * </pre>
     */
    void delete() throws TypeDBDriverException;
}
