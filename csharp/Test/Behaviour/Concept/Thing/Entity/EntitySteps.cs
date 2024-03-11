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
        [When(@"\\$([a-zA-Z0-9]+) = entity\\( ?([a-zA-Z0-9-_]+) ?) create new instance")]
        public void EntityTypeCreateNewInstance(string var, string typeLabel)
        {
            var entityType = Tx.Concepts
                .GetEntityType(typeLabel).Resolve()
                .Create(Tx).Resolve();

            Put(var, entityType);
        }

        [When(@"entity\\( ?([a-zA-Z0-9-_]+) ?) create new instance; throws exception")]
        public void EntityTypeCreateNewInstanceThrowsException(string typeLabel)
        {
            Assert.Throws<TypeDBDriverException>(() => Tx.Concepts
                .GetEntityType(typeLabel).Resolve()
                .Create(Tx).Resolve());
        }

        [When(@"\\$([a-zA-Z0-9]+) = entity\\( ?([a-zA-Z0-9-_]+) ?) create new instance with key\\( ?([a-zA-Z0-9-_]+) ?): {int}")]
        public void EntityTypeCreateNewInstanceWithKey(string var, string type, string keyType, int keyValue)
        {
            IAttribute key = Tx.Concepts
                .GetAttributeType(keyType).Resolve()
                .Put(Tx, keyValue).Resolve();

            IEntity entity = Tx.Concepts
                .GetEntityType(type).Resolve()
                .Create(Tx).Resolve();

            entity.SetHas(Tx, key).Resolve();

            Put(var, entity);
        }

        [When(@"\\$([a-zA-Z0-9]+) = entity\\( ?([a-zA-Z0-9-_]+) ?) create new instance with key\\( ?([a-zA-Z0-9-_]+) ?): {word}")]
        public void EntityTypeCreateNewInstanceWithKey(string var, string type, string keyType, string keyValue)
        {
            IAttribute key = Tx.Concepts
                .GetAttributeType(keyType).Resolve()
                .Put(Tx, keyValue).Resolve();

            IEntity entity = Tx.Concepts
                .GetEntityType(type).Resolve()
                .Create(Tx).Resolve();

            entity.SetHas(Tx, key).Resolve();

            Put(var, entity);
        }

        [When(@"\\$([a-zA-Z0-9]+) = entity\\( ?([a-zA-Z0-9-_]+) ?) create new instance with key\\( ?([a-zA-Z0-9-_]+) ?): {datetime}")]
        public void EntityTypeCreateNewInstanceWithKey(
            string var, string type, string keyType, DateTime keyValue)
        {
            IAttribute key = Tx.Concepts
                .GetAttributeType(keyType).Resolve()
                .Put(Tx, keyValue).Resolve();

            IEntity entity = Tx.Concepts
                .GetEntityType(type).Resolve()
                .Create(Tx).Resolve();

            entity.SetHas(Tx, key).Resolve();

            Put(var, entity);
        }

        [When(@"\\$([a-zA-Z0-9]+) = entity\\( ?([a-zA-Z0-9-_]+) ?) get instance with key\\( ?([a-zA-Z0-9-_]+) ?): {long}")]
        public void EntityTypeGetInstanceWithKey(string var, string type, string keyType, long keyValue)
        {
            var entityType = Tx.Concepts
                .GetAttributeType(keyType).Resolve()
                .Get(Tx, keyValue).Resolve()
                .GetOwners(Tx)
                .Where(owner => owner.Type.Label.Equals(new Label(type)))
                .First();

            Put(var, entityType);
        }

        [When(@"\\$([a-zA-Z0-9]+) = entity\\( ?([a-zA-Z0-9-_]+) ?) get instance with key\\( ?([a-zA-Z0-9-_]+) ?): {word}")]
        public void EntityTypeGetInstanceWithKey(string var, string type, string keyType, string keyValue)
        {
            var entityType = Tx.Concepts
                .GetAttributeType(keyType).Resolve()
                .Get(Tx, keyValue).Resolve()
                .GetOwners(Tx)
                .Where(owner => owner.Type.Label.Equals(new Label(type)))
                .First();

            Put(var, entityType);
        }

        [When(@"\\$([a-zA-Z0-9]+) = entity\\( ?([a-zA-Z0-9-_]+) ?) get instance with key\\( ?([a-zA-Z0-9-_]+) ?): {datetime}")]
        public void EntityTypeGetInstanceWithKey(
            string var, string type, string keyType, DateTime keyValue)
        {
            var entityType = Tx.Concepts
                .GetAttributeType(keyType).Resolve()
                .Get(Tx, keyValue).Resolve()
                .GetOwners(Tx)
                .Where(owner => owner.Type.Label.Equals(new Label(type)))
                .First();

            Put(var, entityType);
        }

        [Then(@"entity\\( ?([a-zA-Z0-9-_]+) ?) get instances contain: \\$([a-zA-Z0-9]+)")]
        public void EntityTypeGetInstancesContain(string typeLabel, string var)
        {
            Assert.True(Tx.Concepts
                .GetEntityType(typeLabel).Resolve()
                .GetInstances(Tx)
                .Where(i => i.Equals(Get(var)))
                .Any());
        }

        [Then(@"entity\\( ?([a-zA-Z0-9-_]+) ?) get instances is empty")]
        public void EntityTypeGetInstancesIsEmpty(string typeLabel)
        {
            var instances = Tx.Concepts
                .GetEntityType(typeLabel).Resolve()
                .GetInstances(Tx);

            Assert.Equal(0, instances.Count());
        }
    }
}
