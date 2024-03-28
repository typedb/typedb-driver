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
using System.Collections.Generic;
using System.Linq;
using Xunit;
using Xunit.Gherkin.Quick;

using TypeDB.Driver;
using TypeDB.Driver.Api;
using TypeDB.Driver.Common;
using static TypeDB.Driver.Api.IThingType.Annotation;

namespace TypeDB.Driver.Test.Behaviour
{
    public partial class BehaviourSteps
    {
        private static Dictionary<string, IThing?> _things = new Dictionary<string, IThing?>();

        public static IThing? Get(string variable)
        {
            return _things[variable];
        }

        public static void Put(string variable, IThing? thing)
        {
            _things[variable] = thing;
        }

        [Then(@"(entity|attribute|relation) \$([a-zA-Z0-9]+) is null: (true|false)")]
        public void ThingIsNull(string rootLabel, string var, bool isNull)
        {
            Assert.Equal(isNull, Get(var) == null);
        }

        [Then(@"(entity|attribute|relation) \$([a-zA-Z0-9]+) is deleted: (true|false)")]
        public void ThingIsDeleted(string rootLabel, string var, bool isDeleted)
        {
            Assert.Equal(isDeleted, Get(var)!.IsDeleted(Tx).Resolve()!);
        }

        [Then(@"(entity|attribute|relation|thing) \$([a-zA-Z0-9]+) has type: ([a-zA-Z0-9-_]+)")]
        public void ThingHasType(string rootLabel, string var, string typeLabel)
        {
            IThingType type = GetThingType(rootLabel, typeLabel);
            Assert.Equal(type, Get(var)!.Type);
        }

        [When(@"delete (entity|attribute|relation): \$([a-zA-Z0-9]+)")]
        public void DeleteThings(string rootLabel, string var)
        {
            Get(var)!.Delete(Tx).Resolve();
        }

        [When(@"(entity|attribute|relation) \$([a-zA-Z0-9]+) set has: \$([a-zA-Z0-9]+)")]
        public void ThingSetHas(string rootLabel, string var1, string var2)
        {
            Get(var1)!.SetHas(Tx, Get(var2)!.AsAttribute()).Resolve();
        }

        [When(@"(entity|attribute|relation) \$([a-zA-Z0-9]+) set has: \$([a-zA-Z0-9]+); throws exception")]
        [Then(@"(entity|attribute|relation) \$([a-zA-Z0-9]+) set has: \$([a-zA-Z0-9]+); throws exception")]
        public void ThingSetHasThrowsException(string rootLabel, string var1, string var2)
        {
            Assert.Throws<TypeDBDriverException>(
                () => Get(var1)!.SetHas(Tx, Get(var2)!.AsAttribute()).Resolve());
        }

        [When(@"(entity|attribute|relation) \$([a-zA-Z0-9]+) unset has: \$([a-zA-Z0-9]+)")]
        public void ThingUnsetHas(string rootLabel, string var1, string var2)
        {
            Get(var1)!.UnsetHas(Tx, Get(var2)!.AsAttribute()).Resolve();
        }

        [Then(@"(entity|attribute|relation) \$([a-zA-Z0-9]+) get keys contain: \$([a-zA-Z0-9]+)")]
        public void ThingGetKeysContain(string rootLabel, string var1, string var2)
        {
            Assert.True(Get(var1)!
                .GetHas(Tx, new []{NewKey()})
                .Where(k => k.Equals(Get(var2)!))
                .Any());
        }

        [Then(@"(entity|attribute|relation) \$([a-zA-Z0-9]+) get keys do not contain: \$([a-zA-Z0-9]+)")]
        public void ThingGetKeysDoNotContain(string rootLabel, string var1, string var2)
        {
            Assert.False(Get(var1)!
                .GetHas(Tx, new []{NewKey()})
                .Where(k => k.Equals(Get(var2)!))
                .Any());
        }

        [Then(@"(entity|attribute|relation) \$([a-zA-Z0-9]+) get attributes contain: \$([a-zA-Z0-9]+)")]
        public void ThingGetAttributesContain(string rootLabel, string var1, string var2)
        {
            Assert.True(Get(var1)!.GetHas(Tx).Where(k => k.Equals(Get(var2)!)).Any());
        }

        [Then(@"(entity|attribute|relation) \$([a-zA-Z0-9]+) get attributes\(([a-zA-Z0-9-_]+)\) contain: \$([a-zA-Z0-9]+)")]
        public void ThingGetAttributesContain(string rootLabel, string var1, string typeLabel, string var2)
        {
            Assert.True(Get(var1)!
                .GetHas(Tx, Tx.Concepts.GetAttributeType(typeLabel).Resolve()!)
                .Where(k => k.Equals(Get(var2)!))
                .Any());
        }

