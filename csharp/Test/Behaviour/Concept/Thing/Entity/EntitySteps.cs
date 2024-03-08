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

using DataTable = Gherkin.Ast.DataTable; // TODO Remove if not needed
using DocString = Gherkin.Ast.DocString; // TODO Remove if not needed
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Xunit;
using Xunit.Gherkin.Quick;

using Vaticle.Typedb.Driver;
using Vaticle.Typedb.Driver.Api;
using Vaticle.Typedb.Driver.Common;

namespace Vaticle.Typedb.Driver.Test.Behaviour
{
    public partial class BehaviourSteps
    {
        [When(@"{var} = entity\\( ?{type_label} ?) create new instance")]
        public void EntityTypeCreateNewInstance(string var, string typeLabel)
        {
            var entityType = SingleTransaction
                .Concepts
                .GetEntityType(typeLabel).Resolve()
                .create(SingleTransaction).Resolve();

            Put(var, entityType);
        }

        [When(@"entity\\( ?{type_label} ?) create new instance; throws exception")]
        public void EntityTypeCreateNewInstanceThrowsException(string typeLabel)
        {
            Assert.Throws<TypeDBDriverException>(() => SingleTransaction
                .Concepts
                .GetEntityType(typeLabel).Resolve()
                .create(SingleTransaction).Resolve());
        }

        [When(@"{var} = entity\\( ?{type_label} ?) create new instance with key\\( ?{type_label} ?): {int}")]
        public void EntityTypeCreateNewInstanceWithKey(string var, string type, string keyType, int keyValue)
        {
            Attribute key = SingleTransaction
                .Concepts
                .GetAttributeType(keyType).Resolve()
                .Put(SingleTransaction, keyValue).Resolve();

            Entity entity = SingleTransaction
                .Concepts
                .GetEntityType(type).Resolve()
                .create(SingleTransaction).Resolve();

            entity.SetHas(SingleTransaction, key).Resolve();

            Put(var, entity);
        }

        [When(@"{var} = entity\\( ?{type_label} ?) create new instance with key\\( ?{type_label} ?): {word}")]
        public void EntityTypeCreateNewInstanceWithKey(string var, string type, string keyType, string keyValue)
        {
            Attribute key = SingleTransaction
                .Concepts
                .GetAttributeType(keyType).Resolve()
                .Put(SingleTransaction, keyValue).Resolve();

            Entity entity = SingleTransaction
                .Concepts
                .GetEntityType(type).Resolve()
                .create(SingleTransaction).Resolve();

            entity.SetHas(SingleTransaction, key).Resolve();

            Put(var, entity);
        }

        [When(@"{var} = entity\\( ?{type_label} ?) create new instance with key\\( ?{type_label} ?): {datetime}")]
        public void EntityTypeCreateNewInstanceWithKey(
            string var, string type, string keyType, DateTime keyValue)
        {
            Attribute key = SingleTransaction
                .Concepts
                .GetAttributeType(keyType).Resolve()
                .Put(SingleTransaction, keyValue).Resolve();

            Entity entity = SingleTransaction
                .Concepts
                .GetEntityType(type).Resolve()
                .create(SingleTransaction).Resolve();

            entity.SetHas(SingleTransaction, key).Resolve();

            Put(var, entity);
        }

        [When(@"{var} = entity\\( ?{type_label} ?) get instance with key\\( ?{type_label} ?): {long}")]
        public void EntityTypeGetInstanceWithKey(string var1, string type, string keyType, long keyValue)
        {
            var entityType = SingleTransaction
                .Concepts
                .GetAttributeType(keyType).Resolve()
                .Get(SingleTransaction, keyValue).Resolve()
                .GetOwners(SingleTransaction)
                .filter(owner => owner.GetType().Label.equals(Label.of(type)))
                .findFirst().orElse(null);

            Put(var, entityType);
        }

        [When(@"{var} = entity\\( ?{type_label} ?) get instance with key\\( ?{type_label} ?): {word}")]
        public void EntityTypeGetInstanceWithKey(string var1, string type, string keyType, string keyValue)
        {
            var entityType = SingleTransaction
                .Concepts
                .GetAttributeType(keyType).Resolve()
                .Get(SingleTransaction, keyValue).Resolve()
                .GetOwners(SingleTransaction)
                .filter(owner => owner.GetType().Label.equals(Label.of(type)))
                .findFirst().orElse(null);

            Put(var, entityType);
        }

        [When(@"{var} = entity\\( ?{type_label} ?) get instance with key\\( ?{type_label} ?): {datetime}")]
        public void EntityTypeGetInstanceWithKey(
            string var1, string type, string keyType, DateTime keyValue)
        {
            var entityType = SingleTransaction
                .Concepts
                .GetAttributeType(keyType).Resolve()
                .Get(SingleTransaction, keyValue).Resolve()
                .GetOwners(SingleTransaction)
                .filter(owner => owner.GetType().Label.equals(Label.of(type)))
                .findFirst().orElse(null);

            Put(var, entityType);
        }

        [Then(@"entity\\( ?{type_label} ?) get instances contain: {var}")]
        public void EntityTypeGetInstancesContain(string typeLabel, string var)
        {
            Assert.True(SingleTransaction
                .Concepts
                .GetEntityType(typeLabel).Resolve()
                .GetInstances(SingleTransaction)
                .anyMatch(i => i.equals(Get(var))));
        }

        [Then(@"entity\\( ?{type_label} ?) get instances is empty")]
        public void EntityTypeGetInstancesIsEmpty(string typeLabel)
        {
            var instances = SingleTransaction
                .Concepts
                .GetEntityType(typeLabel).Resolve()
                .GetInstances(SingleTransaction);

            Assert.Equals(0, instances.Count);
        }
    }
}
