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
using static Vaticle.Typedb.Driver.Api.IConcept.Transitivity;

using Vaticle.Typedb.Driver;
using Vaticle.Typedb.Driver.Api;
using Vaticle.Typedb.Driver.Common;

namespace Vaticle.Typedb.Driver.Test.Behaviour
{
    public partial class BehaviourSteps
    {
        [When(@"put attribute type: ([a-zA-Z0-9-_]+), with value type: {value_type}")] // TODO: Add!
        public void PutAttributeTypeWithValueType(string typeLabel, Value.Type valueType)
        {
            Tx.Concepts.putAttributeType(typeLabel, valueType).Resolve();
        }

        [Then(@"attribute\\( ?([a-zA-Z0-9-_]+) ?) get value type: {value_type}")]
        public void AttributeTypeGetValueType(string typeLabel, Value.Type valueType)
        {
            Assert.Equals(
                valueType,
                Tx.Concepts.GetAttributeType(typeLabel).Resolve().GetValueType());
        }

        [Then(@"attribute\\( ?([a-zA-Z0-9-_]+) ?) get supertype value type: {value_type}")]
        public void AttributeTypeGetSupertypeValueType(string typeLabel, Value.Type valueType)
        {
            AttributeType supertype = Tx.Concepts
                .GetAttributeType(typeLabel).Resolve()
                .GetSupertype(Tx).Resolve()
                .AsAttributeType();

            Assert.Equals(valueType, supertype.GetValueType());
        }

        [Then(@"attribute\\( ?([a-zA-Z0-9-_]+) ?) as\\( ?{value_type} ?) get subtypes contain:")]
        public void AttributeTypeAsValueTypeGetSubtypesContain(
            string typeLabel, Value.Type valueType, DataTable subLabelsData)
        {
            var subLabels = ParseDataTableToTypeList<string>(subLabelsData);

            AttributeType attributeType = Tx.Concepts.GetAttributeType(typeLabel).Resolve();
            HashSet<string> actuals = attributeType
                .GetSubtypes(Tx, valueType)
                .Select(t => t.Label.Name)
                .ToHashSet();

            Assert.False(actuals.Except(subLabels).Any());
        }

        [Then(@"attribute\\( ?([a-zA-Z0-9-_]+) ?) as\\( ?{value_type} ?) get subtypes do not contain:")]
        public void AttributeTypeAsValueTypeGetSubtypesDoNotContain(
        string typeLabel, Value.Type valueType, DataTable subLabelsData)
        {
            var subLabels = ParseDataTableToTypeList<string>(subLabelsData);

            AttributeType attributeType = Tx.Concepts.GetAttributeType(typeLabel).Resolve();
            HashSet<string> actuals = attributeType
                .GetSubtypes(Tx, valueType)
                .Select(t => t.Label.Name)
                .ToHashSet();

            foreach (string subLabel in subLabels)
            {
                Assert.False(actuals.Contains(subLabel));
            }
        }

        [Then(@"attribute\\( ?([a-zA-Z0-9-_]+) ?) as\\( ?{value_type} ?) set regex: {}")]
        public void AttributeTypeAsValueTypeSetRegex(string typeLabel, Value.Type valueType, string regex)
        {
            if (!valueType.Equals(Value.Type.STRING))
            {
                fail();
            }

            AttributeType attributeType = Tx.Concepts.GetAttributeType(typeLabel).Resolve();
            attributeType.SetRegex(Tx, regex).Resolve();
        }

        [Then(@"attribute\\( ?([a-zA-Z0-9-_]+) ?) as\\( ?{value_type} ?) unset regex")]
        public void AttributeTypeAsValueTypeUnsetRegex(string typeLabel, Value.Type valueType)
        {
            if (!valueType.Equals(Value.Type.STRING))
            {
                fail();
            }

            AttributeType attributeType = Tx.Concepts.GetAttributeType(typeLabel).Resolve();
            attributeType.unsetRegex(Tx).Resolve();
        }

        [Then(@"attribute\\( ?([a-zA-Z0-9-_]+) ?) as\\( ?{value_type} ?) get regex: {}")]
        public void AttributeTypeAsValueTypeGetRegex(string typeLabel, Value.Type valueType, string regex)
        {
            if (!valueType.Equals(Value.Type.STRING))
            {
                fail();
            }

            AttributeType attributeType = Tx.Concepts.GetAttributeType(typeLabel).Resolve();
            Assert.Equals(regex, attributeType.GetRegex(Tx).Resolve());
        }

        [Then(@"attribute\\( ?([a-zA-Z0-9-_]+) ?) as\\( ?{value_type} ?) does not have any regex")]
        public void AttributeTypeAsValueTypeDoesNotHaveAnyRegex(string typeLabel, Value.Type valueType)
        {
            AttributeTypeAsValueTypeGetRegex(typeLabel, valueType, null);
        }

        [Then(@"attribute\\( ?([a-zA-Z0-9-_]+) ?) get owners, with annotations: (\s*([\w\-_]+,\s*)*[\w\-_]*\s*); contain:")]
        public void AttributeTypeGetOwnersWithAnnotationsContain(
            string typeLabel, string annotationsData, DataTable ownerLabelsData)
        {
            List<Annotation> annotations = GetAnnotations(Utils.ParseEnumerationToList(annotationsData));
        
            var ownerLabels = ParseDataTableToTypeList<string>(ownerLabelsData);

            AttributeType attributeType = Tx.Concepts.GetAttributeType(typeLabel).Resolve();
            HashSet<string> actuals = attributeType
                .GetOwners(Tx, new []{annotations})
                .Select(t => t.Label.Name)
                .ToHashSet();

            Assert.False(actuals.Except(ownerLabels).Any());
        }

