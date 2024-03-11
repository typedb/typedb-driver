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
using static Vaticle.Typedb.Driver.Api.IConcept.Transitivity;
using static Vaticle.Typedb.Driver.Api.IThingType.Annotation;
using static Vaticle.Typedb.Driver.Test.Behaviour.RootLabel;

namespace Vaticle.Typedb.Driver.Test.Behaviour
{
    public partial class BehaviourSteps
    {
        public static ThingType GetThingType(string rootLabel, string typeLabel)
        {
            Util.ValidateRootLabel(rootLabel);

            switch (rootLabel)
            {
                case ENTITY:
                    return Tx.Concepts.GetEntityType(typeLabel).Resolve();

                case ATTRIBUTE:
                    return Tx.Concepts.GetAttributeType(typeLabel).Resolve();

                case RELATION:
                    return Tx.Concepts.GetRelationType(typeLabel).Resolve();

                default:
                    throw new BehaviourTestException($"Label {rootLabel} is not accepted");
            }
        }

        [When(@"put {} type: {}")]
        public void PutThingType(string rootLabel, string typeLabel)
        {
            switch (rootLabel)
            {
                case ENTITY:
                    Tx.Concepts.PutEntityType(typeLabel).Resolve();
                    break;

                case RELATION:
                    Tx.Concepts.PutRelationType(typeLabel).Resolve();
                    break;

                default:
                    throw new BehaviourTestException($"Label {rootLabel} is not accepted");
            }
        }

        public List<Annotations> GetAnnotations(List<string> textAnnotations)
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

        [When(@"delete {} type: ([a-zA-Z0-9-_]+)")]
        public void DeleteThingType(string rootLabel, string typeLabel)
        {
            GetThingType(rootLabel, typeLabel).delete(Tx).Resolve();
        }

        [Then(@"delete {} type: ([a-zA-Z0-9-_]+); throws exception")]
        public void DeleteThingTypeThrowsException(string rootLabel, string typeLabel)
        {
            Assert.Throws<TypeDBDriverException>(() => DeleteThingType(rootLabel, typeLabel));
        }

        [Then(@"{}\\( ?([a-zA-Z0-9-_]+) ?) is null: {bool}")]
        public void ThingTypeIsNull(string rootLabel, string typeLabel, bool isNull)
        {
            Assert.Equals(isNull, isNull(GetThingType(rootLabel, typeLabel)));
        }

        [When(@"{}\\( ?([a-zA-Z0-9-_]+) ?) set label: ([a-zA-Z0-9-_]+)")]
        public void ThingTypeSetLabel(string rootLabel, string typeLabel, string newLabel)
        {
            GetThingType(rootLabel, typeLabel).SetLabel(Tx, newLabel).Resolve();
        }

        [Then(@"{}\\( ?([a-zA-Z0-9-_]+) ?) get label: ([a-zA-Z0-9-_]+)")]
        public void ThingTypeGetLabel(string rootLabel, string typeLabel, string getLabel)
        {
            Assert.Equals(getLabel, GetThingType(rootLabel, typeLabel).Label.Name);
        }

        [When(@"{}\\( ?([a-zA-Z0-9-_]+) ?) set abstract: {bool}")]
        public void ThingTypeSetAbstract(string rootLabel, string typeLabel, bool isAbstract)
        {
            ThingType thingType = GetThingType(rootLabel, typeLabel);

            if (isAbstract)
            {
                thingType.SetAbstract(Tx).Resolve();
            }
            else
            {
                thingType.unsetAbstract(Tx).Resolve();
            }
        }

        [When(@"{}\\( ?([a-zA-Z0-9-_]+) ?) set abstract: {bool}; throws exception")]
        public void ThingTypeSetAbstractThrowsException(
            string rootLabel, string typeLabel, bool isAbstract)
        {
            Assert.Throws<TypeDBDriverException>(() => ThingTypeSetAbstract(rootLabel, typeLabel, isAbstract));
        }

        [Then(@"{}\\( ?([a-zA-Z0-9-_]+) ?) is abstract: {bool}")]
        public void ThingTypeIsAbstract(string rootLabel, string typeLabel, bool isAbstract)
        {
            Assert.Equals(isAbstract, GetThingType(rootLabel, typeLabel).IsAbstract());
        }

