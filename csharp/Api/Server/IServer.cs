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

namespace TypeDB.Driver.Api
{
    /// <summary>
    /// The metadata and state of an individual raft server of a driver connection.
    /// </summary>
    public interface IServer
    {
        /// <summary>
        /// Returns the id of this server.
        /// </summary>
        long Id { get; }

        /// <summary>
        /// Returns the address this server is hosted at.
        /// </summary>
        string Address { get; }

        /// <summary>
        /// Returns whether this is the primary server of the raft cluster or any of the supporting roles.
        /// </summary>
        ReplicationRole? Role { get; }

        /// <summary>
        /// Checks whether this is the primary server of the raft cluster.
        /// </summary>
        bool IsPrimary { get; }

        /// <summary>
        /// Returns the raft protocol 'term' of this server.
        /// </summary>
        long? Term { get; }
    }
}
