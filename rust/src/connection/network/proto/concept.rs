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

use std::collections::HashMap;

use chrono::NaiveDateTime;
use itertools::Itertools;
use typedb_protocol::{
    concept,
    numeric::Value as NumericValue,
    r#type::{annotation, Annotation as AnnotationProto, Transitivity as TransitivityProto},
    thing, thing_type,
    value::Value as ValueProtoInner,
    Attribute as AttributeProto, AttributeType as AttributeTypeProto, Concept as ConceptProto,
    ConceptMap as ConceptMapProto, ConceptMapGroup as ConceptMapGroupProto, Entity as EntityProto,
    EntityType as EntityTypeProto, Explainable as ExplainableProto, Explainables as ExplainablesProto,
    Explanation as ExplanationProto, Numeric as NumericProto, NumericGroup as NumericGroupProto,
    Relation as RelationProto, RelationType as RelationTypeProto, RoleType as RoleTypeProto, Thing as ThingProto,
    ThingType as ThingTypeProto, Value as ValueProto, ValueType as ValueTypeProto,
};

use super::{FromProto, IntoProto, TryFromProto};
use crate::{
    answer::{ConceptMap, ConceptMapGroup, Explainable, Explainables, Numeric, NumericGroup},
    concept::{
        Annotation, Attribute, AttributeType, Concept, Entity, EntityType, Relation, RelationType, RoleType,
        RootThingType, ScopedLabel, Thing, ThingType, Transitivity, Value, ValueType,
    },
    error::{ConnectionError, InternalError},
    logic::{Explanation, Rule},
    Result,
};

impl IntoProto<i32> for Transitivity {
    fn into_proto(self) -> i32 {
        match self {
            Self::Explicit => TransitivityProto::Explicit.into(),
            Self::Transitive => TransitivityProto::Transitive.into(),
        }
    }
}

impl IntoProto<AnnotationProto> for Annotation {
    fn into_proto(self) -> AnnotationProto {
        match self {
            Self::Key => AnnotationProto { annotation: Some(annotation::Annotation::Key(annotation::Key {})) },
            Self::Unique => AnnotationProto { annotation: Some(annotation::Annotation::Unique(annotation::Unique {})) },
        }
    }
}

impl TryFromProto<NumericGroupProto> for NumericGroup {
    fn try_from_proto(proto: NumericGroupProto) -> Result<Self> {
        let NumericGroupProto { owner: owner_proto, number: number_proto } = proto;
        let owner = Concept::try_from_proto(owner_proto.ok_or(ConnectionError::MissingResponseField("owner"))?)?;
        let numeric = Numeric::try_from_proto(number_proto.ok_or(ConnectionError::MissingResponseField("number"))?)?;
        Ok(Self { owner, numeric })
    }
}

impl TryFromProto<NumericProto> for Numeric {
    fn try_from_proto(proto: NumericProto) -> Result<Self> {
        let NumericProto { value: value_proto } = proto;
        match value_proto {
            Some(NumericValue::LongValue(long)) => Ok(Self::Long(long)),
            Some(NumericValue::DoubleValue(double)) => Ok(Self::Double(double)),
            Some(NumericValue::Nan(_)) => Ok(Self::NaN),
            None => Err(ConnectionError::MissingResponseField("value").into()),
        }
    }
}

impl TryFromProto<ConceptMapGroupProto> for ConceptMapGroup {
    fn try_from_proto(proto: ConceptMapGroupProto) -> Result<Self> {
        let ConceptMapGroupProto { owner: owner_proto, concept_maps: concept_maps_proto } = proto;
        let owner = Concept::try_from_proto(owner_proto.ok_or(ConnectionError::MissingResponseField("owner"))?)?;
        let concept_maps = concept_maps_proto.into_iter().map(ConceptMap::try_from_proto).try_collect()?;
        Ok(Self { owner, concept_maps })
    }
}

impl TryFromProto<ConceptMapProto> for ConceptMap {
    fn try_from_proto(proto: ConceptMapProto) -> Result<Self> {
        let ConceptMapProto { map: map_proto, explainables: explainables_proto } = proto;
        let map = map_proto.into_iter().map(|(k, v)| Concept::try_from_proto(v).map(|v| (k, v))).try_collect()?;
        let explainables =
            explainables_proto.ok_or::<ConnectionError>(ConnectionError::MissingResponseField("explainables"))?;
        Ok(Self { map, explainables: Explainables::from_proto(explainables) })
    }
}

