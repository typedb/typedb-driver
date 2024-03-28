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

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Common;
using TypeDB.Driver.Common.Validation;

using DriverError = TypeDB.Driver.Common.Error.Driver;
using ConceptError = TypeDB.Driver.Common.Error.Concept;

namespace TypeDB.Driver.Logic
{
    public class Rule : NativeObjectWrapper<Pinvoke.Rule>, IRule
    {
        private string? _label;
        private int _hash = 0;

        internal Rule(Pinvoke.Rule nativeRule)
            : base(nativeRule)
        {
            When = Pinvoke.typedb_driver.rule_get_when(NativeObject);
            Then = Pinvoke.typedb_driver.rule_get_then(NativeObject);
        }

        public string Label
        {
            get { return _label ?? (_label = Pinvoke.typedb_driver.rule_get_label(NativeObject)); }
        }

        public string When { get; }

        public string Then { get; }

        public VoidPromise SetLabel(ITypeDBTransaction transaction, string label)
        {
            Validator.NonEmptyString(label, ConceptError.MISSING_LABEL);

            return new VoidPromise(Pinvoke.typedb_driver.rule_set_label(
                NativeTransaction(transaction), NativeObject, label).Resolve);
        }

        public VoidPromise Delete(ITypeDBTransaction transaction)
        {
            return new VoidPromise(Pinvoke.typedb_driver.rule_delete(
                NativeTransaction(transaction), NativeObject).Resolve);
        }

        public Promise<bool> IsDeleted(ITypeDBTransaction transaction)
        {
            return new Promise<bool>(Pinvoke.typedb_driver.rule_is_deleted(
                NativeTransaction(transaction), NativeObject).Resolve);
        }

        private static Pinvoke.Transaction NativeTransaction(ITypeDBTransaction transaction)
        {
            Pinvoke.Transaction nativeTransaction = ((LogicManager)transaction.Logic).NativeTransaction;
            Validator.ThrowIfFalse(nativeTransaction.IsOwned, DriverError.TRANSACTION_CLOSED);

            return nativeTransaction;
        }

        public override string ToString()
        {
            return Pinvoke.typedb_driver.rule_to_string(NativeObject);
        }

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

            Rule that = (Rule)obj;

            return Label.Equals(that.Label);
        }

        public override int GetHashCode()
        {
            if (_hash == 0)
            {
                _hash = Label.GetHashCode();
            }

            return _hash;
        }
    }
}
