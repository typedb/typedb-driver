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

namespace com.vaticle.typedb.driver.Common
{
    /**
     * A <code>Label</code> holds the uniquely identifying name of a type.
     * <p>It consists of an optional <code>scope</code>, and a <code>name</code>, represented <code>scope:name</code>.
     * The scope is used only used to distinguish between role-types of the same name declared in different
     * relation types.</p>
     */
    public class Label
    {
        private readonly string? _scope;
        private readonly string _name;
        private readonly int _hash;

        private Label(string? scope, string name)
        {
            _scope = scope;
            _name = name;
            _hash = 0; // TODO: Objects.hash(name, scope);
        }

        /**
         * Creates a Label from a specified name.
         *
         * <h3>Examples</h3>
         * <pre>
         * Label.Of("entity");
         * </pre>
         *
         * @param name Label name
         */
        public static Label Of(string name)
        {
            return new Label(null, name);
        }

        /**
         * Creates a Label from a specified scope and name.
         *
         * <h3>Examples</h3>
         * <pre>
         * Label.Of("relation", "role");
         * </pre>
         *
         * @param scope Label scope
         * @param name Label name
         */
        public static Label of(string scope, string name)
        {
            return new Label(scope, name);
        }

        /**
         * Returns the scope of this Label.
         *
         * <h3>Examples</h3>
         * <pre>
         * label.Scope();
         * </pre>
         */
        public string? Scope()
        {
            return _scope;
        }

        /**
         * Returns the name of this Label.
         *
         * <h3>Examples</h3>
         * <pre>
         * label.Name();
         * </pre>
         */
        public string Name()
        {
            return _name;
        }

        /**
         * Returns the string representation of the scoped name.
         *
         * <h3>Examples</h3>
         * <pre>
         * label.ScopedName();
         * </pre>
         */
        public string ScopedName()
        {
            if (_scope == null)
            {
                return _name;
            }

            return _scope + ":" + _name;
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
            return ScopedName();
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
         */ // TODO: Implement for C#
//        public override bool Equals(Object obj)
//        {
//            if (this == obj)
//            {
//                return true;
//            }
//
//            if (obj == null || getClass() != obj.getClass())
//            {
//                return false;
//            }
//
//            Label that = (Label)obj;
//
//            return this.name.equals(that.name) && Objects.equals(this.scope, that.scope);
//        }

        /**
         * @hidden
         */ //TODO:
//        public override int HashCode()
//        {
//            return _hash;
//        }
    }
}
