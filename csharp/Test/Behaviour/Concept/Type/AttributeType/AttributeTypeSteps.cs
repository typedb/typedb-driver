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
        public void put_attribute_type_with_value_type(string typeLabel, Value.Type valueType)
        {
            SingleTransaction.Concepts.putAttributeType(typeLabel, valueType).Resolve();
        }

        [Then(@"attribute\\( ?{type_label} ?) get value type: {value_type}")]
        public void attribute_type_get_value_type(string typeLabel, Value.Type valueType)
        {
            assertEquals(
                valueType,
                SingleTransaction.Concepts.getAttributeType(typeLabel).Resolve().getValueType());
        }

        [Then(@"attribute\\( ?{type_label} ?) get supertype value type: {value_type}")]
        public void attribute_type_get_supertype_value_type(string typeLabel, Value.Type valueType)
        {
            AttributeType supertype = SingleTransaction
                .Concepts
                .getAttributeType(typeLabel).Resolve()
                .getSupertype(SingleTransaction).Resolve()
                .asAttributeType();

            assertEquals(valueType, supertype.getValueType());
        }

        [Then(@"attribute\\( ?{type_label} ?) as\\( ?{value_type} ?) get subtypes contain:")]
        public void attribute_type_as_value_type_get_subtypes_contain(
            string typeLabel, Value.Type valueType, List<string> subLabels)
        {
            AttributeType attributeType = SingleTransaction.Concepts.getAttributeType(typeLabel).Resolve();
            Set<string> actuals = attributeType
                .getSubtypes(SingleTransaction, valueType)
                .Select(t => t.getLabel().name())
                .ToHashSet();

            assertTrue(actuals.containsAll(subLabels));
        }

        [Then(@"attribute\\( ?{type_label} ?) as\\( ?{value_type} ?) get subtypes do not contain:")]
        public void attribute_type_as_value_type_get_subtypes_do_not_contain(
        string typeLabel, Value.Type valueType, List<string> subLabels)
        {
            AttributeType attributeType = SingleTransaction.Concepts.getAttributeType(typeLabel).Resolve();
            Set<string> actuals = attributeType
                .getSubtypes(SingleTransaction, valueType)
                .Select(t => t.getLabel().name())
                .collect(toSet());

            for (string subLabel : subLabels)
            {
                assertFalse(actuals.contains(subLabel));
            }
        }

        [Then(@"attribute\\( ?{type_label} ?) as\\( ?{value_type} ?) set regex: {}")]
        public void attribute_type_as_value_type_set_regex(string typeLabel, Value.Type valueType, string regex)
        {
            if (!valueType.equals(Value.Type.STRING))
            {
                fail();
            }

            AttributeType attributeType = SingleTransaction.Concepts.getAttributeType(typeLabel).Resolve();
            attributeType.setRegex(SingleTransaction, regex).Resolve();
        }

        [Then(@"attribute\\( ?{type_label} ?) as\\( ?{value_type} ?) unset regex")]
        public void attribute_type_as_value_type_unset_regex(string typeLabel, Value.Type valueType)
        {
            if (!valueType.equals(Value.Type.STRING))
            {
                fail();
            }

            AttributeType attributeType = SingleTransaction.Concepts.getAttributeType(typeLabel).Resolve();
            attributeType.unsetRegex(SingleTransaction).Resolve();
        }

        [Then(@"attribute\\( ?{type_label} ?) as\\( ?{value_type} ?) get regex: {}")]
        public void attribute_type_as_value_type_get_regex(string typeLabel, Value.Type valueType, string regex)
        {
            if (!valueType.equals(Value.Type.STRING))
            {
                fail();
            }

            AttributeType attributeType = SingleTransaction.Concepts.getAttributeType(typeLabel).Resolve();
            assertEquals(regex, attributeType.getRegex(SingleTransaction).Resolve());
        }

        [Then(@"attribute\\( ?{type_label} ?) as\\( ?{value_type} ?) does not have any regex")]
        public void attribute_type_as_value_type_does_not_have_any_regex(string typeLabel, Value.Type valueType)
        {
            attribute_type_as_value_type_get_regex(typeLabel, valueType, null);
        }

        [Then(@"attribute\\( ?{type_label} ?) get owners, with annotations: {annotations}; contain:")]
        public void attribute_type_get_owners_with_annotations_contain(
            string typeLabel, List<Annotation> annotations, List<string> ownerLabels)
        {
            AttributeType attributeType = SingleTransaction.Concepts.getAttributeType(typeLabel).Resolve();
            Set<string> actuals = attributeType
                .getOwners(SingleTransaction, set(annotations))
                .map(t => t.getLabel().name())
                .collect(toSet());

            assertTrue(actuals.containsAll(ownerLabels));
        }

        [Then(@"attribute\\( ?{type_label} ?) get owners, with annotations: {annotations}; do not contain:")]
        public void attribute_type_get_owners_with_annotations_do_not_contain(
            string typeLabel, List<Annotation> annotations, List<string> ownerLabels)
        {
            AttributeType attributeType = SingleTransaction.Concepts.getAttributeType(typeLabel).Resolve();
            Set<string> actuals = attributeType
                .getOwners(SingleTransaction, set(annotations))
                .map(t => t.getLabel().name())
                .collect(toSet());

            for (string ownerLabel : ownerLabels)
            {
                assertFalse(actuals.contains(ownerLabel));
            }
        }

        [Then(@"attribute\\( ?{type_label} ?) get owners explicit, with annotations: {annotations}; contain:")]
        public void attribute_type_get_owners_explicit_with_annotations_contain(
            string typeLabel, List<Annotation> annotations, List<string> ownerLabels)
        {
            AttributeType attributeType = SingleTransaction.Concepts.getAttributeType(typeLabel).Resolve();
            Set<string> actuals = attributeType
                .getOwners(SingleTransaction, set(annotations), EXPLICIT)
                .map(t => t.getLabel().name())
                .collect(toSet());

            assertTrue(actuals.containsAll(ownerLabels));
        }

        [Then(@"attribute\\( ?{type_label} ?) get owners explicit, with annotations: {annotations}; do not contain:")]
        public void attribute_type_get_owners_explicit_with_annotations_do_not_contain(
            string typeLabel, List<Annotation> annotations, List<string> ownerLabels)
        {
            AttributeType attributeType = SingleTransaction.Concepts.getAttributeType(typeLabel).Resolve();
            Set<string> actuals = attributeType
                .getOwners(SingleTransaction, set(annotations), EXPLICIT)
                .map(t => t.getLabel().name())
                .collect(toSet());

            for (string ownerLabel : ownerLabels)
            {
                assertFalse(actuals.contains(ownerLabel));
            }
        }

        [Then(@"attribute\\( ?{type_label} ?) get owners contain:")]
        public void attribute_type_get_owners_contain(string typeLabel, List<string> ownerLabels)
        {
            AttributeType attributeType = SingleTransaction.Concepts.getAttributeType(typeLabel).Resolve();
            Set<string> actuals = attributeType
                .getOwners(SingleTransaction, emptySet())
                .map(t => t.getLabel().name())
                .collect(toSet());

            assertTrue(actuals.containsAll(ownerLabels));
        }

        [Then(@"attribute\\( ?{type_label} ?) get owners do not contain:")]
        public void attribute_type_get_owners_do_not_contain(string typeLabel, List<string> ownerLabels)
        {
            AttributeType attributeType = SingleTransaction.Concepts.getAttributeType(typeLabel).Resolve();
            Set<string> actuals = attributeType
                .getOwners(SingleTransaction, emptySet())
                .map(t => t.getLabel().name())
                .collect(toSet());

            for (string ownerLabel : ownerLabels)
            {
                assertFalse(actuals.contains(ownerLabel));
            }
        }

        [Then(@"attribute\\( ?{type_label} ?) get owners explicit contain:")]
        public void attribute_type_get_owners_explicit_contain(string typeLabel, List<string> ownerLabels)
        {
            AttributeType attributeType = SingleTransaction.Concepts.getAttributeType(typeLabel).Resolve();
            Set<string> actuals = attributeType
                .getOwners(SingleTransaction, emptySet(), EXPLICIT)
                .map(t => t.getLabel().name())
                .collect(toSet());

            assertTrue(actuals.containsAll(ownerLabels));
        }

        [Then(@"attribute\\( ?{type_label} ?) get owners explicit do not contain:")]
        public void attribute_type_get_owners_explicit_do_not_contain(string typeLabel, List<string> ownerLabels)
        {
            AttributeType attributeType = SingleTransaction.Concepts.getAttributeType(typeLabel).Resolve();
            Set<string> actuals = attributeType
                .getOwners(SingleTransaction, emptySet(), EXPLICIT)
                .map(t => t.getLabel().name())
                .collect(toSet());

            for (string ownerLabel : ownerLabels)
            {
                assertFalse(actuals.contains(ownerLabel));
            }
        }
    }
}
