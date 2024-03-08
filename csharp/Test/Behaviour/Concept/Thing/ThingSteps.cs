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

        public static Thing Get(string variable)
        {
        // TODO: DO I NEED IT?
            return _things.Get(variable);
        }

        public static void Put(string variable, Thing thing)
        {
            _things.Put(variable, thing);
        }

        [Then(@"entity/attribute/relation {var} is null: {bool}")]
        public void ThingIsNull(string var, bool isNull)
        {
            if (isNull)
            {
                Assert.Null(Get(var));
            }
            else
            {
                Assert.NotNull(Get(var));
            }
        }

        [Then(@"entity/attribute/relation {var} is deleted: {bool}")]
        public void ThingIsDeleted(string var, bool isDeleted)
        {
            Assert.Equals(isDeleted, Get(var).isDeleted(SingleTransaction).Resolve());
        }

        [Then(@"{root_label} {var} has type: {type_label}")]
        public void ThingHasType(RootLabel rootLabel, string var, string typeLabel)
        {
            ThingType type = get_Thingtype(rootLabel, typeLabel);
            Assert.Equals(type, Get(var).GetType());
        }

        [When(@"delete entity:/attribute:/relation: {var}")]
        public void DeleteThings(string var)
        {
            Get(var).delete(SingleTransaction).Resolve();
        }

        [When(@"entity/attribute/relation {var} set has: {var}")]
        public void ThingSetHas(string var1, string var2)
        {
            Get(var1).SetHas(SingleTransaction, Get(var2).AsAttribute()).Resolve();
        }

        [Then(@"entity/attribute/relation {var} set has: {var}; throws exception")]
        public void ThingSetHasThrowsException(string var1, string var2)
        {
            Assert.Throws<TypeDBDriverException>(
                () => Get(var1).SetHas(SingleTransaction, Get(var2).AsAttribute()).Resolve());
        }

        [When(@"entity/attribute/relation {var} unset has: {var}")]
        public void ThingUnsetHas(string var1, string var2)
        {
            Get(var1).UnsetHas(SingleTransaction, Get(var2).AsAttribute()).Resolve();
        }

        [Then(@"entity/attribute/relation {var} get keys contain: {var}")]
        public void ThingGetKeysContain(string var1, string var2)
        {
            Assert.True(Get(var1)
                .GetHas(
                    SingleTransaction,
                    new []{IThingType.Annotation.NewKey()})
                .Where(k => k.Equals(Get(var2)))
                .Any());
        }

        [Then(@"entity/attribute/relation {var} get keys do not contain: {var}")]
        public void ThingGetKeysDoNotContain(string var1, string var2)
        {
            Assert.False(Get(var1)
                .GetHas(
                    SingleTransaction,
                    new []{IThingType.Annotation.NewKey()})
                .Where(k => k.Equals(Get(var2)))
                .Any());
        }

        [Then(@"entity/attribute/relation {var} get attributes contain: {var}")]
        public void ThingGetAttributesContain(string var1, string var2)
        {
            Assert.True(Get(var1).GetHas(SingleTransaction).Where(k => k.Equals(Get(var2))).Any());
        }

        [Then(@"entity/attribute/relation {var} get attributes\\( ?{type_label} ?) contain: {var}")]
        public void ThingGetAttributesContain(string var1, string typeLabel, string var2)
        {
            Assert.True(Get(var1)
                .GetHas(SingleTransaction, SingleTransaction.Concepts.GetAttributeType(typeLabel).Resolve())
                .Where(k => k.Equals(Get(var2)))
                .Any());
        }

        [Then(@"entity/attribute/relation {var} get attributes\\( ?{type_label} ?) as\\( ?boolean ?) contain: {var}")]
        public void ThingGetAttributesAsBooleanContain(string var1, string typeLabel, string var2)
        {
            Assert.True(Get(var1)
                .GetHas(SingleTransaction, SingleTransaction.Concepts.GetAttributeType(typeLabel).Resolve())
                .Where(k => k.Equals(Get(var2)))
                .Any());
        }

        [Then(@"entity/attribute/relation {var} get attributes\\( ?{type_label} ?) as\\( ?long ?) contain: {var}")]
        public void ThingGetAttributesAsLongContain(string var1, string typeLabel, string var2)
        {
            Assert.True(Get(var1)
                .GetHas(SingleTransaction, SingleTransaction.Concepts.GetAttributeType(typeLabel).Resolve())
                .Where(k => k.Equals(Get(var2)))
                .Any());
        }

        [Then(@"entity/attribute/relation {var} get attributes\\( ?{type_label} ?) as\\( ?double ?) contain: {var}")]
        public void ThingGetAttributesAsDoubleContain(string var1, string typeLabel, string var2)
        {
            Assert.True(Get(var1)
                .GetHas(SingleTransaction, SingleTransaction.Concepts.GetAttributeType(typeLabel).Resolve())
                .Where(k => k.Equals(Get(var2)))
                .Any());
        }

        [Then(@"entity/attribute/relation {var} get attributes\\( ?{type_label} ?) as\\( ?string ?) contain: {var}")]
        public void ThingGetAttributesAsStringContain(string var1, string typeLabel, string var2)
        {
            Assert.True(Get(var1)
                .GetHas(SingleTransaction, SingleTransaction.Concepts.GetAttributeType(typeLabel).Resolve())
                .Where(k => k.Equals(Get(var2)))
                .Any());
        }

        [Then(@"entity/attribute/relation {var} get attributes\\( ?{type_label} ?) as\\( ?datetime ?) contain: {var}")]
        public void ThingGetAttributesAsDatetimeContain(string var1, string typeLabel, string var2)
        {
            Assert.True(Get(var1)
                .GetHas(SingleTransaction, SingleTransaction.Concepts.GetAttributeType(typeLabel).Resolve())
                .Where(k => k.Equals(Get(var2)))
                .Any());
        }

        [Then(@"entity/attribute/relation {var} get attributes do not contain: {var}")]
        public void ThingGetAttributesDoNotContain(string var1, string var2)
        {
            Assert.False(Get(var1).GetHas(SingleTransaction).Where(k => k.Equals(Get(var2))).Any());
        }

        [Then(@"entity/attribute/relation {var} get attributes\\( ?{type_label} ?) do not contain: {var}")]
        public void ThingGetAttributesDoNotContain(string var1, string typeLabel, string var2)
        {
            Assert.False(Get(var1)
                .GetHas(SingleTransaction, SingleTransaction.Concepts.GetAttributeType(typeLabel).Resolve())
                .Where(k => k.Equals(Get(var2)))
                .Any());
        }

        [Then(@"entity/attribute/relation {var} get attributes\\( ?{type_label} ?) as\\( ?boolean ?) do not contain: {var}")]
        public void ThingGetAttributesAsBooleanDoNotContain(string var1, string typeLabel, string var2)
        {
            Assert.False(Get(var1)
                .GetHas(SingleTransaction, SingleTransaction.Concepts.GetAttributeType(typeLabel).Resolve())
                .Where(k => k.Equals(Get(var2)))
                .Any());
        }

        [Then(@"entity/attribute/relation {var} get attributes\\( ?{type_label} ?) as\\( ?long ?) do not contain: {var}")]
        public void ThingGetAttributesAsLongDoNotContain(string var1, string typeLabel, string var2)
        {
            Assert.False(Get(var1)
                .GetHas(SingleTransaction, SingleTransaction.Concepts.GetAttributeType(typeLabel).Resolve())
                .Where(k => k.Equals(Get(var2)))
                .Any());
        }

        [Then(@"entity/attribute/relation {var} get attributes\\( ?{type_label} ?) as\\( ?double ?) do not contain: {var}")]
        public void ThingGetAttributesAsDoubleDoNotContain(string var1, string typeLabel, string var2)
        {
            Assert.False(Get(var1)
                .GetHas(SingleTransaction, SingleTransaction.Concepts.GetAttributeType(typeLabel).Resolve())
                .Where(k => k.Equals(Get(var2)))
                .Any());
        }

        [Then(@"entity/attribute/relation {var} get attributes\\( ?{type_label} ?) as\\( ?string ?) do not contain: {var}")]
        public void ThingGetAttributesAsStringDoNotContain(string var1, string typeLabel, string var2)
        {
            Assert.False(Get(var1)
                .GetHas(SingleTransaction, SingleTransaction.Concepts.GetAttributeType(typeLabel).Resolve())
                .Where(k => k.Equals(Get(var2)))
                .Any());
        }

        [Then(@"entity/attribute/relation {var} get attributes\\( ?{type_label} ?) as\\( ?datetime ?) do not contain: {var}")]
        public void ThingGetAttributesAsDatetimeDoNotContain(string var1, string typeLabel, string var2)
        {
            Assert.False(Get(var1)
                .GetHas(SingleTransaction, SingleTransaction.Concepts.GetAttributeType(typeLabel).Resolve())
                .Where(k => k.Equals(Get(var2)))
                .Any());
        }

        [Then(@"entity/attribute/relation {var} get relations\\( ?{scoped_label} ?) contain: {var}")]
        public void ThingGetRelationsContain(string var1, Label scopedLabel, string var2)
        {
            var relates = SingleTransaction
                .Concepts
                .GetRelationType(scopedLabel.Scope.Get()).Resolve()
                .GetRelates(SingleTransaction, scopedLabel.Name).Resolve();

            Assert.True(Get(var1).GetRelations(SingleTransaction, relates).Where(k => k.Equals(Get(var2))).Any());
        }

        [Then(@"entity/attribute/relation {var} get relations contain: {var}")]
        public void ThingGetRelationsContain(string var1, string var2)
        {
            Assert.True(Get(var1).GetRelations(SingleTransaction).Where(k => k.Equals(Get(var2))).Any());
        }

        [Then(@"entity/attribute/relation {var} get relations\\( ?{scoped_label} ?) do not contain: {var}")]
        public void ThingGetRelationsDoNotContain(string var1, Label scopedLabel, string var2)
        {
            var relates = SingleTransaction
                .Concepts
                .GetRelationType(scopedLabel.Scope.Get()).Resolve()
                .GetRelates(SingleTransaction, scopedLabel.Name).Resolve();

            Assert.False(Get(var1).GetRelations(SingleTransaction, relates).Where(k => k.Equals(Get(var2))).Any());
        }

        [Then(@"entity/attribute/relation {var} get relations do not contain: {var}")]
        public void ThingGetRelationsDoNotContain(string var1, string var2)
        {
            Assert.False(Get(var1).GetRelations(SingleTransaction).Where(k => k.Equals(Get(var2))).Any());
        }

        public void override Dispose()
        {
            _things.Clear();
            base.Dispose();
        }
    }
}
