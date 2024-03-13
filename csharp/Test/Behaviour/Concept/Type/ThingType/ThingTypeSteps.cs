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

using DataTable = Gherkin.Ast.DataTable;
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
using static Vaticle.Typedb.Driver.Api.IConcept.Transitivity;
using static Vaticle.Typedb.Driver.Api.IThingType;
using static Vaticle.Typedb.Driver.Api.IThingType.Annotation;
using static Vaticle.Typedb.Driver.Test.Behaviour.RootLabel;

namespace Vaticle.Typedb.Driver.Test.Behaviour
{
    public partial class BehaviourSteps
    {
        public static IThingType GetThingType(string rootLabel, string typeLabel)
        {
            Util.ValidateRootLabel(rootLabel);

            if (rootLabel == ENTITY)
            {
                return Tx.Concepts.GetEntityType(typeLabel).Resolve();
            }

            if (rootLabel == ATTRIBUTE)
            {
                return Tx.Concepts.GetAttributeType(typeLabel).Resolve();
            }

            if (rootLabel == RELATION)
            {
                return Tx.Concepts.GetRelationType(typeLabel).Resolve();
            }

            throw new BehaviourTestException($"Label {rootLabel} is not accepted");
        }

        [Given(@"put (entity|relation) type: {}")]
        [When(@"put (entity|relation) type: {}")]
        public void PutThingType(string rootLabel, string typeLabel)
        {
            if (rootLabel == ENTITY)
            {
                Tx.Concepts.PutEntityType(typeLabel).Resolve();
                return;
            }

            if (rootLabel == RELATION)
            {
                Tx.Concepts.PutRelationType(typeLabel).Resolve();
                return;
            }

            throw new BehaviourTestException($"Label {rootLabel} is not accepted");
        }

        public Label GetScopedLabel(string scopedLabels)
        {
            string[] labels = scopedLabels.Split(":");
            if (labels.Count() == 1)
            {
                return new Label(labels[0]);
            }

            return new Label(labels[0], labels[1]);
        }

        public List<Annotation> GetAnnotations(IEnumerable<string> textAnnotations)
        {
            List<Annotation> annotations = new List<Annotation>();

            foreach (var textAnnotation in textAnnotations)
            {
                if (textAnnotation.Equals("key"))
                {
                    annotations.Add(NewKey());
                }
                else if (textAnnotation.Equals("unique"))
                {
                    annotations.Add(NewUnique());
                }
                else
                {
                    throw new BehaviourTestException($"The annotation {textAnnotation} has not been recognised");
                }
            }

            return annotations;
        }

        [When(@"delete (entity|attribute|relation|thing) type: ([a-zA-Z0-9-_]+)")]
        public void DeleteThingType(string rootLabel, string typeLabel)
        {
            GetThingType(rootLabel, typeLabel).Delete(Tx).Resolve();
        }

        [Then(@"delete (entity|attribute|relation|thing) type: ([a-zA-Z0-9-_]+); throws exception")]
        public void DeleteThingTypeThrowsException(string rootLabel, string typeLabel)
        {
            Assert.Throws<TypeDBDriverException>(() => DeleteThingType(rootLabel, typeLabel));
        }

