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
        private static Dictionary<string, Thing> _things = new Dictionary<string, IThing>();

        public static Thing get(string variable)
        {
        // TODO: DO I NEED IT?
            return _things.get(variable);
        }

        public static void put(string variable, Thing thing)
        {
            _things.put(variable, thing);
        }

        [Then(@"entity/attribute/relation {var} is null: {bool}")]
        public void thing_is_null(string var, boolean isNull)
        {
            if (isNull)
            {
                assertNull(get(var));
            }
            else
            {
                assertNotNull(get(var));
            }
        }

        [Then(@"entity/attribute/relation {var} is deleted: {bool}")]
        public void thing_is_deleted(string var, boolean isDeleted)
        {
            assertEquals(isDeleted, get(var).isDeleted(SingleTransaction).Resolve());
        }

        [Then(@"{root_label} {var} has type: {type_label}")]
        public void thing_has_type(RootLabel rootLabel, string var, string typeLabel)
        {
            ThingType type = get_thing_type(rootLabel, typeLabel);
            assertEquals(type, get(var).getType());
        }

        [When(@"delete entity:/attribute:/relation: {var}")]
        public void delete_thing(string var)
        {
            get(var).delete(SingleTransaction).Resolve();
        }

        [When(@"entity/attribute/relation {var} set has: {var}")]
        public void thing_set_has(string var1, string var2)
        {
            get(var1).setHas(SingleTransaction, get(var2).asAttribute()).Resolve();
        }

        [Then(@"entity/attribute/relation {var} set has: {var}; throws exception")]
        public void thing_set_has_throws_exception(string var1, string var2)
        {
            assertThrows(() => get(var1).setHas(SingleTransaction, get(var2).asAttribute()).Resolve());
        }

        [When(@"entity/attribute/relation {var} unset has: {var}")]
        public void thing_unset_has(string var1, string var2)
        {
            get(var1).unsetHas(SingleTransaction, get(var2).asAttribute()).Resolve();
        }

        [Then(@"entity/attribute/relation {var} get keys contain: {var}")]
        public void thing_get_keys_contain(string var1, string var2)
        {
            assertTrue(get(var1).getHas(SingleTransaction, set(key())).anyMatch(k => k.equals(get(var2))));
        }

        [Then(@"entity/attribute/relation {var} get keys do not contain: {var}")]
        public void thing_get_keys_do_not_contain(string var1, string var2)
        {
            assertTrue(get(var1).getHas(SingleTransaction, set(key())).noneMatch(k => k.equals(get(var2))));
        }

        [Then(@"entity/attribute/relation {var} get attributes contain: {var}")]
        public void thing_get_attributes_contain(string var1, string var2)
        {
            assertTrue(get(var1).getHas(SingleTransaction).anyMatch(k => k.equals(get(var2))));
        }

        [Then(@"entity/attribute/relation {var} get attributes\\( ?{type_label} ?) contain: {var}")]
        public void thing_get_attributes_contain(string var1, string typeLabel, string var2)
        {
            assertTrue(get(var1)
                .getHas(SingleTransaction, SingleTransaction.Concepts.getAttributeType(typeLabel).Resolve())
                .anyMatch(k => k.equals(get(var2))));
        }

        [Then(@"entity/attribute/relation {var} get attributes\\( ?{type_label} ?) as\\( ?boolean ?) contain: {var}")]
        public void thing_get_attributes_as_boolean_contain(string var1, string typeLabel, string var2)
        {
            assertTrue(get(var1)
                .getHas(SingleTransaction, SingleTransaction.Concepts.getAttributeType(typeLabel).Resolve())
                .anyMatch(k => k.equals(get(var2))));
        }

        [Then(@"entity/attribute/relation {var} get attributes\\( ?{type_label} ?) as\\( ?long ?) contain: {var}")]
        public void thing_get_attributes_as_long_contain(string var1, string typeLabel, string var2)
        {
            assertTrue(get(var1)
                .getHas(SingleTransaction, SingleTransaction.Concepts.getAttributeType(typeLabel).Resolve())
                .anyMatch(k => k.equals(get(var2))));
        }

        [Then(@"entity/attribute/relation {var} get attributes\\( ?{type_label} ?) as\\( ?double ?) contain: {var}")]
        public void thing_get_attributes_as_double_contain(string var1, string typeLabel, string var2)
        {
            assertTrue(get(var1)
                .getHas(SingleTransaction, SingleTransaction.Concepts.getAttributeType(typeLabel).Resolve())
                .anyMatch(k => k.equals(get(var2))));
        }

        [Then(@"entity/attribute/relation {var} get attributes\\( ?{type_label} ?) as\\( ?string ?) contain: {var}")]
        public void thing_get_attributes_as_string_contain(string var1, string typeLabel, string var2)
        {
            assertTrue(get(var1)
                .getHas(SingleTransaction, SingleTransaction.Concepts.getAttributeType(typeLabel).Resolve())
                .anyMatch(k => k.equals(get(var2))));
        }

        [Then(@"entity/attribute/relation {var} get attributes\\( ?{type_label} ?) as\\( ?datetime ?) contain: {var}")]
        public void thing_get_attributes_as_datetime_contain(string var1, string typeLabel, string var2)
        {
            assertTrue(get(var1)
                .getHas(SingleTransaction, SingleTransaction.Concepts.getAttributeType(typeLabel).Resolve())
                .anyMatch(k => k.equals(get(var2))));
        }

        [Then(@"entity/attribute/relation {var} get attributes do not contain: {var}")]
        public void thing_get_attributes_do_not_contain(string var1, string var2)
        {
            assertTrue(get(var1).getHas(SingleTransaction).noneMatch(k => k.equals(get(var2))));
        }

        [Then(@"entity/attribute/relation {var} get attributes\\( ?{type_label} ?) do not contain: {var}")]
        public void thing_get_attributes_do_not_contain(string var1, string typeLabel, string var2)
        {
            assertTrue(get(var1)
                .getHas(SingleTransaction, SingleTransaction.Concepts.getAttributeType(typeLabel).Resolve())
                .noneMatch(k => k.equals(get(var2))));
        }

        [Then(@"entity/attribute/relation {var} get attributes\\( ?{type_label} ?) as\\( ?boolean ?) do not contain: {var}")]
        public void thing_get_attributes_as_boolean_do_not_contain(string var1, string typeLabel, string var2)
        {
            assertTrue(get(var1)
                .getHas(SingleTransaction, SingleTransaction.Concepts.getAttributeType(typeLabel).Resolve())
                .noneMatch(k => k.equals(get(var2))));
        }

        [Then(@"entity/attribute/relation {var} get attributes\\( ?{type_label} ?) as\\( ?long ?) do not contain: {var}")]
        public void thing_get_attributes_as_long_do_not_contain(string var1, string typeLabel, string var2)
        {
            assertTrue(get(var1)
                .getHas(SingleTransaction, SingleTransaction.Concepts.getAttributeType(typeLabel).Resolve())
                .noneMatch(k => k.equals(get(var2))));
        }

        [Then(@"entity/attribute/relation {var} get attributes\\( ?{type_label} ?) as\\( ?double ?) do not contain: {var}")]
        public void thing_get_attributes_as_double_do_not_contain(string var1, string typeLabel, string var2)
        {
            assertTrue(get(var1)
                .getHas(SingleTransaction, SingleTransaction.Concepts.getAttributeType(typeLabel).Resolve())
                .noneMatch(k => k.equals(get(var2))));
        }

        [Then(@"entity/attribute/relation {var} get attributes\\( ?{type_label} ?) as\\( ?string ?) do not contain: {var}")]
        public void thing_get_attributes_as_string_do_not_contain(string var1, string typeLabel, string var2)
        {
            assertTrue(get(var1)
                .getHas(SingleTransaction, SingleTransaction.Concepts.getAttributeType(typeLabel).Resolve())
                .noneMatch(k => k.equals(get(var2))));
        }

        [Then(@"entity/attribute/relation {var} get attributes\\( ?{type_label} ?) as\\( ?datetime ?) do not contain: {var}")]
        public void thing_get_attributes_as_datetime_do_not_contain(string var1, string typeLabel, string var2)
        {
            assertTrue(get(var1)
                .getHas(SingleTransaction, SingleTransaction.Concepts.getAttributeType(typeLabel).Resolve())
                .noneMatch(k => k.equals(get(var2))));
        }

        [Then(@"entity/attribute/relation {var} get relations\\( ?{scoped_label} ?) contain: {var}")]
        public void thing_get_relations_contain(string var1, Label scopedLabel, string var2)
        {
            var relates = SingleTransaction
                .Concepts
                .getRelationType(scopedLabel.scope().get()).Resolve()
                .getRelates(SingleTransaction, scopedLabel.name()).Resolve();
            assertTrue(get(var1).getRelations(SingleTransaction, relates).anyMatch(k => k.equals(get(var2))));
        }

        [Then(@"entity/attribute/relation {var} get relations contain: {var}")]
        public void thing_get_relations_contain(string var1, string var2)
        {
            assertTrue(get(var1).getRelations(SingleTransaction).anyMatch(k => k.equals(get(var2))));
        }

        [Then(@"entity/attribute/relation {var} get relations\\( ?{scoped_label} ?) do not contain: {var}")]
        public void thing_get_relations_do_not_contain(string var1, Label scopedLabel, string var2)
        {
            var relates = SingleTransaction
                .Concepts
                .getRelationType(scopedLabel.scope().get()).Resolve()
                .getRelates(SingleTransaction, scopedLabel.name()).Resolve();

            assertTrue(get(var1).getRelations(SingleTransaction, relates).noneMatch(k => k.equals(get(var2))));
        }

        [Then(@"entity/attribute/relation {var} get relations do not contain: {var}")]
        public void thing_get_relations_do_not_contain(string var1, string var2)
        {
            assertTrue(get(var1).getRelations(SingleTransaction).noneMatch(k => k.equals(get(var2))));
        }

        public void override Dispose()
        {
            _things.clear();
            base.Dispose();
        }
    }
}
