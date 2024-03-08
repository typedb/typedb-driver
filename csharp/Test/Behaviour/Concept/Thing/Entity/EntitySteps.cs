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
        public void entity_type_create_new_instance(string var, string typeLabel)
        {
            var entityType = SingleTransaction
                .Concepts
                .getEntityType(typeLabel).Resolve()
                .create(SingleTransaction).Resolve();

            put(var, entityType);
        }

        [When(@"entity\\( ?{type_label} ?) create new instance; throws exception")]
        public void entity_type_create_new_instance_throws_exception(string typeLabel)
        {
            assertThrows(() => SingleTransaction
                .Concepts
                .getEntityType(typeLabel).Resolve()
                .create(SingleTransaction).Resolve());
        }

        [When(@"{var} = entity\\( ?{type_label} ?) create new instance with key\\( ?{type_label} ?): {int}")]
        public void entity_type_create_new_instance_with_key(string var, string type, string keyType, int keyValue)
        {
            Attribute key = SingleTransaction
                .Concepts
                .getAttributeType(keyType).Resolve()
                .put(SingleTransaction, keyValue).Resolve();

            Entity entity = SingleTransaction
                .Concepts
                .getEntityType(type).Resolve()
                .create(SingleTransaction).Resolve();

            entity.setHas(SingleTransaction, key).Resolve();

            put(var, entity);
        }

        [When(@"{var} = entity\\( ?{type_label} ?) create new instance with key\\( ?{type_label} ?): {word}")]
        public void entity_type_create_new_instance_with_key(string var, string type, string keyType, string keyValue)
        {
            Attribute key = SingleTransaction
                .Concepts
                .getAttributeType(keyType).Resolve()
                .put(SingleTransaction, keyValue).Resolve();

            Entity entity = SingleTransaction
                .Concepts
                .getEntityType(type).Resolve()
                .create(SingleTransaction).Resolve();

            entity.setHas(SingleTransaction, key).Resolve();

            put(var, entity);
        }

        [When(@"{var} = entity\\( ?{type_label} ?) create new instance with key\\( ?{type_label} ?): {datetime}")]
        public void entity_type_create_new_instance_with_key(
            string var, string type, string keyType, LocalDateTime keyValue)
        {
            Attribute key = SingleTransaction
                .Concepts
                .getAttributeType(keyType).Resolve()
                .put(SingleTransaction, keyValue).Resolve();

            Entity entity = SingleTransaction
                .Concepts
                .getEntityType(type).Resolve()
                .create(SingleTransaction).Resolve();

            entity.setHas(SingleTransaction, key).Resolve();

            put(var, entity);
        }

        [When(@"{var} = entity\\( ?{type_label} ?) get instance with key\\( ?{type_label} ?): {long}")]
        public void entity_type_get_instance_with_key(string var1, string type, string keyType, long keyValue)
        {
            var entityType = SingleTransaction
                .Concepts
                .getAttributeType(keyType).Resolve()
                .get(SingleTransaction, keyValue).Resolve()
                .getOwners(SingleTransaction)
                .filter(owner => owner.getType().getLabel().equals(Label.of(type)))
                .findFirst().orElse(null);

            put(var, entityType);
        }

        [When(@"{var} = entity\\( ?{type_label} ?) get instance with key\\( ?{type_label} ?): {word}")]
        public void entity_type_get_instance_with_key(string var1, string type, string keyType, string keyValue)
        {
            var entityType = SingleTransaction
                .Concepts
                .getAttributeType(keyType).Resolve()
                .get(SingleTransaction, keyValue).Resolve()
                .getOwners(SingleTransaction)
                .filter(owner => owner.getType().getLabel().equals(Label.of(type)))
                .findFirst().orElse(null);

            put(var, entityType);
        }

        [When(@"{var} = entity\\( ?{type_label} ?) get instance with key\\( ?{type_label} ?): {datetime}")]
        public void entity_type_get_instance_with_key(
            string var1, string type, string keyType, LocalDateTime keyValue)
        {
            var entityType = SingleTransaction
                .Concepts
                .getAttributeType(keyType).Resolve()
                .get(SingleTransaction, keyValue).Resolve()
                .getOwners(SingleTransaction)
                .filter(owner => owner.getType().getLabel().equals(Label.of(type)))
                .findFirst().orElse(null);

            put(var, entityType);
        }

        [Then(@"entity\\( ?{type_label} ?) get instances contain: {var}")]
        public void entity_type_get_instances_contain(string typeLabel, string var)
        {
            assertTrue(SingleTransaction
                .Concepts
                .getEntityType(typeLabel).Resolve()
                .getInstances(SingleTransaction)
                .anyMatch(i => i.equals(get(var))));
        }

        [Then(@"entity\\( ?{type_label} ?) get instances is empty")]
        public void entity_type_get_instances_is_empty(string typeLabel)
        {
            var instances = SingleTransaction
                .Concepts
                .getEntityType(typeLabel).Resolve()
                .getInstances(SingleTransaction);

            assertEquals(0, instances.Count);
        }
    }
}