impl TryFromProto<ConceptProto> for Concept {
    fn try_from_proto(proto: ConceptProto) -> Result<Self> {
        let ConceptProto { concept: concept_proto } = proto;
        match concept_proto {
            Some(concept::Concept::EntityType(entity_type_proto)) => {
                Ok(Self::EntityType(EntityType::from_proto(entity_type_proto)))
            }
            Some(concept::Concept::RelationType(relation_type_proto)) => {
                Ok(Self::RelationType(RelationType::from_proto(relation_type_proto)))
            }
            Some(concept::Concept::AttributeType(attribute_type_proto)) => {
                AttributeType::try_from_proto(attribute_type_proto).map(Self::AttributeType)
            }

            Some(concept::Concept::RoleType(role_type_proto)) => {
                Ok(Self::RoleType(RoleType::from_proto(role_type_proto)))
            }

            Some(concept::Concept::Entity(entity_proto)) => Entity::try_from_proto(entity_proto).map(Self::Entity),
            Some(concept::Concept::Relation(relation_proto)) => {
                Relation::try_from_proto(relation_proto).map(Self::Relation)
            }
            Some(concept::Concept::Attribute(attribute_proto)) => {
                Attribute::try_from_proto(attribute_proto).map(Self::Attribute)
            }

            Some(concept::Concept::Value(value_proto)) => Value::try_from_proto(value_proto).map(Self::Value),

            Some(concept::Concept::ThingTypeRoot(_)) => Ok(Self::RootThingType(RootThingType)),
            None => Err(ConnectionError::MissingResponseField("concept").into()),
        }
    }
}

impl TryFromProto<ThingTypeProto> for ThingType {
    fn try_from_proto(proto: ThingTypeProto) -> Result<Self> {
        match proto.r#type {
            Some(thing_type::Type::EntityType(entity_type_proto)) => {
                Ok(Self::EntityType(EntityType::from_proto(entity_type_proto)))
            }
            Some(thing_type::Type::RelationType(relation_type_proto)) => {
                Ok(Self::RelationType(RelationType::from_proto(relation_type_proto)))
            }
            Some(thing_type::Type::AttributeType(attribute_type_proto)) => {
                AttributeType::try_from_proto(attribute_type_proto).map(Self::AttributeType)
            }
            Some(thing_type::Type::ThingTypeRoot(_)) => Ok(Self::RootThingType(RootThingType)),
            None => Err(ConnectionError::MissingResponseField("type").into()),
        }
    }
}