        [Then(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) is null: (true|false)")]
        public void ThingTypeIsNull(string rootLabel, string typeLabel, bool isNull)
        {
            Assert.Equal(isNull, GetThingType(rootLabel, typeLabel) == null);
        }

        [When(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) set label: ([a-zA-Z0-9-_]+)")]
        public void ThingTypeSetLabel(string rootLabel, string typeLabel, string newLabel)
        {
            GetThingType(rootLabel, typeLabel).SetLabel(Tx, newLabel).Resolve();
        }

        [Then(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) get label: ([a-zA-Z0-9-_]+)")]
        public void ThingTypeGetLabel(string rootLabel, string typeLabel, string getLabel)
        {
            Assert.Equal(getLabel, GetThingType(rootLabel, typeLabel).Label.Name);
        }

        [Then(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) set abstract: (true|false); throws exception")]
        public void ThingTypeSetAbstractThrowsException(
            string rootLabel, string typeLabel, bool isAbstract)
        {
            Assert.Throws<TypeDBDriverException>(() => ThingTypeSetAbstract(rootLabel, typeLabel, isAbstract));
        }

        [When(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) set abstract: (true|false)")]
        [Then(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) set abstract: (true|false)")]
        public void ThingTypeSetAbstract(string rootLabel, string typeLabel, bool isAbstract)
        {
            IThingType thingType = GetThingType(rootLabel, typeLabel);

            if (isAbstract)
            {
                thingType.SetAbstract(Tx).Resolve();
            }
            else
            {
                thingType.UnsetAbstract(Tx).Resolve();
            }
        }

        [Then(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) is abstract: (true|false)")]
        public void ThingTypeIsAbstract(string rootLabel, string typeLabel, bool isAbstract)
        {
            Assert.Equal(isAbstract, GetThingType(rootLabel, typeLabel).IsAbstract());
        }

        [When(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) set supertype: ([a-zA-Z0-9-_]+)")]
        public void ThingTypeSetSupertype(string rootLabel, string typeLabel, string superLabel)
        {
            Util.ValidateRootLabel(rootLabel);
            
            if (rootLabel == ENTITY)
            {
                IEntityType entitySuperType = Tx.Concepts.GetEntityType(superLabel).Resolve();
                Tx.Concepts
                    .GetEntityType(typeLabel).Resolve()
                    .SetSupertype(Tx, entitySuperType).Resolve();

                return;
            }

            if (rootLabel == ATTRIBUTE)
            {
                IAttributeType attributeSuperType = Tx.Concepts.GetAttributeType(superLabel).Resolve();
                Tx.Concepts
                    .GetAttributeType(typeLabel).Resolve()
                    .SetSupertype(Tx, attributeSuperType).Resolve();

                return;
            }


            if (rootLabel == RELATION)
            {
                IRelationType relationSuperType = Tx.Concepts.GetRelationType(superLabel).Resolve();
                Tx.Concepts
                    .GetRelationType(typeLabel).Resolve()
                    .SetSupertype(Tx, relationSuperType).Resolve();

                return;
            }


            throw new BehaviourTestException($"Label {rootLabel} is not accepted");
        }

        [Then(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) set supertype: ([a-zA-Z0-9-_]+); throws exception")]
        public void ThingTypeSetSupertypeThrowsException(
            string rootLabel, string typeLabel, string superLabel)
        {
            Assert.Throws<TypeDBDriverException>(() => ThingTypeSetSupertype(rootLabel, typeLabel, superLabel));
        }

        [Then(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) get supertype: ([a-zA-Z0-9-_]+)")]
        public void ThingTypeGetSupertype(string rootLabel, string typeLabel, string superLabel)
        {
            IThingType supertype = GetThingType(rootLabel, superLabel);
            Assert.Equal(
                supertype,
                GetThingType(rootLabel, typeLabel).GetSupertype(Tx).Resolve());
        }

        [Then(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) get supertypes contain:")]
        public void ThingTypeGetSupertypesContain(
            string rootLabel, string typeLabel, DataTable superLabelsData)
        {
            List<string> superLabels = Util.ParseDataTableToTypeList<string>(superLabelsData, val => val.ToString());

            IThingType thing_type = GetThingType(rootLabel, typeLabel);
            HashSet<string> actuals = thing_type
                .GetSupertypes(Tx)
                .Select(t => t.Label.Name)
                .ToHashSet();

            Assert.False(superLabels.Except(actuals).Any());
        }

        [Then(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) get supertypes do not contain:")]
        public void ThingTypeGetSupertypesDoNotContain(
            string rootLabel, string typeLabel, DataTable superLabelsData)
        {
            var superLabels = Util.ParseDataTableToTypeList<string>(superLabelsData, val => val.ToString());

            HashSet<string> actuals = GetThingType(rootLabel, typeLabel)
                .GetSupertypes(Tx)
                .Select(t => t.Label.Name)
                .ToHashSet();

            foreach (string superLabel in superLabels)
            {
                Assert.False(actuals.Contains(superLabel));
            }
        }

        [Then(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) get subtypes contain:")]
        public void ThingTypeGetSubtypesContain(string rootLabel, string typeLabel, DataTable subLabelsData)
        {
            var subLabels = Util.ParseDataTableToTypeList<string>(subLabelsData, val => val.ToString());

            HashSet<string> actuals = GetThingType(rootLabel, typeLabel)
                .GetSubtypes(Tx)
                .Select(t => t.Label.Name)
                .ToHashSet();

            Assert.False(subLabels.Except(actuals).Any());
        }

        [Then(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) get subtypes do not contain:")]
        public void ThingTypeGetSubtypesDoNotContain(
            string rootLabel, string typeLabel, DataTable subLabelsData)
        {
            var subLabels = Util.ParseDataTableToTypeList<string>(subLabelsData, val => val.ToString());

            HashSet<string> actuals = GetThingType(rootLabel, typeLabel)
                .GetSubtypes(Tx)
                .Select(t => t.Label.Name)
                .ToHashSet();

            foreach (string subLabel in subLabels)
            {
                Assert.False(actuals.Contains(subLabel));
            }
        }

        [Given(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) set owns attribute type: ([a-zA-Z0-9-_]+), with annotations: (\s*([\w\-_]+,\s*)*[\w\-_]*\s*)")]
        [When(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) set owns attribute type: ([a-zA-Z0-9-_]+), with annotations: (\s*([\w\-_]+,\s*)*[\w\-_]*\s*)")]
        [Then(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) set owns attribute type: ([a-zA-Z0-9-_]+), with annotations: (\s*([\w\-_]+,\s*)*[\w\-_]*\s*)")]
        public void ThingTypeSetOwnsAttributeTypeWithAnnotations(
            string rootLabel, string typeLabel, string attTypeLabel, string annotationsData)
        {
            List<Annotation> annotations = GetAnnotations(Util.ParseEnumerationToList(annotationsData));
        
            IAttributeType attributeType = Tx.Concepts.GetAttributeType(attTypeLabel).Resolve();
            GetThingType(rootLabel, typeLabel).SetOwns(Tx, attributeType, annotations).Resolve();
        }

        [Given(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) set owns attribute type: ([a-zA-Z0-9-_]+) as ([a-zA-Z0-9-_]+), with annotations: (\s*([\w\-_]+,\s*)*[\w\-_]*\s*)")]
        [When(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) set owns attribute type: ([a-zA-Z0-9-_]+) as ([a-zA-Z0-9-_]+), with annotations: (\s*([\w\-_]+,\s*)*[\w\-_]*\s*)")]
        public void ThingTypeSetOwnsAttributeTypeAsTypeWithAnnotations(
            string rootLabel,
            string typeLabel,
            string attTypeLabel,
            string overriddenLabel,
            string annotationsData)
        {
            List<Annotation> annotations = GetAnnotations(Util.ParseEnumerationToList(annotationsData));

            IAttributeType attributeType = Tx.Concepts.GetAttributeType(attTypeLabel).Resolve();
            IAttributeType overriddenType = Tx.Concepts.GetAttributeType(overriddenLabel).Resolve();

            GetThingType(rootLabel, typeLabel)
                .SetOwns(Tx, attributeType, overriddenType, annotations).Resolve();
        }

        [When(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) set owns attribute type: ([a-zA-Z0-9-_]+), with annotations: (\s*([\w\-_]+,\s*)*[\w\-_]*\s*); throws exception")]
        [Then(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) set owns attribute type: ([a-zA-Z0-9-_]+), with annotations: (\s*([\w\-_]+,\s*)*[\w\-_]*\s*); throws exception")]
        public void ThingTypeSetOwnsAttributeTypeWithAnnotationsThrowsException(
            string rootLabel, string typeLabel, string attributeLabel, string annotationsData)
        {
            Assert.Throws<TypeDBDriverException>(() => ThingTypeSetOwnsAttributeTypeWithAnnotations(
                rootLabel, typeLabel, attributeLabel, annotationsData));
        }

        [Then(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) set owns attribute type: ([a-zA-Z0-9-_]+) as ([a-zA-Z0-9-_]+), with annotations: (\s*([\w\-_]+,\s*)*[\w\-_]*\s*); throws exception")]
        public void ThingTypeSetOwnsAttributeTypeAsTypeWithAnnotationsThrowsException(
            string rootLabel,
            string typeLabel,
            string attributeLabel,
            string overriddenLabel,
            string annotationsData)
        {
            Assert.Throws<TypeDBDriverException>(() => ThingTypeSetOwnsAttributeTypeAsTypeWithAnnotations(
                rootLabel, typeLabel, attributeLabel, overriddenLabel, annotationsData));
        }

        [Then(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) get owns attribute types, with annotations: (\s*([\w\-_]+,\s*)*[\w\-_]*\s*); contain:")]
        public void ThingTypeGetOwnsAttributeTypesWithAnnotationsContain(
            string rootLabel, string typeLabel, string annotationsData, DataTable attributeLabelsData)
        {
            List<Annotation> annotations = GetAnnotations(Util.ParseEnumerationToList(annotationsData));

            var attributeLabels = Util.ParseDataTableToTypeList<string>(attributeLabelsData, val => val.ToString());

            HashSet<string> actuals = GetThingType(rootLabel, typeLabel)
                .GetOwns(Tx, annotations)
                .Select(t => t.Label.Name)
                .ToHashSet();

            Assert.False(attributeLabels.Except(actuals).Any());
        }

        [Then(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) get owns attribute types, with annotations: (\s*([\w\-_]+,\s*)*[\w\-_]*\s*); do not contain:")]
        public void ThingTypeGetOwnsAttributeTypesWithAnnotationsDoNotContain(
            string rootLabel, string typeLabel, string annotationsData, DataTable attributeLabelsData)
        {
            List<Annotation> annotations = GetAnnotations(Util.ParseEnumerationToList(annotationsData));

            var attributeLabels = Util.ParseDataTableToTypeList<string>(attributeLabelsData, val => val.ToString());

            HashSet<string> actuals = GetThingType(rootLabel, typeLabel)
                .GetOwns(Tx, annotations)
                .Select(t => t.Label.Name)
                .ToHashSet();

            foreach (string attributeLabel in attributeLabels)
            {
                Assert.False(actuals.Contains(attributeLabel));
            }
        }

        [Then(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) get owns explicit attribute types, with annotations: (\s*([\w\-_]+,\s*)*[\w\-_]*\s*); contain:")]
        public void ThingTypeGetOwnsExplicitAttributeTypesWithAnnotationsContain(
            string rootLabel, string typeLabel, string annotationsData, DataTable attributeLabelsData)
        {
            List<Annotation> annotations = GetAnnotations(Util.ParseEnumerationToList(annotationsData));

            var attributeLabels = Util.ParseDataTableToTypeList<string>(attributeLabelsData, val => val.ToString());

            HashSet<string> actuals = GetThingType(rootLabel, typeLabel)
                .GetOwns(Tx, annotations, EXPLICIT)
                .Select(t => t.Label.Name)
                .ToHashSet();

            Assert.False(attributeLabels.Except(actuals).Any());
        }

        [Then(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) get owns explicit attribute types, with annotations: (\s*([\w\-_]+,\s*)*[\w\-_]*\s*); do not contain:")]
        public void ThingTypeGetOwnsExplicitAttributeTypesWithAnnotationsDoNotContain(
            string rootLabel, string typeLabel, string annotationsData, DataTable attributeLabelsData)
        {
            List<Annotation> annotations = GetAnnotations(Util.ParseEnumerationToList(annotationsData));

            var attributeLabels = Util.ParseDataTableToTypeList<string>(attributeLabelsData, val => val.ToString());

            HashSet<string> actuals = GetThingType(rootLabel, typeLabel)
                .GetOwns(Tx, annotations, EXPLICIT)
                .Select(t => t.Label.Name)
                .ToHashSet();

            foreach (string attributeLabel in attributeLabels)
            {
                Assert.False(actuals.Contains(attributeLabel));
            }
        }

        [Given(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) set owns attribute type: ([a-zA-Z0-9-_]+)")]
        [When(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) set owns attribute type: ([a-zA-Z0-9-_]+)")]
        [Then(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) set owns attribute type: ([a-zA-Z0-9-_]+)")]
        public void ThingTypeSetOwnsAttributeType(
            string rootLabel, string typeLabel, string attributeLabel)
        {
            IAttributeType attributeType = Tx.Concepts.GetAttributeType(attributeLabel).Resolve();
            GetThingType(rootLabel, typeLabel).SetOwns(Tx, attributeType).Resolve();
        }

        [When(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) set owns attribute type: ([a-zA-Z0-9-_]+); throws exception")]
        [Then(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) set owns attribute type: ([a-zA-Z0-9-_]+); throws exception")]
        public void ThingTypeSetOwnsAttributeTypeThrowsException(
            string rootLabel, string typeLabel, string attributeLabel)
        {
            Assert.Throws<TypeDBDriverException>(() => ThingTypeSetOwnsAttributeType(rootLabel, typeLabel, attributeLabel));
        }

        [When(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) set owns attribute type: ([a-zA-Z0-9-_]+) as ([a-zA-Z0-9-_]+)")]
        public void ThingTypeSetOwnsAttributeTypeAs(
            string rootLabel, string typeLabel, string attributeLabel, string overriddenLabel)
        {
            IAttributeType attributeType = Tx.Concepts.GetAttributeType(attributeLabel).Resolve();
            IAttributeType overriddenType = Tx.Concepts.GetAttributeType(overriddenLabel).Resolve();

            GetThingType(rootLabel, typeLabel).SetOwns(Tx, attributeType, overriddenType).Resolve();
        }

        [Then(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) set owns attribute type: ([a-zA-Z0-9-_]+) as ([a-zA-Z0-9-_]+); throws exception")]
        public void ThingTypeSetowns_attributeAsThrowsException(
            string rootLabel, string typeLabel, string attributeLabel, string overriddenLabel)
        {
            Assert.Throws<TypeDBDriverException>(() =>
                ThingTypeSetOwnsAttributeTypeAs(rootLabel, typeLabel, attributeLabel, overriddenLabel));
        }

        [When(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) unset owns attribute type: ([a-zA-Z0-9-_]+)")]
        public void ThingTypeUnsetOwnsAttributeType(
            string rootLabel, string typeLabel, string attributeLabel)
        {
            IAttributeType attributeType = Tx.Concepts.GetAttributeType(attributeLabel).Resolve();
            GetThingType(rootLabel, typeLabel).UnsetOwns(Tx, attributeType).Resolve();
        }

        [Then(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) unset owns attribute type: ([a-zA-Z0-9-_]+); throws exception")]
        public void ThingTypeUnsetOwnsAttributeTypeThrowsException(
            string rootLabel, string typeLabel, string attributeLabel)
        {
            Assert.Throws<TypeDBDriverException>(() =>
                ThingTypeUnsetOwnsAttributeType(rootLabel, typeLabel, attributeLabel));
        }

        [Then(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) get owns overridden attribute\(([a-zA-Z0-9-_]+)\) is null: (true|false)")]
        public void ThingTypeGetOwnsOverriddenAttributeIsNull(
            string rootLabel, string typeLabel, string attributeLabel, bool isNull)
        {
            IAttributeType attributeType = Tx.Concepts.GetAttributeType(attributeLabel).Resolve();

            var ownsOverridden = GetThingType(rootLabel, typeLabel)
                .GetOwnsOverridden(Tx, attributeType).Resolve();

            Assert.Equal(isNull, ownsOverridden == null);
        }

        [Then(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) get owns overridden attribute\(([a-zA-Z0-9-_]+)\) get label: ([a-zA-Z0-9-_]+)")]
        public void ThingTypeGetOwnsOverriddenAttributeGetLabel(
            string rootLabel, string typeLabel, string attributeLabel, string getLabel)
        {
            IAttributeType attributeType = Tx.Concepts.GetAttributeType(attributeLabel).Resolve();

            var ownsOverridden = GetThingType(rootLabel, typeLabel)
                .GetOwnsOverridden(Tx, attributeType).Resolve();

            Assert.Equal(getLabel, ownsOverridden.Label.Name);
        }

        [Then(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) get owns attribute types contain:")]
        public void ThingTypeGetOwnsAttributeTypesContain(
            string rootLabel, string typeLabel, DataTable attributeLabelsData)
        {
            var attributeLabels = Util.ParseDataTableToTypeList<string>(attributeLabelsData, val => val.ToString());

            HashSet<string> actuals = GetThingType(rootLabel, typeLabel)
                .GetOwns(Tx)
                .Select(at => at.Label.Name)
                .ToHashSet();

            Assert.False(attributeLabels.Except(actuals).Any());
        }

        [Then(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) get owns attribute types do not contain:")]
        public void ThingTypeGetOwnsAttributeTypesDoNotContain(
            string rootLabel, string typeLabel, DataTable attributeLabelsData)
        {
            var attributeLabels = Util.ParseDataTableToTypeList<string>(attributeLabelsData, val => val.ToString());

            HashSet<string> actuals = GetThingType(rootLabel, typeLabel)
                .GetOwns(Tx)
                .Select(at => at.Label.Name)
                .ToHashSet();

            foreach (string attributeLabel in attributeLabels)
            {
                Assert.False(actuals.Contains(attributeLabel));
            }
        }

        [Then(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) get owns explicit attribute types contain:")]
        public void ThingTypeGetOwnsExplicitAttributeTypesContain(
            string rootLabel, string typeLabel, DataTable attributeLabelsData)
        {
            var attributeLabels = Util.ParseDataTableToTypeList<string>(attributeLabelsData, val => val.ToString());

            HashSet<string> actuals = GetThingType(rootLabel, typeLabel)
                .GetOwns(Tx, EXPLICIT)
                .Select(at => at.Label.Name)
                .ToHashSet();

            Assert.False(attributeLabels.Except(actuals).Any());
        }

        [Then(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) get owns explicit attribute types do not contain:")]
        public void ThingTypeGetOwnsExplicitAttributeTypesDoNotContain(
            string rootLabel, string typeLabel, DataTable attributeLabelsData)
        {
            var attributeLabels = Util.ParseDataTableToTypeList<string>(attributeLabelsData, val => val.ToString());

            HashSet<string> actuals = GetThingType(rootLabel, typeLabel)
                .GetOwns(Tx, EXPLICIT)
                .Select(at => at.Label.Name)
                .ToHashSet();

            foreach (string attributeLabel in attributeLabels)
            {
                Assert.False(actuals.Contains(attributeLabel));
            }
        }

        [Given(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) set plays role: ([a-zA-Z0-9-_]+:[a-zA-Z0-9-_]+)")]
        [When(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) set plays role: ([a-zA-Z0-9-_]+:[a-zA-Z0-9-_]+)")]
        public void ThingTypeSetPlaysRole(string rootLabel, string typeLabel, string roleLabelData)
        {
            Label roleLabel = GetScopedLabel(roleLabelData);

            IRoleType roleType = Tx.Concepts
                .GetRelationType(roleLabel.Scope).Resolve()
                .GetRelates(Tx, roleLabel.Name).Resolve();

            GetThingType(rootLabel, typeLabel).SetPlays(Tx, roleType).Resolve();
        }

        [Then(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) set plays role: ([a-zA-Z0-9-_]+:[a-zA-Z0-9-_]+); throws exception")]
        public void ThingTypeSetPlaysRoleThrowsException(string rootLabel, string typeLabel, string roleLabelData)
        {
            Assert.Throws<TypeDBDriverException>(() => ThingTypeSetPlaysRole(rootLabel, typeLabel, roleLabelData));
        }

        [When(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) set plays role: ([a-zA-Z0-9-_]+:[a-zA-Z0-9-_]+) as ([a-zA-Z0-9-_]+:[a-zA-Z0-9-_]+)")]
        public void ThingTypeSetPlaysRoleAs(
            string rootLabel, string typeLabel, string roleLabelData, string overriddenLabelData)
        {
            Label roleLabel = GetScopedLabel(roleLabelData);
            Label overriddenLabel = GetScopedLabel(overriddenLabelData);

            IRoleType roleType = Tx.Concepts
                .GetRelationType(roleLabel.Scope).Resolve()
                .GetRelates(Tx, roleLabel.Name).Resolve();

            IRoleType overriddenType = Tx.Concepts
                .GetRelationType(overriddenLabel.Scope).Resolve()
                .GetRelates(Tx, overriddenLabel.Name).Resolve();

            GetThingType(rootLabel, typeLabel)
                .SetPlays(Tx, roleType, overriddenType).Resolve();
        }

        [Then(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) set plays role: ([a-zA-Z0-9-_]+:[a-zA-Z0-9-_]+) as ([a-zA-Z0-9-_]+:[a-zA-Z0-9-_]+); throws exception")]
        public void ThingTypeSetPlaysRoleAsThrowsException(
            string rootLabel, string typeLabel, string roleLabelData, string overriddenLabelData)
        {
            Assert.Throws<TypeDBDriverException>(() =>
                ThingTypeSetPlaysRoleAs(rootLabel, typeLabel, roleLabelData, overriddenLabelData));
        }

        [When(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) unset plays role: ([a-zA-Z0-9-_]+:[a-zA-Z0-9-_]+)")]
        [Then(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) unset plays role: ([a-zA-Z0-9-_]+:[a-zA-Z0-9-_]+)")]
        public void ThingTypeUnsetPlaysRole(string rootLabel, string typeLabel, string roleLabelData)
        {
            Label roleLabel = GetScopedLabel(roleLabelData);

            IRoleType roleType = Tx.Concepts
                .GetRelationType(roleLabel.Scope).Resolve()
                .GetRelates(Tx, roleLabel.Name).Resolve();

            GetThingType(rootLabel, typeLabel).UnsetPlays(Tx, roleType).Resolve();
        }

        [Then(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) unset plays role: ([a-zA-Z0-9-_]+:[a-zA-Z0-9-_]+); throws exception")]
        public void ThingTypeUnsetPlaysRoleThrowsException(
            string rootLabel, string typeLabel, string roleLabelData)
        {
            Assert.Throws<TypeDBDriverException>(() => ThingTypeUnsetPlaysRole(rootLabel, typeLabel, roleLabelData));
        }

        [Then(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) get playing roles contain:")]
        public void ThingTypeGetPlayingRolesContain(
            string rootLabel, string typeLabel, DataTable roleLabelsData)
        {
            List<Label> roleLabels = Util.ParseDataTableToTypeList<Label>(roleLabelsData, val => GetScopedLabel(val));

            HashSet<Label> actuals = GetThingType(rootLabel, typeLabel)
                .GetPlays(Tx)
                .Select(obj => obj.Label)
                .ToHashSet();

            Assert.False(roleLabels.Except(actuals).Any());
        }

        [Then(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) get playing roles do not contain:")]
        public void ThingTypeGetPlayingRolesDoNotContain(
            string rootLabel, string typeLabel, DataTable roleLabelsData)
        {
            List<Label> roleLabels = Util.ParseDataTableToTypeList<Label>(roleLabelsData, val => GetScopedLabel(val));

            HashSet<Label> actuals = GetThingType(rootLabel, typeLabel)
                .GetPlays(Tx)
                .Select(obj => obj.Label)
                .ToHashSet();

            foreach (Label roleLabel in roleLabels)
            {
                Assert.False(actuals.Contains(roleLabel));
            }
        }

        [Then(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) get playing roles explicit contain:")]
        public void ThingTypeGetPlayingRolesExplicitContain(
            string rootLabel, string typeLabel, DataTable roleLabelsData)
        {
            List<Label> roleLabels = Util.ParseDataTableToTypeList<Label>(roleLabelsData, val => GetScopedLabel(val));

            HashSet<Label> actuals = GetThingType(rootLabel, typeLabel)
                .GetPlays(Tx, EXPLICIT)
                .Select(obj => obj.Label)
                .ToHashSet();

            Assert.False(roleLabels.Except(actuals).Any());
        }

        [Then(@"(entity|attribute|relation|thing)\(([a-zA-Z0-9-_]+)\) get playing roles explicit do not contain:")]
        public void ThingTypeGetPlayingRolesExplicitDoNotContain(
            string rootLabel, string typeLabel, DataTable roleLabelsData)
        {
            List<Label> roleLabels = Util.ParseDataTableToTypeList<Label>(roleLabelsData, val => GetScopedLabel(val));

            HashSet<Label> actuals = GetThingType(rootLabel, typeLabel)
                .GetPlays(Tx, EXPLICIT)
                .Select(obj => obj.Label)
                .ToHashSet();

            foreach (Label roleLabel in roleLabels)
            {
                Assert.False(actuals.Contains(roleLabel));
            }
        }
    }
}
