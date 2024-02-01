/*
 * Copyright (C) 2022 Vaticle
 *
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

#nullable enable

using System.Collections.Generic;

namespace com.vaticle.typedb.driver.Api.Database
{
    public interface IDatabase
    {
        /**
         * The database name as a string.
         *
         * <h3>Examples</h3>
         * <pre>
         * database.Name()
         * </pre>
         */
        public string Name();

        /**
         * A full schema text as a valid TypeQL define query string.
         *
         * <h3>Examples</h3>
         * <pre>
         * database.Schema()
         * </pre>
         */
        public string Schema();

        /**
         * The types in the schema as a valid TypeQL define query string.
         *
         * <h3>Examples</h3>
         * <pre>
         * database.TypeSchema()
         * </pre>
         */
        public string TypeSchema();

        /**
         * The rules in the schema as a valid TypeQL define query string.
         *
         * <h3>Examples</h3>
         * <pre>
         * database.RuleSchema()
         * </pre>
         */
        public string RuleSchema();

        /**
         * Deletes this database.
         *
         * <h3>Examples</h3>
         * <pre>
         * database.Delete()
         * </pre>
         */
        public void Delete();

        /**
         * Set of <code>Replica</code> instances for this database.
         * <b>Only works in TypeDB Cloud</b>
         *
         * <h3>Examples</h3>
         * <pre>
         * database.Replicas()
         * </pre>
         */
        public HashSet<IReplica> Replicas();

        /**
         * Returns the primary replica for this database.
         * _Only works in TypeDB Cloud_
         *
         * <h3>Examples</h3>
         * <pre>
         * database.PrimaryReplica()
         * </pre>
         */
        public IReplica? PrimaryReplica();

        /**
         * Returns the preferred replica for this database. Operations which can be run on any replica will prefer to use this replica.
         * _Only works in TypeDB Cloud_
         *
         * <h3>Examples</h3>
         * <pre>
         * database.PreferredReplica()
         * </pre>
         */
        public IReplica? PreferredReplica();

        /**
         * The metadata and state of an individual raft replica of a database.
         */
        public interface IReplica
        {
            /**
             * Retrieves the address of the server hosting this replica.
             */
            public string Address();

            /**
             * Checks whether this is the primary replica of the raft cluster.
             */
            public bool IsPrimary();

            /**
             * Checks whether this is the preferred replica of the raft cluster.
             * If true, Operations which can be run on any replica will prefer to use this replica.
             */
            public bool IsPreferred();

            /**
             * The raft protocol ‘term’ of this replica.
             */
            public long Term();
        }
    }
}
