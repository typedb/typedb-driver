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
        private static readonly string ILLEGAL_ARGUMENT = "Illegal argument";

        // TODO: Implement RootLabel
        public static ThingType GetThingType(RootLabel rootLabel, string typeLabel)
        {
            switch (rootLabel)  // TODO: Type?
            {
                case ENTITY:
                    return SingleTransaction.Concepts.GetEntityType(typeLabel).Resolve();

                case ATTRIBUTE:
                    return SingleTransaction.Concepts.GetAttributeType(typeLabel).Resolve();

                case RELATION:
                    return SingleTransaction.Concepts.GetRelationType(typeLabel).Resolve();

                default:
                    throw new BehaviourTestException(ILLEGAL_ARGUMENT);
            }
        }

        [When(@"put {} type: {}")]
        public void PutThingType(RootLabel rootLabel, string typeLabel) 
        { // TODO: Parse into rootLabel, typeLabel (string could be different!)
            switch (rootLabel) 
            {
                case ENTITY:
                    SingleTransaction.Concepts.PutEntityType(typeLabel).Resolve();
                    break;

                case RELATION:
                    SingleTransaction.Concepts.PutRelationType(typeLabel).Resolve();
                    break;

                default:
                    throw new IllegalArgumentException(ILLEGAL_ARGUMENT);
            }
        }

        [When(@"delete {root_label} type: {type_label}")]
        public void DeleteThingType(RootLabel rootLabel, string typeLabel)
        {
            GetThingType(rootLabel, typeLabel).delete(SingleTransaction).Resolve();
        }

        [Then(@"delete {root_label} type: {type_label}; throws exception")]
        public void DeleteThingTypeThrowsException(RootLabel rootLabel, string typeLabel)
        {
            Assert.Throws<TypeDBDriverException>(() => DeleteThingType(rootLabel, typeLabel));
        }

        [Then(@"{root_label}\\( ?{type_label} ?) is null: {bool}")]
        public void ThingTypeIsNull(RootLabel rootLabel, string typeLabel, bool isNull)
        {
            Assert.Equals(isNull, isNull(GetThingType(rootLabel, typeLabel)));
        }

        [When(@"{root_label}\\( ?{type_label} ?) set label: {type_label}")]
        public void ThingTypeSetLabel(RootLabel rootLabel, string typeLabel, string newLabel)
        {
            GetThingType(rootLabel, typeLabel).SetLabel(SingleTransaction, newLabel).Resolve();
        }

        [Then(@"{root_label}\\( ?{type_label} ?) get label: {type_label}")]
        public void ThingTypeGetLabel(RootLabel rootLabel, string typeLabel, string getLabel)
        {
            Assert.Equals(getLabel, GetThingType(rootLabel, typeLabel).Label.Name);
        }

        [When(@"{root_label}\\( ?{type_label} ?) set abstract: {bool}")]
        public void ThingTypeSetAbstract(RootLabel rootLabel, string typeLabel, bool isAbstract)
        {
            ThingType thingType = GetThingType(rootLabel, typeLabel);

            if (isAbstract)
            {
                thingType.SetAbstract(SingleTransaction).Resolve();
            }
            else
            {
                thingType.unsetAbstract(SingleTransaction).Resolve();
            }
        }

        [When(@"{root_label}\\( ?{type_label} ?) set abstract: {bool}; throws exception")]
        public void ThingTypeSetAbstractThrowsException(
            RootLabel rootLabel, string typeLabel, bool isAbstract)
        {
            Assert.Throws<TypeDBDriverException>(() => ThingTypeSetAbstract(rootLabel, typeLabel, isAbstract));
        }

        [Then(@"{root_label}\\( ?{type_label} ?) is abstract: {bool}")]
        public void ThingTypeIsAbstract(RootLabel rootLabel, string typeLabel, bool isAbstract)
        {
            Assert.Equals(isAbstract, GetThingType(rootLabel, typeLabel).isAbstract());
        }

        [When(@"{root_label}\\( ?{type_label} ?) set supertype: {type_label}")]
        public void ThingTypeSetSupertype(RootLabel rootLabel, string typeLabel, string superLabel)
        {
            switch (rootLabel)
            {
                case ENTITY:
                    EntityType entitySuperType = SingleTransaction.Concepts.GetEntityType(superLabel).Resolve();
                    SingleTransaction
                        .Concepts
                        .GetEntityType(typeLabel).Resolve()
                        .SetSupertype(SingleTransaction, entitySuperType).Resolve();
                    break;

                case ATTRIBUTE:
                    AttributeType attributeSuperType = SingleTransaction.Concepts.GetAttributeType(superLabel).Resolve();
                    SingleTransaction
                        .Concepts
                        .GetAttributeType(typeLabel).Resolve()
                        .SetSupertype(SingleTransaction, attributeSuperType).Resolve();
                    break;

                case RELATION:
                    RelationType relationSuperType = SingleTransaction.Concepts.GetRelationType(superLabel).Resolve();
                    SingleTransaction
                        .Concepts
                        .GetRelationType(typeLabel).Resolve()
                        .SetSupertype(SingleTransaction, relationSuperType).Resolve();
                    break;

                case THING:
                    throw new IllegalArgumentException(ILLEGAL_ARGUMENT);
            }
        }

        [Then(@"{root_label}\\( ?{type_label} ?) set supertype: {type_label}; throws exception")]
        public void ThingTypeSetSupertypeThrowsException(
            RootLabel rootLabel, string typeLabel, string superLabel)
        {
            Assert.Throws<TypeDBDriverException>(() => ThingTypeSetSupertype(rootLabel, typeLabel, superLabel));
        }

        [Then(@"{root_label}\\( ?{type_label} ?) get supertype: {type_label}")]
        public void ThingTypeGetSupertype(RootLabel rootLabel, string typeLabel, string superLabel)
        {
            ThingType supertype = GetThingType(rootLabel, superLabel);
            Assert.Equals(
                supertype,
                GetThingType(rootLabel, typeLabel).GetSupertype(SingleTransaction).Resolve());
        }

        [Then(@"{root_label}\\( ?{type_label} ?) get supertypes contain:")]
        public void ThingTypeGetSupertypesContain(
            RootLabel rootLabel, string typeLabel, List<string> superLabels)
        {
            ThingType thing_type = GetThingType(rootLabel, typeLabel);
            Set<string> actuals = thing_type
                .GetSupertypes(SingleTransaction)
                .map(t => t.Label.Name)
                .collect(toSet());

            Assert.True(actuals.containsAll(superLabels));
        }

        [Then(@"{root_label}\\( ?{type_label} ?) get supertypes do not contain:")]
        public void ThingTypeGetSupertypesDoNotContain(
            RootLabel rootLabel, string typeLabel, List<string> superLabels)
        {
            Set<string> actuals = GetThingType(rootLabel, typeLabel)
                .GetSupertypes(SingleTransaction)
                .map(t => t.Label.Name)
                .collect(toSet());

            for (string superLabel : superLabels)
            {
                Assert.False(actuals.contains(superLabel));
            }
        }

        [Then(@"{root_label}\\( ?{type_label} ?) get subtypes contain:")]
        public void ThingTypeGetSubtypesContain(RootLabel rootLabel, string typeLabel, List<string> subLabels)
        {
            Set<string> actuals = GetThingType(rootLabel, typeLabel)
                .GetSubtypes(SingleTransaction)
                .map(t => t.Label.Name)
                .collect(toSet());

            Assert.True(actuals.containsAll(subLabels));
        }

        [Then(@"{root_label}\\( ?{type_label} ?) get subtypes do not contain:")]
        public void ThingTypeGetSubtypesDoNotContain(
            RootLabel rootLabel, string typeLabel, List<string> subLabels)
        {
            Set<string> actuals = GetThingType(rootLabel, typeLabel)
                .GetSubtypes(SingleTransaction)
                .map(t => t.Label.Name)
                .collect(toSet());

            for (string subLabel : subLabels)
            {
                Assert.False(actuals.contains(subLabel));
            }
        }

        [When(@"{root_label}\\( ?{type_label} ?) set owns attribute type: {type_label}, with annotations: {annotations}")]
        public void ThingTypeSetOwnsAttributeTypeWithAnnotations(
            RootLabel rootLabel, string typeLabel, string attTypeLabel, List<Annotation> annotations)
        {
            AttributeType attributeType = SingleTransaction.Concepts.GetAttributeType(attTypeLabel).Resolve();
            GetThingType(rootLabel, typeLabel).SetOwns(SingleTransaction, attributeType, set(annotations)).Resolve();
        }

        [When(@"{root_label}\\( ?{type_label} ?) set owns attribute type: {type_label} as {type_label}, with annotations: {annotations}")]
        public void ThingTypeSetOwnsAttributeTypeAsTypeWithAnnotations(
            RootLabel rootLabel,
            string typeLabel,
            string attTypeLabel,
            string overriddenLabel,
            List<Annotation> annotations)
        {
            AttributeType attributeType = SingleTransaction.Concepts.GetAttributeType(attTypeLabel).Resolve();
            AttributeType overriddenType = SingleTransaction.Concepts.GetAttributeType(overriddenLabel).Resolve();

            GetThingType(rootLabel, typeLabel)
                .SetOwns(SingleTransaction, attributeType, overriddenType, set(annotations)).Resolve();
        }

        [Then(@"{root_label}\\( ?{type_label} ?) set owns attribute type: {type_label}, with annotations: {annotations}; throws exception")]
        public void ThingTypeSetOwnsAttributeTypeWithAnnotationsThrowsException(
            RootLabel rootLabel, string typeLabel, string attributeLabel, List<Annotation> annotations)
        {
            Assert.Throws<TypeDBDriverException>(() => ThingTypeSetOwnsAttributeTypeWithAnnotations(
                rootLabel, typeLabel, attributeLabel, annotations));
        }

        [Then(@"{root_label}\\( ?{type_label} ?) set owns attribute type: {type_label} as {type_label}, with annotations: {annotations}; throws exception")]
        public void ThingTypeSetOwnsAttributeTypeAsTypeWithAnnotationsThrowsException(
            RootLabel rootLabel,
            string typeLabel,
            string attributeLabel,
            string overriddenLabel,
            List<Annotation> annotations)
        {
            Assert.Throws<TypeDBDriverException>(() => ThingTypeSetOwnsAttributeTypeAsTypeWithAnnotations(
                rootLabel, typeLabel, attributeLabel, overriddenLabel, annotations));
        }

        [Then(@"{root_label}\\( ?{type_label} ?) get owns attribute types, with annotations: {annotations}; contain:")]
        public void ThingTypeGetOwnsAttributeTypesWithAnnotationsContain(
            RootLabel rootLabel, string typeLabel, List<Annotation> annotations, List<string> attributeLabels)
        {
            Set<string> actuals = GetThingType(rootLabel, typeLabel)
                .GetOwns(SingleTransaction, set(annotations))
                .map(t => t.Label.Name)
                .collect(toSet());

            Assert.True(actuals.containsAll(attributeLabels));
        }

        [Then(@"{root_label}\\( ?{type_label} ?) get owns attribute types, with annotations: {annotations}; do not contain:")]
        public void ThingTypeGetOwnsAttributeTypesWithAnnotationsDoNotContain(
            RootLabel rootLabel, string typeLabel, List<Annotation> annotations, List<string> attributeLabels)
        {
            Set<string> actuals = GetThingType(rootLabel, typeLabel)
                .GetOwns(SingleTransaction, set(annotations))
                .map(t => t.Label.Name)
                .collect(toSet());

            for (string attributeLabel : attributeLabels)
            {
                Assert.False(actuals.contains(attributeLabel));
            }
        }

        [Then(@"{root_label}\\( ?{type_label} ?) get owns explicit attribute types, with annotations: {annotations}; contain:")]
        public void ThingTypeGetOwnsExplicitAttributeTypesWithAnnotationsContain(
            RootLabel rootLabel, string typeLabel, List<Annotation> annotations, List<string> attributeLabels)
        {
            Set<string> actuals = GetThingType(rootLabel, typeLabel)
                .GetOwns(SingleTransaction, set(annotations), EXPLICIT)
                .map(t => t.Label.Name)
                .collect(toSet());

            Assert.True(actuals.containsAll(attributeLabels));
        }

        [Then(@"{root_label}\\( ?{type_label} ?) get owns explicit attribute types, with annotations: {annotations}; do not contain:")]
        public void ThingTypeGetOwnsExplicitAttributeTypesWithAnnotationsDoNotContain(
            RootLabel rootLabel, string typeLabel, List<Annotation> annotations, List<string> attributeLabels)
        {
            Set<string> actuals = GetThingType(rootLabel, typeLabel)
                .GetOwns(SingleTransaction, set(annotations), EXPLICIT)
                .map(t => t.Label.Name)
                .collect(toSet());

            for (string attributeLabel : attributeLabels)
            {
                Assert.False(actuals.contains(attributeLabel));
            }
        }

        [When(@"{root_label}\\( ?{type_label} ?) set owns attribute type: {type_label}")]
        public void ThingTypeSetOwnsAttributeType(
            RootLabel rootLabel, string typeLabel, string attributeLabel)
        {
            AttributeType attributeType = SingleTransaction.Concepts.GetAttributeType(attributeLabel).Resolve();
            GetThingType(rootLabel, typeLabel).SetOwns(SingleTransaction, attributeType).Resolve();
        }

        [Then(@"{root_label}\\( ?{type_label} ?) set owns attribute type: {type_label}; throws exception")]
        public void ThingTypeSetOwnsAttributeTypeThrowsException(
            RootLabel rootLabel, string typeLabel, string attributeLabel)
        {
            Assert.Throws<TypeDBDriverException>(() => ThingTypeSetOwnsAttributeType(rootLabel, typeLabel, attributeLabel));
        }

        [When(@"{root_label}\\( ?{type_label} ?) set owns attribute type: {type_label} as {type_label}")]
        public void ThingTypeSetOwnsAttributeTypeAs(
            RootLabel rootLabel, string typeLabel, string attributeLabel, string overriddenLabel)
        {
            AttributeType attributeType = SingleTransaction.Concepts.GetAttributeType(attributeLabel).Resolve();
            AttributeType overriddenType = SingleTransaction.Concepts.GetAttributeType(overriddenLabel).Resolve();
            GetThingType(rootLabel, typeLabel).SetOwns(SingleTransaction, attributeType, overriddenType).Resolve();
        }

        [Then(@"{root_label}\\( ?{type_label} ?) set owns attribute type: {type_label} as {type_label}; throws exception")]
        public void ThingTypeSetowns_attributeAsThrowsException(
            RootLabel rootLabel, string typeLabel, string attributeLabel, string overriddenLabel)
        {
            Assert.Throws<TypeDBDriverException>(() =>
                ThingTypeSetOwnsAttributeTypeAs(rootLabel, typeLabel, attributeLabel, overriddenLabel));
        }

        [When(@"{root_label}\\( ?{type_label} ?) unset owns attribute type: {type_label}")]
        public void ThingTypeUnsetOwnsAttributeType(
            RootLabel rootLabel, string typeLabel, string attributeLabel)
        {
            AttributeType attributeType = SingleTransaction.Concepts.GetAttributeType(attributeLabel).Resolve();
            GetThingType(rootLabel, typeLabel).unsetOwns(SingleTransaction, attributeType).Resolve();
        }

        [When(@"{root_label}\\( ?{type_label} ?) unset owns attribute type: {type_label}; throws exception")]
        public void ThingTypeUnsetOwnsAttributeTypeThrowsException(
            RootLabel rootLabel, string typeLabel, string attributeLabel)
        {
            Assert.Throws<TypeDBDriverException>(() => ThingTypeUnsetOwnsAttributeType(rootLabel, typeLabel, attributeLabel));
        }

        [Then(@"{root_label}\\( ?{type_label} ?) get owns overridden attribute\\( ?{type_label} ?) is null: {bool}")]
        public void ThingTypeGetOwnsOverriddenAttributeIsNull(
            RootLabel rootLabel, string typeLabel, string attributeLabel, bool isNull)
        {
            AttributeType attributeType = SingleTransaction.Concepts.GetAttributeType(attributeLabel).Resolve();

            var ownsOverridden = GetThingType(rootLabel, typeLabel)
                .GetOwnsOverridden(SingleTransaction, attributeType).Resolve();

            Assert.Equals(isNull, isNull(ownsOverridden));
        }

        [Then(@"{root_label}\\( ?{type_label} ?) get owns overridden attribute\\( ?{type_label} ?) get label: {type_label}")]
        public void ThingTypeGetOwnsOverriddenAttributeGetLabel(
            RootLabel rootLabel, string typeLabel, string attributeLabel, string getLabel)
        {
            AttributeType attributeType = SingleTransaction.Concepts.GetAttributeType(attributeLabel).Resolve();

            var ownsOverridden = GetThingType(rootLabel, typeLabel)
                .GetOwnsOverridden(SingleTransaction, attributeType).Resolve();

            Assert.Equals(getLabel, ownsOverridden.Label.Name);
        }

        [Then(@"{root_label}\\( ?{type_label} ?) get owns attribute types contain:")]
        public void ThingTypeGetOwnsAttributeTypesContain(
            RootLabel rootLabel, string typeLabel, List<string> attributeLabels)
        {
            Set<string> actuals = GetThingType(rootLabel, typeLabel)
                .GetOwns(SingleTransaction)
                .map(at => at.Label.Name)
                .collect(toSet());

            Assert.True(actuals.containsAll(attributeLabels));
        }

        [Then(@"{root_label}\\( ?{type_label} ?) get owns attribute types do not contain:")]
        public void ThingTypeGetOwnsAttributeTypesDoNotContain(
            RootLabel rootLabel, string typeLabel, List<string> attributeLabels)
        {
            Set<string> actuals = GetThingType(rootLabel, typeLabel)
                .GetOwns(SingleTransaction)
                .map(at => at.Label.Name)
                .collect(toSet());

            for (string attributeLabel : attributeLabels)
            {
                Assert.False(actuals.contains(attributeLabel));
            }
        }

        [Then(@"{root_label}\\( ?{type_label} ?) get owns explicit attribute types contain:")]
        public void ThingTypeGetOwnsExplicitAttributeTypesContain(
            RootLabel rootLabel, string typeLabel, List<string> attributeLabels)
        {
            Set<string> actuals = GetThingType(rootLabel, typeLabel)
                .GetOwns(SingleTransaction, EXPLICIT)
                .map(at => at.Label.Name)
                .collect(toSet());

            Assert.True(actuals.containsAll(attributeLabels));
        }

        [Then(@"{root_label}\\( ?{type_label} ?) get owns explicit attribute types do not contain:")]
        public void ThingTypeGetOwnsExplicitAttributeTypesDoNotContain(
            RootLabel rootLabel, string typeLabel, List<string> attributeLabels)
        {
            Set<string> actuals = GetThingType(rootLabel, typeLabel)
                .GetOwns(SingleTransaction, EXPLICIT)
                .map(at => at.Label.Name)
                .collect(toSet());

            for (string attributeLabel : attributeLabels)
            {
                Assert.False(actuals.contains(attributeLabel));
            }
        }

        [When(@"{root_label}\\( ?{type_label} ?) set plays role: {scoped_label}")]
        public void ThingTypeSetPlaysRole(RootLabel rootLabel, string typeLabel, Label roleLabel)
        {
            RoleType roleType = SingleTransaction
                .Concepts
                .GetRelationType(roleLabel.scope().Get()).Resolve()
                .GetRelates(SingleTransaction, roleLabel.Name).Resolve();

            GetThingType(rootLabel, typeLabel).SetPlays(SingleTransaction, roleType).Resolve();
        }

        [When(@"{root_label}\\( ?{type_label} ?) set plays role: {scoped_label}; throws exception")]
        public void ThingTypeSetPlaysRoleThrowsException(RootLabel rootLabel, string typeLabel, Label roleLabel)
        {
            Assert.Throws<TypeDBDriverException>(() => ThingTypeSetPlaysRole(rootLabel, typeLabel, roleLabel));
        }

        [When(@"{root_label}\\( ?{type_label} ?) set plays role: {scoped_label} as {scoped_label}")]
        public void ThingTypeSetPlaysRoleAs(
            RootLabel rootLabel, string typeLabel, Label roleLabel, Label overriddenLabel)
        {
            RoleType roleType = SingleTransaction
                .Concepts
                .GetRelationType(roleLabel.scope().Get()).Resolve()
                .GetRelates(SingleTransaction, roleLabel.Name).Resolve();

            RoleType overriddenType = SingleTransaction
                .Concepts
                .GetRelationType(overriddenLabel.scope().Get()).Resolve()
                .GetRelates(SingleTransaction, overriddenLabel.Name).Resolve();

            GetThingType(rootLabel, typeLabel)
                .SetPlays(SingleTransaction, roleType, overriddenType).Resolve();
        }

        [When(@"{root_label}\\( ?{type_label} ?) set plays role: {scoped_label} as {scoped_label}; throws exception")]
        public void ThingTypeSetPlaysRoleAsThrowsException(
            RootLabel rootLabel, string typeLabel, Label roleLabel, Label overriddenLabel)
        {
            Assert.Throws<TypeDBDriverException>(() => ThingTypeSetPlaysRoleAs(rootLabel, typeLabel, roleLabel, overriddenLabel));
        }

        [When(@"{root_label}\\( ?{type_label} ?) unset plays role: {scoped_label}")]
        public void ThingTypeUnsetPlaysRole(RootLabel rootLabel, string typeLabel, Label roleLabel)
        {
            RoleType roleType = SingleTransaction
                .Concepts
                .GetRelationType(roleLabel.scope().Get()).Resolve()
                .GetRelates(SingleTransaction, roleLabel.Name).Resolve();

            GetThingType(rootLabel, typeLabel).unsetPlays(SingleTransaction, roleType).Resolve();
        }

        [When(@"{root_label}\\( ?{type_label} ?) unset plays role: {scoped_label}; throws exception")]
        public void ThingTypeUnsetPlaysRoleThrowsException(
            RootLabel rootLabel, string typeLabel, Label roleLabel)
        {
            Assert.Throws<TypeDBDriverException>(() => ThingTypeUnsetPlaysRole(rootLabel, typeLabel, roleLabel));
        }

        [Then(@"{root_label}\\( ?{type_label} ?) get playing roles contain:")]
        public void ThingTypeGetPlayingRolesContain(
            RootLabel rootLabel, string typeLabel, List<Label> roleLabels)
        {
            Set<Label> actuals = GetThingType(rootLabel, typeLabel)
                .GetPlays(SingleTransaction)
                .map(Type::getLabel)
                .collect(toSet());

            Assert.True(actuals.containsAll(roleLabels));
        }

        [Then(@"{root_label}\\( ?{type_label} ?) get playing roles do not contain:")]
        public void ThingTypeGetPlayingRolesDoNotContain(
            RootLabel rootLabel, string typeLabel, List<Label> roleLabels)
        {
            Set<Label> actuals = GetThingType(rootLabel, typeLabel)
                .GetPlays(SingleTransaction)
                .map(Type::getLabel)
                .collect(toSet());

            for (Label roleLabel : roleLabels)
            {
                Assert.False(actuals.contains(roleLabel));
            }
        }

        [Then(@"{root_label}\\( ?{type_label} ?) get playing roles explicit contain:")]
        public void ThingTypeGetPlayingRolesExplicitContain(
            RootLabel rootLabel, string typeLabel, List<Label> roleLabels)
        {
            Set<Label> actuals = GetThingType(rootLabel, typeLabel)
                .GetPlays(SingleTransaction, EXPLICIT)
                .map(Type::getLabel)
                .collect(toSet());

            Assert.True(actuals.containsAll(roleLabels));
        }

        [Then(@"{root_label}\\( ?{type_label} ?) get playing roles explicit do not contain:")]
        public void ThingTypeGetPlayingRolesExplicitDoNotContain(
            RootLabel rootLabel, string typeLabel, List<Label> roleLabels)
        {
            Set<Label> actuals = GetThingType(rootLabel, typeLabel)
                .GetPlays(SingleTransaction, EXPLICIT)
                .map(Type::getLabel)
                .collect(toSet());

            for (Label roleLabel : roleLabels)
            {
                Assert.False(actuals.contains(roleLabel));
            }
        }
    }
}
