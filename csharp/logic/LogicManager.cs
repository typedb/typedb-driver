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

using System.Collections.Generic;

using Vaticle.Typedb.Driver.Api;
using Vaticle.Typedb.Driver.Common;
using Vaticle.Typedb.Driver.Util;

using DriverError = Vaticle.Typedb.Driver.Common.Error.Driver;
using ConceptError = Vaticle.Typedb.Driver.Common.Error.Concept;

namespace Vaticle.Typedb.Driver.Logic
{
    public class LogicManager : ILogicManager
    {
        private Pinvoke.Transaction _nativeTransaction { get; }

        public LogicManager(Pinvoke.Transaction nativeTransaction)
        {
            _nativeTransaction = nativeTransaction;
        }

        public IEnumerable<IRule> Rules
        {
            get
            {
                if (!nativeTransaction.IsOwned())
                {
                    throw new TypeDBDriverException(DriverError.TRANSACTION_CLOSED);
                }

                try
                {
                    return new NativeEnumerable<Pinvoke.Rule>(
                        Pinvoke.typedb_driver.logic_manager_get_rules(_nativeTransaction))
                        .Select(obj => new Rule(obj));
                }
                catch (Pinvoke.Error e)
                {
                    throw new TypeDBDriverException(e);
                }
            }
        }

        public Promise<IRule> GetRule(string label)
        {
            InputChecker.NonEmptyString(label, ConceptError.MISSING_LABEL);
            if (!_nativeTransaction.IsOwned())
            {
                throw new TypeDBDriverException(DriverError.TRANSACTION_CLOSED);
            }

            return Promise<IRule>.Map<IRule, Pinvoke.Concept>(
                Pinvoke.typedb_driver.logic_manager_get_rule(
                    _nativeTransaction, label).Resolve,
                obj => new Rule(obj));
        }

        public Promise<IRule> PutRule(string label, string when, string then)
        {
            InputChecker.NonEmptyString(label, ConceptError.MISSING_LABEL);
            if (!_nativeTransaction.IsOwned())
            {
                throw new TypeDBDriverException(DriverError.TRANSACTION_CLOSED);
            }

            Pinvoke.RulePromise promise =
                Pinvoke.typedb_driver.logic_manager_put_rule(_nativeTransaction, label, when, then);
            return new Promise<IRule>(() => new Rule(promise.Resolve()));
        }
    }
}
