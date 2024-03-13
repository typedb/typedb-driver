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

using System;

namespace TypeDB.Driver.Common
{
    /**
     * A <code>Label</code> holds the uniquely identifying name of a type.
     * <p>It consists of an optional <code>scope</code>, and a <code>name</code>, represented <code>scope:name</code>.
     * The scope is used only used to distinguish between role-types of the same name declared in different
     * relation types.</p>
     */
    public struct Label
    {
        /**
         * Returns the scope of this Label.
         *
         * <h3>Examples</h3>
         * <pre>
         * label.Scope;
         * </pre>
         */
        public readonly string? Scope;

        /**
         * Returns the name of this Label.
         *
         * <h3>Examples</h3>
         * <pre>
         * label.Name;
         * </pre>
         */
        public readonly string Name;

        /**
         * @hidden
         */
        private readonly int _hash;

        /**
         * Creates a Label from a specified scope and name.
         *
         * <h3>Examples</h3>
         * <pre>
         * new Label("relation", "role");
         * </pre>
         *
         * @param scope Label scope
         * @param name Label name
         */
        public Label(string? scope, string name)
        {
            Scope = scope;
            Name = name;
            _hash = (Name, Scope).GetHashCode();
        }

        /**
         * Creates a Label from a specified name.
         *
         * <h3>Examples</h3>
         * <pre>
         * new Label("entity");
         * </pre>
         *
         * @param name Label name
         */
        public Label(string name)
            : this(null, name)
        {
        }

        /**
         * Returns the string representation of the scoped name.
         *
         * <h3>Examples</h3>
         * <pre>
         * label.ScopedName;
         * </pre>
         */
        public string ScopedName
        {
            get => Scope == null ? Name : Scope + ":" + Name;
        }

        /**
         * Returns the string representation of the scoped name.
         *
         * <h3>Examples</h3>
         * <pre>
         * label.ToString();
         * </pre>
         */
        public override string ToString()
        {
            return ScopedName;
        }

        /**
         * Checks if this Label is equal to another object.
         *
         * <h3>Examples</h3>
         * <pre>
         * label.Equals(obj);
         * </pre>
         *
         * @param obj Object to compare with
         */
        public override bool Equals(object? obj)
        {
            if (Object.ReferenceEquals(this, obj))
            {
                return true;
            }

            if (obj == null || this.GetType() != obj.GetType())
            {
                return false;
            }

            Label that = (Label)obj;

            return this.Name == that.Name && this.Scope == that.Scope;
        }

        /**
         * @hidden
         */
        public override int GetHashCode()
        {
            return _hash;
        }
    }
}
