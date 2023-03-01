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
use typedb_protocol::{
    attribute::value::Value as ValueProto, attribute_type::ValueType, concept as concept_proto, numeric::Value,
    r#type::Encoding, Concept as ConceptProto, ConceptMap as ConceptMapProto, Numeric as NumericProto,
    Thing as ThingProto, Type as TypeProto,
};

use super::TryFromProto;
use crate::{
    answer::{ConceptMap, Numeric},
    concept::{
        Attribute, AttributeType, BooleanAttribute, BooleanAttributeType, Concept, DateTimeAttribute,
        DateTimeAttributeType, DoubleAttribute, DoubleAttributeType, Entity, EntityType, LongAttribute,
        LongAttributeType, Relation, RelationType, RoleType, RootAttributeType, RootThingType, ScopedLabel,
        StringAttribute, StringAttributeType, Thing, ThingType, Type,
    },
    connection::network::proto::FromProto,
    error::{ConnectionError, InternalError},
    Result,
};

impl TryFromProto<NumericProto> for Numeric {
    fn try_from_proto(proto: NumericProto) -> Result<Self> {
        match proto.value {
            Some(Value::LongValue(long)) => Ok(Numeric::Long(long)),
            Some(Value::DoubleValue(double)) => Ok(Numeric::Double(double)),
            Some(Value::Nan(_)) => Ok(Numeric::NaN),
            None => Err(ConnectionError::MissingResponseField("value").into()),
        }
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
        let concept = proto.concept.ok_or(ConnectionError::MissingResponseField("concept"))?;
        match concept {
            concept_proto::Concept::Thing(thing) => Ok(Self::Thing(Thing::try_from_proto(thing)?)),
            concept_proto::Concept::Type(type_) => Ok(Self::Type(Type::try_from_proto(type_)?)),
        }
    }
}

impl TryFromProto<i32> for Encoding {
    fn try_from_proto(proto: i32) -> Result<Self> {
        Self::from_i32(proto).ok_or(InternalError::EnumOutOfBounds(proto, "Encoding").into())
    }
}

impl TryFromProto<TypeProto> for Type {
    fn try_from_proto(proto: TypeProto) -> Result<Self> {
        match Encoding::try_from_proto(proto.encoding)? {
            Encoding::ThingType => Ok(Self::Thing(ThingType::Root(RootThingType::default()))),
            Encoding::EntityType => Ok(Self::Thing(ThingType::Entity(EntityType::from_proto(proto)))),
            Encoding::RelationType => Ok(Self::Thing(ThingType::Relation(RelationType::from_proto(proto)))),
            Encoding::AttributeType => Ok(Self::Thing(ThingType::Attribute(AttributeType::try_from_proto(proto)?))),
            Encoding::RoleType => Ok(Self::Role(RoleType::from_proto(proto))),
        }
    }
}

impl FromProto<TypeProto> for EntityType {
    fn from_proto(proto: TypeProto) -> Self {
        Self::new(proto.label)
    }
}

impl FromProto<TypeProto> for RelationType {
    fn from_proto(proto: TypeProto) -> Self {
        Self::new(proto.label)
    }
}

impl TryFromProto<i32> for ValueType {
    fn try_from_proto(proto: i32) -> Result<Self> {
        Self::from_i32(proto).ok_or(InternalError::EnumOutOfBounds(proto, "ValueType").into())
    }
}

impl TryFromProto<TypeProto> for AttributeType {
    fn try_from_proto(proto: TypeProto) -> Result<Self> {
        match ValueType::try_from_proto(proto.value_type)? {
            ValueType::Object => Ok(Self::Root(RootAttributeType::default())),
            ValueType::Boolean => Ok(Self::Boolean(BooleanAttributeType { label: proto.label })),
            ValueType::Long => Ok(Self::Long(LongAttributeType { label: proto.label })),
            ValueType::Double => Ok(Self::Double(DoubleAttributeType { label: proto.label })),
            ValueType::String => Ok(Self::String(StringAttributeType { label: proto.label })),
            ValueType::Datetime => Ok(Self::DateTime(DateTimeAttributeType { label: proto.label })),
        }
    }
}

impl FromProto<TypeProto> for RoleType {
    fn from_proto(proto: TypeProto) -> Self {
        Self::new(ScopedLabel::new(proto.scope, proto.label))
    }
}

impl TryFromProto<ThingProto> for Thing {
    fn try_from_proto(proto: ThingProto) -> Result<Self> {
        let encoding = proto.r#type.clone().ok_or(ConnectionError::MissingResponseField("type"))?.encoding;
        match Encoding::try_from_proto(encoding)? {
            Encoding::EntityType => Ok(Self::Entity(Entity::try_from_proto(proto)?)),
            Encoding::RelationType => Ok(Self::Relation(Relation::try_from_proto(proto)?)),
            Encoding::AttributeType => Ok(Self::Attribute(Attribute::try_from_proto(proto)?)),
            _ => todo!(),
        }
    }
}

impl TryFromProto<ThingProto> for Entity {
    fn try_from_proto(proto: ThingProto) -> Result<Self> {
        Ok(Self {
            type_: EntityType::from_proto(proto.r#type.ok_or(ConnectionError::MissingResponseField("type"))?),
            iid: proto.iid,
        })
    }
}

impl TryFromProto<ThingProto> for Relation {
    fn try_from_proto(proto: ThingProto) -> Result<Self> {
        Ok(Self {
            type_: RelationType::from_proto(proto.r#type.ok_or(ConnectionError::MissingResponseField("type"))?),
            iid: proto.iid,
        })
    }
}

impl TryFromProto<ThingProto> for Attribute {
    fn try_from_proto(proto: ThingProto) -> Result<Self> {
        let value = proto.value.and_then(|v| v.value).ok_or(ConnectionError::MissingResponseField("value"))?;

        let value_type = proto.r#type.ok_or(ConnectionError::MissingResponseField("type"))?.value_type;
        let iid = proto.iid;

        match ValueType::try_from_proto(value_type)? {
            ValueType::Object => todo!(),
            ValueType::Boolean => Ok(Self::Boolean(BooleanAttribute {
                value: if let ValueProto::Boolean(value) = value { value } else { unreachable!() },
                iid,
            })),
            ValueType::Long => Ok(Self::Long(LongAttribute {
                value: if let ValueProto::Long(value) = value { value } else { unreachable!() },
                iid,
            })),
            ValueType::Double => Ok(Self::Double(DoubleAttribute {
                value: if let ValueProto::Double(value) = value { value } else { unreachable!() },
                iid,
            })),
            ValueType::String => Ok(Self::String(StringAttribute {
                value: if let ValueProto::String(value) = value { value } else { unreachable!() },
                iid,
            })),
            ValueType::Datetime => Ok(Self::DateTime(DateTimeAttribute {
                value: if let ValueProto::DateTime(value) = value {
                    NaiveDateTime::from_timestamp_opt(value / 1000, (value % 1000) as u32 * 1_000_000).unwrap()
                } else {
                    unreachable!()
                },
                iid,
            })),
        }
    }
}