        [When(@"{}\\( ?([a-zA-Z0-9-_]+) ?) set supertype: ([a-zA-Z0-9-_]+)")]
        public void ThingTypeSetSupertype(string rootLabel, string typeLabel, string superLabel)
        {
            Util.ValidateRootLabel(rootLabel);
            
            switch (rootLabel)
            {
                case ENTITY:
                    EntityType entitySuperType = Tx.Concepts.GetEntityType(superLabel).Resolve();
                    Tx.Concepts
                        .GetEntityType(typeLabel).Resolve()
                        .SetSupertype(Tx, entitySuperType).Resolve();
                    break;

                case ATTRIBUTE:
                    AttributeType attributeSuperType = Tx.Concepts.GetAttributeType(superLabel).Resolve();
                    Tx.Concepts
                        .GetAttributeType(typeLabel).Resolve()
                        .SetSupertype(Tx, attributeSuperType).Resolve();
                    break;

                case RELATION:
                    RelationType relationSuperType = Tx.Concepts.GetRelationType(superLabel).Resolve();
                    Tx.Concepts
                        .GetRelationType(typeLabel).Resolve()
                        .SetSupertype(Tx, relationSuperType).Resolve();
                    break;

                case THING:
                    throw new BehaviourTestException($"Label {rootLabel} is not accepted");
            }
        }

        [Then(@"{}\\( ?([a-zA-Z0-9-_]+) ?) set supertype: ([a-zA-Z0-9-_]+); throws exception")]
        public void ThingTypeSetSupertypeThrowsException(
            string rootLabel, string typeLabel, string superLabel)
        {
            Assert.Throws<TypeDBDriverException>(() => ThingTypeSetSupertype(rootLabel, typeLabel, superLabel));
        }

        [Then(@"{}\\( ?([a-zA-Z0-9-_]+) ?) get supertype: ([a-zA-Z0-9-_]+)")]
        public void ThingTypeGetSupertype(string rootLabel, string typeLabel, string superLabel)
        {
            ThingType supertype = GetThingType(rootLabel, superLabel);
            Assert.Equals(
                supertype,
                GetThingType(rootLabel, typeLabel).GetSupertype(Tx).Resolve());
        }

        [Then(@"{}\\( ?([a-zA-Z0-9-_]+) ?) get supertypes contain:")]
        public void ThingTypeGetSupertypesContain(
            string rootLabel, string typeLabel, DataTable superLabelsData)
        {
            List<string> superLabels = ParseDataTableToTypeList<string>(superLabelsData);

            ThingType thing_type = GetThingType(rootLabel, typeLabel);
            HashSet<string> actuals = thing_type
                .GetSupertypes(Tx)
                .Select(t => t.Label.Name)
                .ToHashSet();

            Assert.False(actuals.Except(superLabels).Any());
        }

        [Then(@"{}\\( ?([a-zA-Z0-9-_]+) ?) get supertypes do not contain:")]
        public void ThingTypeGetSupertypesDoNotContain(
            string rootLabel, string typeLabel, DataTable superLabelsData)
        {
            var superLabels = ParseDataTableToTypeList<string>(superLabelsData);

            HashSet<string> actuals = GetThingType(rootLabel, typeLabel)
                .GetSupertypes(Tx)
                .Select(t => t.Label.Name)
                .ToHashSet();

            foreach (string superLabel in superLabels)
            {
                Assert.False(actuals.Contains(superLabel));
            }
        }

        [Then(@"{}\\( ?([a-zA-Z0-9-_]+) ?) get subtypes contain:")]
        public void ThingTypeGetSubtypesContain(string rootLabel, string typeLabel, DataTable subLabelsData)
        {
            var subLabels = ParseDataTableToTypeList<string>(subLabelsData);

            HashSet<string> actuals = GetThingType(rootLabel, typeLabel)
                .GetSubtypes(Tx)
                .Select(t => t.Label.Name)
                .ToHashSet();

            Assert.False(actuals.Except(superLabels).Any());
        }

