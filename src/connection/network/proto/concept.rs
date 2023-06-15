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
    attribute::{value::Value as ValueProtoInner, Value as ValueProto},
    attribute_type::ValueType as ValueTypeProto,
    concept,
    numeric::Value as NumericValue,
    r#type::{annotation, Annotation as AnnotationProto, Transitivity as TransitivityProto},
    thing, thing_type, Attribute as AttributeProto, AttributeType as AttributeTypeProto, Concept as ConceptProto,
    ConceptMap as ConceptMapProto, ConceptMapGroup as ConceptMapGroupProto, Entity as EntityProto,
    EntityType as EntityTypeProto, Numeric as NumericProto, NumericGroup as NumericGroupProto,
    Relation as RelationProto, RelationType as RelationTypeProto, RoleType as RoleTypeProto, Thing as ThingProto,
    ThingType as ThingTypeProto,
};

use super::{FromProto, IntoProto, TryFromProto};
use crate::{
    answer::{ConceptMap, ConceptMapGroup, Numeric, NumericGroup},
    concept::{
        Annotation, Attribute, AttributeType, Concept, Entity, EntityType, Relation, RelationType, RoleType,
        RootThingType, ScopedLabel, Thing, ThingType, Transitivity, Value, ValueType,
    },
    error::{ConnectionError, InternalError},
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
        let mut map = HashMap::with_capacity(proto.map.len());
        for (k, v) in proto.map {
            map.insert(k, Concept::try_from_proto(v)?);
        }
        Ok(Self { map })
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
        let EntityProto { iid, entity_type, inferred: _ } = proto;
        Ok(Self {
            iid: iid.into(),
            type_: EntityType::from_proto(entity_type.ok_or(ConnectionError::MissingResponseField("entity_type"))?),
        })
    }
}

impl IntoProto<EntityProto> for Entity {
    fn into_proto(self) -> EntityProto {
        EntityProto {
            iid: self.iid.into(),
            entity_type: Some(self.type_.into_proto()),
            inferred: false, // FIXME
        }
    }
}

impl TryFromProto<RelationProto> for Relation {
    fn try_from_proto(proto: RelationProto) -> Result<Self> {
        let RelationProto { iid, relation_type, inferred: _ } = proto;
        Ok(Self {
            iid: iid.into(),
            type_: RelationType::from_proto(
                relation_type.ok_or(ConnectionError::MissingResponseField("relation_type"))?,
            ),
        })
    }
}

impl IntoProto<RelationProto> for Relation {
    fn into_proto(self) -> RelationProto {
        RelationProto {
            iid: self.iid.into(),
            relation_type: Some(self.type_.into_proto()),
            inferred: false, // FIXME
        }
    }
}

impl TryFromProto<AttributeProto> for Attribute {
    fn try_from_proto(proto: AttributeProto) -> Result<Self> {
        let AttributeProto { iid, attribute_type, value, inferred: _ } = proto;
        Ok(Self {
            iid: iid.into(),
            type_: AttributeType::try_from_proto(
                attribute_type.ok_or(ConnectionError::MissingResponseField("attribute_type"))?,
            )?,
            value: Value::try_from_proto(value.ok_or(ConnectionError::MissingResponseField("value"))?)?,
        })
    }
}

impl IntoProto<AttributeProto> for Attribute {
    fn into_proto(self) -> AttributeProto {
        AttributeProto {
            iid: self.iid.into(),
            attribute_type: Some(self.type_.into_proto()),
            value: Some(self.value.into_proto()),
            inferred: false, // FIXME
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