impl IntoProto<ThingTypeProto> for ThingType {
    fn into_proto(self) -> ThingTypeProto {
        let thing_type_inner_proto = match self {
            Self::EntityType(entity_type) => thing_type::Type::EntityType(entity_type.into_proto()),
            Self::RelationType(relation_type) => thing_type::Type::RelationType(relation_type.into_proto()),
            Self::AttributeType(attribute_type) => thing_type::Type::AttributeType(attribute_type.into_proto()),
            Self::RootThingType(_) => thing_type::Type::ThingTypeRoot(thing_type::Root {}),
        };
        ThingTypeProto { r#type: Some(thing_type_inner_proto) }
    }
}

impl FromProto<EntityTypeProto> for EntityType {
    fn from_proto(proto: EntityTypeProto) -> Self {
        let EntityTypeProto { label, is_root, is_abstract } = proto;
        Self { label, is_root, is_abstract }
    }
}

impl IntoProto<EntityTypeProto> for EntityType {
    fn into_proto(self) -> EntityTypeProto {
        let EntityType { label, is_root, is_abstract } = self;
        EntityTypeProto { label, is_root, is_abstract }
    }
}

impl FromProto<RelationTypeProto> for RelationType {
    fn from_proto(proto: RelationTypeProto) -> Self {
        let RelationTypeProto { label, is_root, is_abstract } = proto;
        Self { label, is_root, is_abstract }
    }
}

impl IntoProto<RelationTypeProto> for RelationType {
    fn into_proto(self) -> RelationTypeProto {
        let RelationType { label, is_root, is_abstract } = self;
        RelationTypeProto { label, is_root, is_abstract }
    }
}
impl TryFromProto<AttributeTypeProto> for AttributeType {
    fn try_from_proto(proto: AttributeTypeProto) -> Result<Self> {
        let AttributeTypeProto { label, is_root, is_abstract, value_type } = proto;
        Ok(Self { label, is_root, is_abstract, value_type: ValueType::try_from_proto(value_type)? })
    }
}

impl IntoProto<AttributeTypeProto> for AttributeType {
    fn into_proto(self) -> AttributeTypeProto {
        let AttributeType { label, is_root, is_abstract, value_type } = self;
        AttributeTypeProto { label, is_root, is_abstract, value_type: value_type.into_proto() }
    }
}

impl TryFromProto<i32> for ValueType {
    fn try_from_proto(proto: i32) -> Result<Self> {
        match ValueTypeProto::from_i32(proto) {
            Some(ValueTypeProto::Object) => Ok(Self::Object),
            Some(ValueTypeProto::Boolean) => Ok(Self::Boolean),
            Some(ValueTypeProto::Long) => Ok(Self::Long),
            Some(ValueTypeProto::Double) => Ok(Self::Double),
            Some(ValueTypeProto::String) => Ok(Self::String),
            Some(ValueTypeProto::Datetime) => Ok(Self::DateTime),
            None => Err(InternalError::EnumOutOfBounds(proto, "ValueType").into()),
        }
    }
}

impl IntoProto<i32> for ValueType {
    fn into_proto(self) -> i32 {
        match self {
            Self::Object => ValueTypeProto::Object.into(),
            Self::Boolean => ValueTypeProto::Boolean.into(),
            Self::Long => ValueTypeProto::Long.into(),
            Self::Double => ValueTypeProto::Double.into(),
            Self::String => ValueTypeProto::String.into(),
            Self::DateTime => ValueTypeProto::Datetime.into(),
        }
    }
}

impl FromProto<RoleTypeProto> for RoleType {
    fn from_proto(proto: RoleTypeProto) -> Self {
        let RoleTypeProto { scope, label, is_root, is_abstract } = proto;
        Self { label: ScopedLabel { scope, name: label }, is_root, is_abstract }
    }
}

impl IntoProto<RoleTypeProto> for RoleType {
    fn into_proto(self) -> RoleTypeProto {
        let RoleType { label: ScopedLabel { scope, name }, is_root, is_abstract } = self;
        RoleTypeProto { scope, label: name, is_root, is_abstract }
    }
}

impl TryFromProto<ThingProto> for Thing {
    fn try_from_proto(proto: ThingProto) -> Result<Self> {
        match proto.thing {
            Some(thing::Thing::Entity(entity_proto)) => Entity::try_from_proto(entity_proto).map(Self::Entity),
            Some(thing::Thing::Relation(relation_proto)) => {
                Relation::try_from_proto(relation_proto).map(Self::Relation)
            }
            Some(thing::Thing::Attribute(attribute_proto)) => {
                Attribute::try_from_proto(attribute_proto).map(Self::Attribute)
            }
            None => Err(ConnectionError::MissingResponseField("thing").into()),
        }
    }
}

impl IntoProto<ThingProto> for Thing {
    fn into_proto(self) -> ThingProto {
        let thing_inner_proto = match self {
            Self::Entity(entity) => thing::Thing::Entity(entity.into_proto()),
            Self::Relation(relation) => thing::Thing::Relation(relation.into_proto()),
            Self::Attribute(attribute) => thing::Thing::Attribute(attribute.into_proto()),
        };
        ThingProto { thing: Some(thing_inner_proto) }
    }
}

impl TryFromProto<EntityProto> for Entity {
    fn try_from_proto(proto: EntityProto) -> Result<Self> {
        let EntityProto { iid, entity_type, inferred } = proto;
        Ok(Self {
            iid: iid.into(),
            type_: EntityType::from_proto(entity_type.ok_or(ConnectionError::MissingResponseField("entity_type"))?),
            is_inferred: inferred,
        })
    }
}

impl IntoProto<EntityProto> for Entity {
    fn into_proto(self) -> EntityProto {
        let Self { iid, type_, is_inferred } = self;
        EntityProto { iid: iid.into(), entity_type: Some(type_.into_proto()), inferred: is_inferred }
    }
}

impl TryFromProto<RelationProto> for Relation {
    fn try_from_proto(proto: RelationProto) -> Result<Self> {
        let RelationProto { iid, relation_type, inferred } = proto;
        Ok(Self {
            iid: iid.into(),
            type_: RelationType::from_proto(
                relation_type.ok_or(ConnectionError::MissingResponseField("relation_type"))?,
            ),
            is_inferred: inferred,
        })
    }
}

impl IntoProto<RelationProto> for Relation {
    fn into_proto(self) -> RelationProto {
        let Self { iid, type_, is_inferred } = self;
        RelationProto { iid: iid.into(), relation_type: Some(type_.into_proto()), inferred: is_inferred }
    }
}

impl TryFromProto<AttributeProto> for Attribute {
    fn try_from_proto(proto: AttributeProto) -> Result<Self> {
        let AttributeProto { iid, attribute_type, value, inferred } = proto;
        Ok(Self {
            iid: iid.into(),
            type_: AttributeType::try_from_proto(
                attribute_type.ok_or(ConnectionError::MissingResponseField("attribute_type"))?,
            )?,
            value: Value::try_from_proto(value.ok_or(ConnectionError::MissingResponseField("value"))?)?,
            is_inferred: inferred,
        })
    }
}

impl IntoProto<AttributeProto> for Attribute {
    fn into_proto(self) -> AttributeProto {
        let Self { iid, type_, value, is_inferred } = self;
        AttributeProto {
            iid: iid.into(),
            attribute_type: Some(type_.into_proto()),
            value: Some(value.into_proto()),
            inferred: is_inferred,
        }
    }
}

impl TryFromProto<ValueProto> for Value {
    fn try_from_proto(proto: ValueProto) -> Result<Self> {
        match proto.value {
            Some(ValueProtoInner::Boolean(boolean)) => Ok(Self::Boolean(boolean)),
            Some(ValueProtoInner::Long(long)) => Ok(Self::Long(long)),
            Some(ValueProtoInner::Double(double)) => Ok(Self::Double(double)),
            Some(ValueProtoInner::String(string)) => Ok(Self::String(string)),
            Some(ValueProtoInner::DateTime(millis)) => {
                Ok(Self::DateTime(NaiveDateTime::from_timestamp_millis(millis).unwrap()))
            }
            None => Err(ConnectionError::MissingResponseField("value").into()),
        }
    }
}

impl IntoProto<ValueProto> for Value {
    fn into_proto(self) -> ValueProto {
        ValueProto {
            value: Some(match self {
                Self::Boolean(boolean) => ValueProtoInner::Boolean(boolean),
                Self::Long(long) => ValueProtoInner::Long(long),
                Self::Double(double) => ValueProtoInner::Double(double),
                Self::String(string) => ValueProtoInner::String(string),
                Self::DateTime(date_time) => ValueProtoInner::DateTime(date_time.timestamp_millis()),
            }),
        }
    }
}

impl FromProto<ExplainablesProto> for Explainables {
    fn from_proto(proto: ExplainablesProto) -> Self {
        let ExplainablesProto {
            relations: relations_proto,
            attributes: attributes_proto,
            ownerships: ownerships_proto,
        } = proto;
        let relations = relations_proto.into_iter().map(|(k, v)| (k, Explainable::from_proto(v))).collect();
        let attributes = attributes_proto.into_iter().map(|(k, v)| (k, Explainable::from_proto(v))).collect();
        let mut ownerships = HashMap::new();
        for (k1, owned) in ownerships_proto {
            for (k2, v) in owned.owned {
                ownerships.insert((k1.clone(), k2), Explainable::from_proto(v));
            }
        }

        Self { relations, attributes, ownerships }
    }
}

impl FromProto<ExplainableProto> for Explainable {
    fn from_proto(proto: ExplainableProto) -> Self {
        let ExplainableProto { conjunction, id } = proto;
        Self::new(conjunction, id)
    }
}

impl TryFromProto<ExplanationProto> for Explanation {
    fn try_from_proto(proto: ExplanationProto) -> Result<Self> {
        let ExplanationProto { rule, conclusion, condition, var_mapping } = proto;
        let variable_mapping = var_mapping.iter().map(|(k, v)| (k.clone(), v.clone().vars)).collect();
        Ok(Self {
            rule: Rule::try_from_proto(rule.ok_or(ConnectionError::MissingResponseField("rule"))?)?,
            conclusion: ConceptMap::try_from_proto(
                conclusion.ok_or(ConnectionError::MissingResponseField("conclusion"))?,
            )?,
            condition: ConceptMap::try_from_proto(
                condition.ok_or(ConnectionError::MissingResponseField("condition"))?,
            )?,
            variable_mapping,
        })
    }
}