        [Then(@"{}\\( ?([a-zA-Z0-9-_]+) ?) get subtypes do not contain:")]
        public void ThingTypeGetSubtypesDoNotContain(
            string rootLabel, string typeLabel, DataTable subLabelsData)
        {
            var subLabels = ParseDataTableToTypeList<string>(subLabelsData);

            HashSet<string> actuals = GetThingType(rootLabel, typeLabel)
                .GetSubtypes(Tx)
                .Select(t => t.Label.Name)
                .ToHashSet();

            foreach (string subLabel in subLabels)
            {
                Assert.False(actuals.Contains(subLabel));
            }
        }

        [When(@"{}\\( ?([a-zA-Z0-9-_]+) ?) set owns attribute type: ([a-zA-Z0-9-_]+), with annotations: (\s*([\w\-_]+,\s*)*[\w\-_]*\s*)")]
        public void ThingTypeSetOwnsAttributeTypeWithAnnotations(
            string rootLabel, string typeLabel, string attTypeLabel, string annotationsData)
        {
            List<Annotation> annotations = GetAnnotations(Utils.ParseEnumerationToList(annotationsData));
        
            AttributeType attributeType = Tx.Concepts.GetAttributeType(attTypeLabel).Resolve();
            GetThingType(rootLabel, typeLabel).SetOwns(Tx, attributeType, new []{annotations}.Resolve();
        }

        [When(@"{}\\( ?([a-zA-Z0-9-_]+) ?) set owns attribute type: ([a-zA-Z0-9-_]+) as ([a-zA-Z0-9-_]+), with annotations: (\s*([\w\-_]+,\s*)*[\w\-_]*\s*)")]
        public void ThingTypeSetOwnsAttributeTypeAsTypeWithAnnotations(
            string rootLabel,
            string typeLabel,
            string attTypeLabel,
            string overriddenLabel,
            string annotationsData)
        {
            List<Annotation> annotations = GetAnnotations(Utils.ParseEnumerationToList(annotationsData));

            AttributeType attributeType = Tx.Concepts.GetAttributeType(attTypeLabel).Resolve();
            AttributeType overriddenType = Tx.Concepts.GetAttributeType(overriddenLabel).Resolve();

            GetThingType(rootLabel, typeLabel)
                .SetOwns(Tx, attributeType, overriddenType, new []{annotations}.Resolve();
        }

        [Then(@"{}\\( ?([a-zA-Z0-9-_]+) ?) set owns attribute type: ([a-zA-Z0-9-_]+), with annotations: (\s*([\w\-_]+,\s*)*[\w\-_]*\s*); throws exception")]
        public void ThingTypeSetOwnsAttributeTypeWithAnnotationsThrowsException(
            string rootLabel, string typeLabel, string attributeLabel, string annotationsData)
        {
            List<Annotation> annotations = GetAnnotations(Utils.ParseEnumerationToList(annotationsData));

            Assert.Throws<TypeDBDriverException>(() => ThingTypeSetOwnsAttributeTypeWithAnnotations(
                rootLabel, typeLabel, attributeLabel, annotations));
        }

        [Then(@"{}\\( ?([a-zA-Z0-9-_]+) ?) set owns attribute type: ([a-zA-Z0-9-_]+) as ([a-zA-Z0-9-_]+), with annotations: (\s*([\w\-_]+,\s*)*[\w\-_]*\s*); throws exception")]
        public void ThingTypeSetOwnsAttributeTypeAsTypeWithAnnotationsThrowsException(
            string rootLabel,
            string typeLabel,
            string attributeLabel,
            string overriddenLabel,
            string annotationsData)
        {
            List<Annotation> annotations = GetAnnotations(Utils.ParseEnumerationToList(annotationsData));

            Assert.Throws<TypeDBDriverException>(() => ThingTypeSetOwnsAttributeTypeAsTypeWithAnnotations(
                rootLabel, typeLabel, attributeLabel, overriddenLabel, annotations));
        }

        [Then(@"{}\\( ?([a-zA-Z0-9-_]+) ?) get owns attribute types, with annotations: (\s*([\w\-_]+,\s*)*[\w\-_]*\s*); contain:")]
        public void ThingTypeGetOwnsAttributeTypesWithAnnotationsContain(
            string rootLabel, string typeLabel, string annotationsData, DataTable attributeLabelsData)
        {
            List<Annotation> annotations = GetAnnotations(Utils.ParseEnumerationToList(annotationsData));

            var attributeLabels = ParseDataTableToTypeList<string>(attributeLabelsData);

            HashSet<string> actuals = GetThingType(rootLabel, typeLabel)
                .GetOwns(Tx, new []{annotations}
                .Select(t => t.Label.Name)
                .ToHashSet();

            Assert.False(actuals.Except(attributeLabels).Any());
        }

        [Then(@"{}\\( ?([a-zA-Z0-9-_]+) ?) get owns attribute types, with annotations: (\s*([\w\-_]+,\s*)*[\w\-_]*\s*); do not contain:")]
        public void ThingTypeGetOwnsAttributeTypesWithAnnotationsDoNotContain(
            string rootLabel, string typeLabel, string annotationsData, DataTable attributeLabelsData)
        {
            List<Annotation> annotations = GetAnnotations(Utils.ParseEnumerationToList(annotationsData));

            var attributeLabels = ParseDataTableToTypeList<string>(attributeLabelsData);

            HashSet<string> actuals = GetThingType(rootLabel, typeLabel)
                .GetOwns(Tx, new []{annotations}
                .Select(t => t.Label.Name)
                .ToHashSet();

            foreach (string attributeLabel in attributeLabels)
            {
                Assert.False(actuals.Contains(attributeLabel));
            }
        }

        [Then(@"{}\\( ?([a-zA-Z0-9-_]+) ?) get owns explicit attribute types, with annotations: (\s*([\w\-_]+,\s*)*[\w\-_]*\s*); contain:")]
        public void ThingTypeGetOwnsExplicitAttributeTypesWithAnnotationsContain(
            string rootLabel, string typeLabel, string annotationsData, DataTable attributeLabelsData)
        {
            List<Annotation> annotations = GetAnnotations(Utils.ParseEnumerationToList(annotationsData));

            var attributeLabels = ParseDataTableToTypeList<string>(attributeLabelsData);

            HashSet<string> actuals = GetThingType(rootLabel, typeLabel)
                .GetOwns(Tx, new []{annotations}, EXPLICIT)
                .Select(t => t.Label.Name)
                .ToHashSet();

            Assert.False(actuals.Except(attributeLabels).Any());
        }

        [Then(@"{}\\( ?([a-zA-Z0-9-_]+) ?) get owns explicit attribute types, with annotations: (\s*([\w\-_]+,\s*)*[\w\-_]*\s*); do not contain:")]
        public void ThingTypeGetOwnsExplicitAttributeTypesWithAnnotationsDoNotContain(
            string rootLabel, string typeLabel, string annotationsData, DataTable attributeLabelsData)
        {
            List<Annotation> annotations = GetAnnotations(Utils.ParseEnumerationToList(annotationsData));

            var attributeLabels = ParseDataTableToTypeList<string>(attributeLabelsData);

            HashSet<string> actuals = GetThingType(rootLabel, typeLabel)
                .GetOwns(Tx, new []{annotations}, EXPLICIT)
                .Select(t => t.Label.Name)
                .ToHashSet();

            foreach (string attributeLabel in attributeLabels)
            {
                Assert.False(actuals.Contains(attributeLabel));
            }
        }

        [When(@"{}\\( ?([a-zA-Z0-9-_]+) ?) set owns attribute type: ([a-zA-Z0-9-_]+)")]
        public void ThingTypeSetOwnsAttributeType(
            string rootLabel, string typeLabel, string attributeLabel)
        {
            AttributeType attributeType = Tx.Concepts.GetAttributeType(attributeLabel).Resolve();
            GetThingType(rootLabel, typeLabel).SetOwns(Tx, attributeType).Resolve();
        }

        [Then(@"{}\\( ?([a-zA-Z0-9-_]+) ?) set owns attribute type: ([a-zA-Z0-9-_]+); throws exception")]
        public void ThingTypeSetOwnsAttributeTypeThrowsException(
            string rootLabel, string typeLabel, string attributeLabel)
        {
            Assert.Throws<TypeDBDriverException>(() => ThingTypeSetOwnsAttributeType(rootLabel, typeLabel, attributeLabel));
        }

        [When(@"{}\\( ?([a-zA-Z0-9-_]+) ?) set owns attribute type: ([a-zA-Z0-9-_]+) as ([a-zA-Z0-9-_]+)")]
        public void ThingTypeSetOwnsAttributeTypeAs(
            string rootLabel, string typeLabel, string attributeLabel, string overriddenLabel)
        {
            AttributeType attributeType = Tx.Concepts.GetAttributeType(attributeLabel).Resolve();
            AttributeType overriddenType = Tx.Concepts.GetAttributeType(overriddenLabel).Resolve();
            GetThingType(rootLabel, typeLabel).SetOwns(Tx, attributeType, overriddenType).Resolve();
        }

        [Then(@"{}\\( ?([a-zA-Z0-9-_]+) ?) set owns attribute type: ([a-zA-Z0-9-_]+) as ([a-zA-Z0-9-_]+); throws exception")]
        public void ThingTypeSetowns_attributeAsThrowsException(
            string rootLabel, string typeLabel, string attributeLabel, string overriddenLabel)
        {
            Assert.Throws<TypeDBDriverException>(() =>
                ThingTypeSetOwnsAttributeTypeAs(rootLabel, typeLabel, attributeLabel, overriddenLabel));
        }

        [When(@"{}\\( ?([a-zA-Z0-9-_]+) ?) unset owns attribute type: ([a-zA-Z0-9-_]+)")]
        public void ThingTypeUnsetOwnsAttributeType(
            string rootLabel, string typeLabel, string attributeLabel)
        {
            AttributeType attributeType = Tx.Concepts.GetAttributeType(attributeLabel).Resolve();
            GetThingType(rootLabel, typeLabel).unsetOwns(Tx, attributeType).Resolve();
        }

        [When(@"{}\\( ?([a-zA-Z0-9-_]+) ?) unset owns attribute type: ([a-zA-Z0-9-_]+); throws exception")]
        public void ThingTypeUnsetOwnsAttributeTypeThrowsException(
            string rootLabel, string typeLabel, string attributeLabel)
        {
            Assert.Throws<TypeDBDriverException>(() => ThingTypeUnsetOwnsAttributeType(rootLabel, typeLabel, attributeLabel));
        }

        [Then(@"{}\\( ?([a-zA-Z0-9-_]+) ?) get owns overridden attribute\\( ?([a-zA-Z0-9-_]+) ?) is null: {bool}")]
        public void ThingTypeGetOwnsOverriddenAttributeIsNull(
            string rootLabel, string typeLabel, string attributeLabel, bool isNull)
        {
            AttributeType attributeType = Tx.Concepts.GetAttributeType(attributeLabel).Resolve();

            var ownsOverridden = GetThingType(rootLabel, typeLabel)
                .GetOwnsOverridden(Tx, attributeType).Resolve();

            Assert.Equals(isNull, isNull(ownsOverridden));
        }

        [Then(@"{}\\( ?([a-zA-Z0-9-_]+) ?) get owns overridden attribute\\( ?([a-zA-Z0-9-_]+) ?) get label: ([a-zA-Z0-9-_]+)")]
        public void ThingTypeGetOwnsOverriddenAttributeGetLabel(
            string rootLabel, string typeLabel, string attributeLabel, string getLabel)
        {
            AttributeType attributeType = Tx.Concepts.GetAttributeType(attributeLabel).Resolve();

            var ownsOverridden = GetThingType(rootLabel, typeLabel)
                .GetOwnsOverridden(Tx, attributeType).Resolve();

            Assert.Equals(getLabel, ownsOverridden.Label.Name);
        }

        [Then(@"{}\\( ?([a-zA-Z0-9-_]+) ?) get owns attribute types contain:")]
        public void ThingTypeGetOwnsAttributeTypesContain(
            string rootLabel, string typeLabel, DataTable attributeLabelsData)
        {
            var attributeLabels = ParseDataTableToTypeList<string>(attributeLabelsData);

            HashSet<string> actuals = GetThingType(rootLabel, typeLabel)
                .GetOwns(Tx)
                .Select(at => at.Label.Name)
                .ToHashSet();

            Assert.False(actuals.Except(attributeLabels).Any());
        }

        [Then(@"{}\\( ?([a-zA-Z0-9-_]+) ?) get owns attribute types do not contain:")]
        public void ThingTypeGetOwnsAttributeTypesDoNotContain(
            string rootLabel, string typeLabel, DataTable attributeLabelsData)
        {
            var attributeLabels = ParseDataTableToTypeList<string>(attributeLabelsData);

            HashSet<string> actuals = GetThingType(rootLabel, typeLabel)
                .GetOwns(Tx)
                .Select(at => at.Label.Name)
                .ToHashSet();

            foreach (string attributeLabel in attributeLabels)
            {
                Assert.False(actuals.Contains(attributeLabel));
            }
        }

        [Then(@"{}\\( ?([a-zA-Z0-9-_]+) ?) get owns explicit attribute types contain:")]
        public void ThingTypeGetOwnsExplicitAttributeTypesContain(
            string rootLabel, string typeLabel, DataTable attributeLabelsData)
        {
            var attributeLabels = ParseDataTableToTypeList<string>(attributeLabelsData);

            HashSet<string> actuals = GetThingType(rootLabel, typeLabel)
                .GetOwns(Tx, EXPLICIT)
                .Select(at => at.Label.Name)
                .ToHashSet();

            Assert.False(actuals.Except(attributeLabels).Any());
        }

        [Then(@"{}\\( ?([a-zA-Z0-9-_]+) ?) get owns explicit attribute types do not contain:")]
        public void ThingTypeGetOwnsExplicitAttributeTypesDoNotContain(
            string rootLabel, string typeLabel, DataTable attributeLabelsData)
        {
            var attributeLabels = ParseDataTableToTypeList<string>(attributeLabelsData);

            HashSet<string> actuals = GetThingType(rootLabel, typeLabel)
                .GetOwns(Tx, EXPLICIT)
                .Select(at => at.Label.Name)
                .ToHashSet();

            foreach (string attributeLabel in attributeLabels)
            {
                Assert.False(actuals.Contains(attributeLabel));
            }
        }

        [When(@"{}\\( ?([a-zA-Z0-9-_]+) ?) set plays role: ([a-zA-Z0-9-_]+:[a-zA-Z0-9-_]+)")]
        public void ThingTypeSetPlaysRole(string rootLabel, string typeLabel, Label roleLabel)
        {
            RoleType roleType = Tx.Concepts
                .GetRelationType(roleLabel.Scope.Get()).Resolve()
                .GetRelates(Tx, roleLabel.Name).Resolve();

            GetThingType(rootLabel, typeLabel).SetPlays(Tx, roleType).Resolve();
        }

        [When(@"{}\\( ?([a-zA-Z0-9-_]+) ?) set plays role: ([a-zA-Z0-9-_]+:[a-zA-Z0-9-_]+); throws exception")]
        public void ThingTypeSetPlaysRoleThrowsException(string rootLabel, string typeLabel, Label roleLabel)
        {
            Assert.Throws<TypeDBDriverException>(() => ThingTypeSetPlaysRole(rootLabel, typeLabel, roleLabel));
        }

        [When(@"{}\\( ?([a-zA-Z0-9-_]+) ?) set plays role: ([a-zA-Z0-9-_]+:[a-zA-Z0-9-_]+) as ([a-zA-Z0-9-_]+:[a-zA-Z0-9-_]+)")]
        public void ThingTypeSetPlaysRoleAs(
            string rootLabel, string typeLabel, Label roleLabel, Label overriddenLabel)
        {
            RoleType roleType = Tx.Concepts
                .GetRelationType(roleLabel.Scope.Get()).Resolve()
                .GetRelates(Tx, roleLabel.Name).Resolve();

            RoleType overriddenType = Tx.Concepts
                .GetRelationType(overriddenLabel.Scope.Get()).Resolve()
                .GetRelates(Tx, overriddenLabel.Name).Resolve();

            GetThingType(rootLabel, typeLabel)
                .SetPlays(Tx, roleType, overriddenType).Resolve();
        }

        [When(@"{}\\( ?([a-zA-Z0-9-_]+) ?) set plays role: ([a-zA-Z0-9-_]+:[a-zA-Z0-9-_]+) as ([a-zA-Z0-9-_]+:[a-zA-Z0-9-_]+); throws exception")]
        public void ThingTypeSetPlaysRoleAsThrowsException(
            string rootLabel, string typeLabel, Label roleLabel, Label overriddenLabel)
        {
            Assert.Throws<TypeDBDriverException>(() => ThingTypeSetPlaysRoleAs(rootLabel, typeLabel, roleLabel, overriddenLabel));
        }

        [When(@"{}\\( ?([a-zA-Z0-9-_]+) ?) unset plays role: ([a-zA-Z0-9-_]+:[a-zA-Z0-9-_]+)")]
        public void ThingTypeUnsetPlaysRole(string rootLabel, string typeLabel, Label roleLabel)
        {
            RoleType roleType = Tx.Concepts
                .GetRelationType(roleLabel.Scope.Get()).Resolve()
                .GetRelates(Tx, roleLabel.Name).Resolve();

            GetThingType(rootLabel, typeLabel).unsetPlays(Tx, roleType).Resolve();
        }

        [When(@"{}\\( ?([a-zA-Z0-9-_]+) ?) unset plays role: ([a-zA-Z0-9-_]+:[a-zA-Z0-9-_]+); throws exception")]
        public void ThingTypeUnsetPlaysRoleThrowsException(
            string rootLabel, string typeLabel, Label roleLabel)
        {
            Assert.Throws<TypeDBDriverException>(() => ThingTypeUnsetPlaysRole(rootLabel, typeLabel, roleLabel));
        }

        [Then(@"{}\\( ?([a-zA-Z0-9-_]+) ?) get playing roles contain:")]
        public void ThingTypeGetPlayingRolesContain(
            string rootLabel, string typeLabel, DataTable roleLabelsData)
        {
            List<Label> roleLabels = ParseDataTableToTypeList<Label>(roleLabelsData);

            HashSet<Label> actuals = GetThingType(rootLabel, typeLabel)
                .GetPlays(Tx)
                .Select(obj => obj.Label)
                .ToHashSet();

            Assert.False(actuals.Except(roleLabels).Any());
        }

        [Then(@"{}\\( ?([a-zA-Z0-9-_]+) ?) get playing roles do not contain:")]
        public void ThingTypeGetPlayingRolesDoNotContain(
            string rootLabel, string typeLabel, DataTable roleLabelsData)
        {
            List<Label> roleLabels = ParseDataTableToTypeList<Label>(roleLabelsData);

            HashSet<Label> actuals = GetThingType(rootLabel, typeLabel)
                .GetPlays(Tx)
                .Select(obj => obj.Label)
                .ToHashSet();

            foreach (Label roleLabel in roleLabels)
            {
                Assert.False(actuals.Contains(roleLabel));
            }
        }

        [Then(@"{}\\( ?([a-zA-Z0-9-_]+) ?) get playing roles explicit contain:")]
        public void ThingTypeGetPlayingRolesExplicitContain(
            string rootLabel, string typeLabel, DataTable roleLabelsData)
        {
            List<Label> roleLabels = ParseDataTableToTypeList<Label>(roleLabelsData);

            HashSet<Label> actuals = GetThingType(rootLabel, typeLabel)
                .GetPlays(Tx, EXPLICIT)
                .Select(obj => obj.Label)
                .ToHashSet();

            Assert.False(actuals.Except(roleLabels).Any());
        }

        [Then(@"{}\\( ?([a-zA-Z0-9-_]+) ?) get playing roles explicit do not contain:")]
        public void ThingTypeGetPlayingRolesExplicitDoNotContain(
            string rootLabel, string typeLabel, DataTable roleLabelsData)
        {
            List<Label> roleLabels = ParseDataTableToTypeList<Label>(roleLabelsData);

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
