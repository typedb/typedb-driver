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

using System;
using TypeDB.Driver.Common;

namespace TypeDB.Driver.Api
{
    /// <summary>
    /// This enum is used to specify the replication role of a server.
    /// </summary>
    /// <example>
    /// <code>
    /// server.Role;
    /// </code>
    /// </example>
    public enum ReplicationRole
    {
        Primary = 0,
        Candidate = 1,
        Secondary = 2,
    }

    public static class ReplicationRoleExtensions
    {
        public static ReplicationRole Of(Pinvoke.ReplicationRole nativeRole)
        {
            if (nativeRole == Pinvoke.ReplicationRole.Primary) return ReplicationRole.Primary;
            if (nativeRole == Pinvoke.ReplicationRole.Candidate) return ReplicationRole.Candidate;
            if (nativeRole == Pinvoke.ReplicationRole.Secondary) return ReplicationRole.Secondary;
            throw new TypeDBDriverException("Unexpected native ReplicationRole value");
        }

        /// <summary>
        /// Checks whether this is the primary server of the raft cluster.
        /// </summary>
        public static bool IsPrimary(this ReplicationRole role) => role == ReplicationRole.Primary;

        /// <summary>
        /// Checks whether this is a candidate server of the raft cluster.
        /// </summary>
        public static bool IsCandidate(this ReplicationRole role) => role == ReplicationRole.Candidate;

        /// <summary>
        /// Checks whether this is a secondary server of the raft cluster.
        /// </summary>
        public static bool IsSecondary(this ReplicationRole role) => role == ReplicationRole.Secondary;
    }
}