        [Then(@"attribute\\( ?([a-zA-Z0-9-_]+) ?) get owners, with annotations: (\s*([\w\-_]+,\s*)*[\w\-_]*\s*); do not contain:")]
        public void AttributeTypeGetOwnersWithAnnotationsDoNotContain(
            string typeLabel, string annotationsData, DataTable ownerLabelsData)
        {
            List<Annotation> annotations = GetAnnotations(Utils.ParseEnumerationToList(annotationsData));

            var ownerLabels = ParseDataTableToTypeList<string>(ownerLabelsData);

            AttributeType attributeType = Tx.Concepts.GetAttributeType(typeLabel).Resolve();
            HashSet<string> actuals = attributeType
                .GetOwners(Tx, new []{annotations})
                .Select(t => t.Label.Name)
                .ToHashSet();

            foreach (string ownerLabel in ownerLabels)
            {
                Assert.False(actuals.Contains(ownerLabel));
            }
        }

        [Then(@"attribute\\( ?([a-zA-Z0-9-_]+) ?) get owners explicit, with annotations: (\s*([\w\-_]+,\s*)*[\w\-_]*\s*); contain:")]
        public void AttributeTypeGetOwnersExplicitWithAnnotationsContain(
            string typeLabel, string annotationsData, DataTable ownerLabelsData)
        {
            List<Annotation> annotations = GetAnnotations(Utils.ParseEnumerationToList(annotationsData));

            var ownerLabels = ParseDataTableToTypeList<string>(ownerLabelsData);

            AttributeType attributeType = Tx.Concepts.GetAttributeType(typeLabel).Resolve();
            HashSet<string> actuals = attributeType
                .GetOwners(Tx, new []{annotations}, EXPLICIT)
                .Select(t => t.Label.Name)
                .ToHashSet();

            Assert.False(actuals.Except(ownerLabels).Any());
        }

        [Then(@"attribute\\( ?([a-zA-Z0-9-_]+) ?) get owners explicit, with annotations: (\s*([\w\-_]+,\s*)*[\w\-_]*\s*); do not contain:")]
        public void AttributeTypeGetOwnersExplicitWithAnnotationsDoNotContain(
            string typeLabel, string annotationsData, DataTable ownerLabelsData)
        {
            List<Annotation> annotations = GetAnnotations(Utils.ParseEnumerationToList(annotationsData));

            var ownerLabels = ParseDataTableToTypeList<string>(ownerLabelsData);

            AttributeType attributeType = Tx.Concepts.GetAttributeType(typeLabel).Resolve();
            HashSet<string> actuals = attributeType
                .GetOwners(Tx, new []{annotations}, EXPLICIT)
                .Select(t => t.Label.Name)
                .ToHashSet();

            foreach (string ownerLabel in ownerLabels)
            {
                Assert.False(actuals.Contains(ownerLabel));
            }
        }

        [Then(@"attribute\\( ?([a-zA-Z0-9-_]+) ?) get owners contain:")]
        public void AttributeTypeGetOwnersContain(string typeLabel, DataTable ownerLabelsData)
        {
            var ownerLabels = ParseDataTableToTypeList<string>(ownerLabelsData);

            AttributeType attributeType = Tx.Concepts.GetAttributeType(typeLabel).Resolve();
            HashSet<string> actuals = attributeType
                .GetOwners(Tx, new Annotation[0])
                .Select(t => t.Label.Name)
                .ToHashSet();

            Assert.False(actuals.Except(ownerLabels).Any());
        }

        [Then(@"attribute\\( ?([a-zA-Z0-9-_]+) ?) get owners do not contain:")]
        public void AttributeTypeGetOwnersDoNotContain(string typeLabel, DataTable ownerLabelsData)
        {
            var ownerLabels = ParseDataTableToTypeList<string>(ownerLabelsData);

            AttributeType attributeType = Tx.Concepts.GetAttributeType(typeLabel).Resolve();
            HashSet<string> actuals = attributeType
                .GetOwners(Tx, new Annotation[0])
                .Select(t => t.Label.Name)
                .ToHashSet();

            foreach (string ownerLabel in ownerLabels)
            {
                Assert.False(actuals.Contains(ownerLabel));
            }
        }

        [Then(@"attribute\\( ?([a-zA-Z0-9-_]+) ?) get owners explicit contain:")]
        public void AttributeTypeGetOwnersExplicitContain(string typeLabel, DataTable ownerLabelsData)
        {
            var ownerLabels = ParseDataTableToTypeList<string>(ownerLabelsData);

            AttributeType attributeType = Tx.Concepts.GetAttributeType(typeLabel).Resolve();
            HashSet<string> actuals = attributeType
                .GetOwners(Tx, new Annotation[0], EXPLICIT)
                .Select(t => t.Label.Name)
                .ToHashSet();

            Assert.False(actuals.Except(ownerLabels).Any());
        }

        [Then(@"attribute\\( ?([a-zA-Z0-9-_]+) ?) get owners explicit do not contain:")]
        public void AttributeTypeGetOwnersExplicitDoNotContain(string typeLabel, DataTable ownerLabelsData)
        {
            var ownerLabels = ParseDataTableToTypeList<string>(ownerLabelsData);

            AttributeType attributeType = Tx.Concepts.GetAttributeType(typeLabel).Resolve();
            HashSet<string> actuals = attributeType
                .GetOwners(Tx, new Annotation[0], EXPLICIT)
                .Select(t => t.Label.Name)
                .ToHashSet();

            foreach (string ownerLabel in ownerLabels)
            {
                Assert.False(actuals.Contains(ownerLabel));
            }
        }
    }
}