        [Then(@"(entity|attribute|relation) \$([a-zA-Z0-9]+) get attributes\(([a-zA-Z0-9-_]+)\) as\(boolean\) contain: \$([a-zA-Z0-9]+)")]
        public void ThingGetAttributesAsBooleanContain(string rootLabel, string var1, string typeLabel, string var2)
        {
            Assert.True(Get(var1)!
                .GetHas(Tx, Tx.Concepts.GetAttributeType(typeLabel).Resolve()!)
                .Where(k => k.Equals(Get(var2)!))
                .Any());
        }

        [Then(@"(entity|attribute|relation) \$([a-zA-Z0-9]+) get attributes\(([a-zA-Z0-9-_]+)\) as\(long\) contain: \$([a-zA-Z0-9]+)")]
        public void ThingGetAttributesAsLongContain(string rootLabel, string var1, string typeLabel, string var2)
        {
            Assert.True(Get(var1)!
                .GetHas(Tx, Tx.Concepts.GetAttributeType(typeLabel).Resolve()!)
                .Where(k => k.Equals(Get(var2)!))
                .Any());
        }

        [Then(@"(entity|attribute|relation) \$([a-zA-Z0-9]+) get attributes\(([a-zA-Z0-9-_]+)\) as\(double\) contain: \$([a-zA-Z0-9]+)")]
        public void ThingGetAttributesAsDoubleContain(string rootLabel, string var1, string typeLabel, string var2)
        {
            Assert.True(Get(var1)!
                .GetHas(Tx, Tx.Concepts.GetAttributeType(typeLabel).Resolve()!)
                .Where(k => k.Equals(Get(var2)!))
                .Any());
        }

        [Then(@"(entity|attribute|relation) \$([a-zA-Z0-9]+) get attributes\(([a-zA-Z0-9-_]+)\) as\(string\) contain: \$([a-zA-Z0-9]+)")]
        public void ThingGetAttributesAsStringContain(string rootLabel, string var1, string typeLabel, string var2)
        {
            Assert.True(Get(var1)!
                .GetHas(Tx, Tx.Concepts.GetAttributeType(typeLabel).Resolve()!)
                .Where(k => k.Equals(Get(var2)!))
                .Any());
        }

        [Then(@"(entity|attribute|relation) \$([a-zA-Z0-9]+) get attributes\(([a-zA-Z0-9-_]+)\) as\(datetime\) contain: \$([a-zA-Z0-9]+)")]
        public void ThingGetAttributesAsDatetimeContain(string rootLabel, string var1, string typeLabel, string var2)
        {
            Assert.True(Get(var1)!
                .GetHas(Tx, Tx.Concepts.GetAttributeType(typeLabel).Resolve()!)
                .Where(k => k.Equals(Get(var2)!))
                .Any());
        }

        [Then(@"(entity|attribute|relation) \$([a-zA-Z0-9]+) get attributes do not contain: \$([a-zA-Z0-9]+)")]
        public void ThingGetAttributesDoNotContain(string rootLabel, string var1, string var2)
        {
            Assert.False(Get(var1)!.GetHas(Tx).Where(k => k.Equals(Get(var2)!)).Any());
        }

        [Then(@"(entity|attribute|relation) \$([a-zA-Z0-9]+) get attributes\(([a-zA-Z0-9-_]+)\) do not contain: \$([a-zA-Z0-9]+)")]
        public void ThingGetAttributesDoNotContain(string rootLabel, string var1, string typeLabel, string var2)
        {
            Assert.False(Get(var1)!
                .GetHas(Tx, Tx.Concepts.GetAttributeType(typeLabel).Resolve()!)
                .Where(k => k.Equals(Get(var2)!))
                .Any());
        }

        [Then(@"(entity|attribute|relation) \$([a-zA-Z0-9]+) get attributes\(([a-zA-Z0-9-_]+)\) as\(boolean\) do not contain: \$([a-zA-Z0-9]+)")]
        public void ThingGetAttributesAsBooleanDoNotContain(string rootLabel, string var1, string typeLabel, string var2)
        {
            Assert.False(Get(var1)!
                .GetHas(Tx, Tx.Concepts.GetAttributeType(typeLabel).Resolve()!)
                .Where(k => k.Equals(Get(var2)!))
                .Any());
        }

