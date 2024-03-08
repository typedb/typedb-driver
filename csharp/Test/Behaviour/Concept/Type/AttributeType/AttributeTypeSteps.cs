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
        [When(@"put attribute type: {type_label}, with value type: {value_type}")]
        public void PutAttributeTypeWithValueType(string typeLabel, Value.Type valueType)
        {
            SingleTransaction.Concepts.putAttributeType(typeLabel, valueType).Resolve();
        }

        [Then(@"attribute\\( ?{type_label} ?) get value type: {value_type}")]
        public void AttributeTypeGetValueType(string typeLabel, Value.Type valueType)
        {
            Assert.Equals(
                valueType,
                SingleTransaction.Concepts.GetAttributeType(typeLabel).Resolve().GetValueType());
        }

        [Then(@"attribute\\( ?{type_label} ?) get supertype value type: {value_type}")]
        public void AttributeTypeGetSupertypeValueType(string typeLabel, Value.Type valueType)
        {
            AttributeType supertype = SingleTransaction
                .Concepts
                .GetAttributeType(typeLabel).Resolve()
                .GetSupertype(SingleTransaction).Resolve()
                .AsAttributeType();

            Assert.Equals(valueType, supertype.GetValueType());
        }

        [Then(@"attribute\\( ?{type_label} ?) as\\( ?{value_type} ?) get subtypes contain:")]
        public void AttributeTypeAsValueTypeGetSubtypesContain(
            string typeLabel, Value.Type valueType, List<string> subLabels)
        {
            AttributeType attributeType = SingleTransaction.Concepts.GetAttributeType(typeLabel).Resolve();
            HashSet<string> actuals = attributeType
                .GetSubtypes(SingleTransaction, valueType)
                .Select(t => t.Label.Name)
                .ToHashSet();

            Assert.False(actuals.Except(subLabels).Any());
        }

        [Then(@"attribute\\( ?{type_label} ?) as\\( ?{value_type} ?) get subtypes do not contain:")]
        public void AttributeTypeAsValueTypeGetSubtypesDoNotContain(
        string typeLabel, Value.Type valueType, List<string> subLabels)
        {
            AttributeType attributeType = SingleTransaction.Concepts.GetAttributeType(typeLabel).Resolve();
            HashSet<string> actuals = attributeType
                .GetSubtypes(SingleTransaction, valueType)
                .Select(t => t.Label.Name)
                .ToHashSet();

            for (string subLabel : subLabels)
            {
                Assert.False(actuals.Contains(subLabel));
            }
        }

        [Then(@"attribute\\( ?{type_label} ?) as\\( ?{value_type} ?) set regex: {}")]
        public void AttributeTypeAsValueTypeSetRegex(string typeLabel, Value.Type valueType, string regex)
        {
            if (!valueType.Equals(Value.Type.STRING))
            {
                fail();
            }

            AttributeType attributeType = SingleTransaction.Concepts.GetAttributeType(typeLabel).Resolve();
            attributeType.SetRegex(SingleTransaction, regex).Resolve();
        }

        [Then(@"attribute\\( ?{type_label} ?) as\\( ?{value_type} ?) unset regex")]
        public void AttributeTypeAsValueTypeUnsetRegex(string typeLabel, Value.Type valueType)
        {
            if (!valueType.Equals(Value.Type.STRING))
            {
                fail();
            }

            AttributeType attributeType = SingleTransaction.Concepts.GetAttributeType(typeLabel).Resolve();
            attributeType.unsetRegex(SingleTransaction).Resolve();
        }

        [Then(@"attribute\\( ?{type_label} ?) as\\( ?{value_type} ?) get regex: {}")]
        public void AttributeTypeAsValueTypeGetRegex(string typeLabel, Value.Type valueType, string regex)
        {
            if (!valueType.Equals(Value.Type.STRING))
            {
                fail();
            }

            AttributeType attributeType = SingleTransaction.Concepts.GetAttributeType(typeLabel).Resolve();
            Assert.Equals(regex, attributeType.GetRegex(SingleTransaction).Resolve());
        }

        [Then(@"attribute\\( ?{type_label} ?) as\\( ?{value_type} ?) does not have any regex")]
        public void AttributeTypeAsValueTypeDoesNotHaveAnyRegex(string typeLabel, Value.Type valueType)
        {
            AttributeTypeAsValueType_Getregex(typeLabel, valueType, null);
        }

        [Then(@"attribute\\( ?{type_label} ?) get owners, with annotations: {annotations}; contain:")]
        public void AttributeTypeGetOwnersWithAnnotationsContain(
            string typeLabel, List<Annotation> annotations, List<string> ownerLabels)
        {
            AttributeType attributeType = SingleTransaction.Concepts.GetAttributeType(typeLabel).Resolve();
            HashSet<string> actuals = attributeType
                .GetOwners(SingleTransaction, set(annotations))
                .Selectt => t.Label.Name)
                .ToHashSet();

            Assert.False(actuals.Except(ownerLabels).Any());
        }

        [Then(@"attribute\\( ?{type_label} ?) get owners, with annotations: {annotations}; do not contain:")]
        public void AttributeTypeGetOwnersWithAnnotationsDoNotContain(
            string typeLabel, List<Annotation> annotations, List<string> ownerLabels)
        {
            AttributeType attributeType = SingleTransaction.Concepts.GetAttributeType(typeLabel).Resolve();
            HashSet<string> actuals = attributeType
                .GetOwners(SingleTransaction, set(annotations))
                .Selectt => t.Label.Name)
                .ToHashSet();

            for (string ownerLabel : ownerLabels)
            {
                Assert.False(actuals.Contains(ownerLabel));
            }
        }

        [Then(@"attribute\\( ?{type_label} ?) get owners explicit, with annotations: {annotations}; contain:")]
        public void AttributeTypeGetOwnersExplicitWithAnnotationsContain(
            string typeLabel, List<Annotation> annotations, List<string> ownerLabels)
        {
            AttributeType attributeType = SingleTransaction.Concepts.GetAttributeType(typeLabel).Resolve();
            HashSet<string> actuals = attributeType
                .GetOwners(SingleTransaction, set(annotations), EXPLICIT)
                .Selectt => t.Label.Name)
                .ToHashSet();

            Assert.False(actuals.Except(ownerLabels).Any());
        }

        [Then(@"attribute\\( ?{type_label} ?) get owners explicit, with annotations: {annotations}; do not contain:")]
        public void AttributeTypeGetOwnersExplicitWithAnnotationsDoNotContain(
            string typeLabel, List<Annotation> annotations, List<string> ownerLabels)
        {
            AttributeType attributeType = SingleTransaction.Concepts.GetAttributeType(typeLabel).Resolve();
            HashSet<string> actuals = attributeType
                .GetOwners(SingleTransaction, set(annotations), EXPLICIT)
                .Selectt => t.Label.Name)
                .ToHashSet();

            for (string ownerLabel : ownerLabels)
            {
                Assert.False(actuals.Contains(ownerLabel));
            }
        }

        [Then(@"attribute\\( ?{type_label} ?) get owners contain:")]
        public void AttributeTypeGetOwnersContain(string typeLabel, List<string> ownerLabels)
        {
            AttributeType attributeType = SingleTransaction.Concepts.GetAttributeType(typeLabel).Resolve();
            HashSet<string> actuals = attributeType
                .GetOwners(SingleTransaction, emptySet())
                .Selectt => t.Label.Name)
                .ToHashSet();

            Assert.False(actuals.Except(ownerLabels).Any());
        }

        [Then(@"attribute\\( ?{type_label} ?) get owners do not contain:")]
        public void AttributeTypeGetOwnersDoNotContain(string typeLabel, List<string> ownerLabels)
        {
            AttributeType attributeType = SingleTransaction.Concepts.GetAttributeType(typeLabel).Resolve();
            HashSet<string> actuals = attributeType
                .GetOwners(SingleTransaction, emptySet())
                .Selectt => t.Label.Name)
                .ToHashSet();

            for (string ownerLabel : ownerLabels)
            {
                Assert.False(actuals.Contains(ownerLabel));
            }
        }

        [Then(@"attribute\\( ?{type_label} ?) get owners explicit contain:")]
        public void AttributeTypeGetOwnersExplicitContain(string typeLabel, List<string> ownerLabels)
        {
            AttributeType attributeType = SingleTransaction.Concepts.GetAttributeType(typeLabel).Resolve();
            HashSet<string> actuals = attributeType
                .GetOwners(SingleTransaction, emptySet(), EXPLICIT)
                .Selectt => t.Label.Name)
                .ToHashSet();

            Assert.False(actuals.Except(ownerLabels).Any());
        }

        [Then(@"attribute\\( ?{type_label} ?) get owners explicit do not contain:")]
        public void AttributeTypeGetOwnersExplicitDoNotContain(string typeLabel, List<string> ownerLabels)
        {
            AttributeType attributeType = SingleTransaction.Concepts.GetAttributeType(typeLabel).Resolve();
            HashSet<string> actuals = attributeType
                .GetOwners(SingleTransaction, emptySet(), EXPLICIT)
                .Selectt => t.Label.Name)
                .ToHashSet();

            for (string ownerLabel : ownerLabels)
            {
                Assert.False(actuals.Contains(ownerLabel));
            }
        }
    }
}