        [Then(@"(entity|attribute|relation) \$([a-zA-Z0-9]+) get attributes\(([a-zA-Z0-9-_]+)\) as\(long\) do not contain: \$([a-zA-Z0-9]+)")]
        public void ThingGetAttributesAsLongDoNotContain(string rootLabel, string var1, string typeLabel, string var2)
        {
            Assert.False(Get(var1)!
                .GetHas(Tx, Tx.Concepts.GetAttributeType(typeLabel).Resolve()!)
                .Where(k => k.Equals(Get(var2)!))
                .Any());
        }

        [Then(@"(entity|attribute|relation) \$([a-zA-Z0-9]+) get attributes\(([a-zA-Z0-9-_]+)\) as\(double\) do not contain: \$([a-zA-Z0-9]+)")]
        public void ThingGetAttributesAsDoubleDoNotContain(string rootLabel, string var1, string typeLabel, string var2)
        {
            Assert.False(Get(var1)!
                .GetHas(Tx, Tx.Concepts.GetAttributeType(typeLabel).Resolve()!)
                .Where(k => k.Equals(Get(var2)!))
                .Any());
        }

        [Then(@"(entity|attribute|relation) \$([a-zA-Z0-9]+) get attributes\(([a-zA-Z0-9-_]+)\) as\(string\) do not contain: \$([a-zA-Z0-9]+)")]
        public void ThingGetAttributesAsStringDoNotContain(string rootLabel, string var1, string typeLabel, string var2)
        {
            Assert.False(Get(var1)!
                .GetHas(Tx, Tx.Concepts.GetAttributeType(typeLabel).Resolve()!)
                .Where(k => k.Equals(Get(var2)!))
                .Any());
        }

        [Then(@"(entity|attribute|relation) \$([a-zA-Z0-9]+) get attributes\(([a-zA-Z0-9-_]+)\) as\(datetime\) do not contain: \$([a-zA-Z0-9]+)")]
        public void ThingGetAttributesAsDatetimeDoNotContain(string rootLabel, string var1, string typeLabel, string var2)
        {
            Assert.False(Get(var1)!
                .GetHas(Tx, Tx.Concepts.GetAttributeType(typeLabel).Resolve()!)
                .Where(k => k.Equals(Get(var2)!))
                .Any());
        }

        [Then(@"(entity|attribute|relation) \$([a-zA-Z0-9]+) get relations\(([a-zA-Z0-9-_]+:[a-zA-Z0-9-_]+)\) contain: \$([a-zA-Z0-9]+)")]
        public void ThingGetRelationsContain(string rootLabel, string var1, string scopedLabelData, string var2)
        {
            Label scopedLabel = GetScopedLabel(scopedLabelData);

            var relates = Tx.Concepts
                .GetRelationType(scopedLabel.Scope!).Resolve()!
                .GetRelates(Tx, scopedLabel.Name).Resolve()!;

            Assert.True(Get(var1)!.GetRelations(Tx, relates).Where(k => k.Equals(Get(var2)!)).Any());
        }

        [Then(@"(entity|attribute|relation) \$([a-zA-Z0-9]+) get relations contain: \$([a-zA-Z0-9]+)")]
        public void ThingGetRelationsContain(string rootLabel, string var1, string var2)
        {
            Assert.True(Get(var1)!.GetRelations(Tx).Where(k => k.Equals(Get(var2)!)).Any());
        }

        [Then(@"(entity|attribute|relation) \$([a-zA-Z0-9]+) get relations\(([a-zA-Z0-9-_]+:[a-zA-Z0-9-_]+)\) do not contain: \$([a-zA-Z0-9]+)")]
        public void ThingGetRelationsDoNotContain(string rootLabel, string var1, string scopedLabelData, string var2)
        {
            Label scopedLabel = GetScopedLabel(scopedLabelData);

            var relates = Tx.Concepts
                .GetRelationType(scopedLabel.Scope!).Resolve()!
                .GetRelates(Tx, scopedLabel.Name).Resolve()!;

            Assert.False(Get(var1)!.GetRelations(Tx, relates).Where(k => k.Equals(Get(var2)!)).Any());
        }

        [Then(@"(entity|attribute|relation) \$([a-zA-Z0-9]+) get relations do not contain: \$([a-zA-Z0-9]+)")]
        public void ThingGetRelationsDoNotContain(string rootLabel, string var1, string var2)
        {
            Assert.False(Get(var1)!.GetRelations(Tx).Where(k => k.Equals(Get(var2)!)).Any());
        }
    }
}
