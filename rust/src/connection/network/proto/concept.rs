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

use std::collections::HashMap;

use crate::concept::temporal::{Datetime, DatetimeTZ};
use itertools::Itertools;
use time::Date;
use typedb_protocol::{
    Attribute as AttributeProto, AttributeType as AttributeTypeProto, Concept as ConceptProto,
    ConceptDocument as ConceptDocumentProto, ConceptRow as ConceptRowProto, Entity as EntityProto,
    EntityType as EntityTypeProto, Relation as RelationProto, RelationType as RelationTypeProto,
    RoleType as RoleTypeProto, Type as TypeProto, Value as ValueProto, ValueType as ValueTypeStructProto, concept,
    concept_document::{self, node::leaf::Leaf as LeafProto},
    row_entry::Entry,
    value::{self as value_proto, Value as ValueProtoInner, datetime_tz::Timezone as TimezoneProto},
    value_type::ValueType as ValueTypeProto,
};

use super::{FromProto, IntoProto, TryFromProto};
use crate::{
    Error, Result,
    answer::concept_document::{Leaf, Node},
    concept::{
        Attribute, AttributeType, Concept, Entity, EntityType, Kind, Relation, RelationType, RoleType, Value,
        ValueType,
        type_::Type,
        value::{Decimal, Duration, TimeZone},
    },
    error::{
        ConnectionError,
        ConnectionError::{
            ListsNotImplemented, MissingResponseField, ValueStructNotImplemented, ValueTimeZoneNameNotRecognised,
            ValueTimeZoneOffsetNotRecognised,
        },
    },
};

impl TryFromProto<ConceptRowProto> for (Vec<Option<Concept>>, Option<Vec<u8>>) {
    fn try_from_proto(proto: ConceptRowProto) -> Result<Self> {
        let mut row = Vec::new();
        for entry in proto.row {
            match entry.entry.ok_or(Error::from(MissingResponseField { field: "ConceptRow.entry" }))? {
                Entry::Empty(_) => row.push(None),
                Entry::Concept(concept_proto) => row.push(Some(Concept::try_from_proto(concept_proto)?)),
                Entry::Value(value_proto) => row.push(Some(Concept::Value(Value::try_from_proto(value_proto)?))),
                Entry::ConceptList(_) | Entry::ValueList(_) => {
                    return Err(ListsNotImplemented.into());
                }
            }
        }
        let involved_blocks = proto.involved_blocks;
        Ok((row, involved_blocks))
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
                Ok(Self::AttributeType(AttributeType::from_proto(attribute_type_proto)))
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

            None => Err(MissingResponseField { field: "concept" }.into()),
        }
    }
}

impl TryFromProto<TypeProto> for Type {
    fn try_from_proto(proto: TypeProto) -> Result<Self> {
        let TypeProto { r#type: type_proto } = proto;
        match type_proto {
            Some(typedb_protocol::r#type::Type::EntityType(entity_type_proto)) => {
                Ok(Self::EntityType(EntityType::from_proto(entity_type_proto)))
            }
            Some(typedb_protocol::r#type::Type::RelationType(relation_type_proto)) => {
                Ok(Self::RelationType(RelationType::from_proto(relation_type_proto)))
            }
            Some(typedb_protocol::r#type::Type::AttributeType(attribute_type_proto)) => {
                Ok(Self::AttributeType(AttributeType::from_proto(attribute_type_proto)))
            }
            Some(typedb_protocol::r#type::Type::RoleType(role_type_proto)) => {
                Ok(Self::RoleType(RoleType::from_proto(role_type_proto)))
            }

            None => Err(MissingResponseField { field: "type" }.into()),
        }
    }
}

impl TryFromProto<concept_document::Node> for Node {
    fn try_from_proto(proto: concept_document::Node) -> Result<Self> {
        match proto.node {
            Some(concept_document::node::Node::Map(map)) => Ok(Self::Map(HashMap::try_from_proto(map)?)),
            Some(concept_document::node::Node::List(list)) => Ok(Self::List(Vec::try_from_proto(list)?)),
            Some(concept_document::node::Node::Leaf(leaf)) => {
                let result: Result<Option<Leaf>> = Option::try_from_proto(leaf);
                Ok(Self::Leaf(result?))
            }
            None => Err(MissingResponseField { field: "node" }.into()),
        }
    }
}

impl TryFromProto<ConceptDocumentProto> for Option<Node> {
    fn try_from_proto(proto: ConceptDocumentProto) -> Result<Self> {
        let ConceptDocumentProto { root: root_proto } = proto;
        match root_proto {
            None => Ok(None),
            Some(node_proto) => Node::try_from_proto(node_proto).map(Some),
        }
    }
}

impl TryFromProto<concept_document::node::Map> for HashMap<String, Node> {
    fn try_from_proto(proto: concept_document::node::Map) -> Result<Self> {
        let concept_document::node::Map { map } = proto;
        map.into_iter().map(|(var, node_proto)| Node::try_from_proto(node_proto).map(|node| (var, node))).try_collect()
    }
}

impl TryFromProto<concept_document::node::List> for Vec<Node> {
    fn try_from_proto(proto: concept_document::node::List) -> Result<Self> {
        let concept_document::node::List { list } = proto;
        list.into_iter().map(Node::try_from_proto).try_collect()
    }
}

impl TryFromProto<concept_document::node::Leaf> for Option<Leaf> {
    fn try_from_proto(proto: concept_document::node::Leaf) -> Result<Self> {
        match proto.leaf {
            Some(LeafProto::Empty(_)) => Ok(None),
            Some(LeafProto::EntityType(entity_type_proto)) => {
                Ok(Some(Leaf::Concept(Concept::EntityType(EntityType::from_proto(entity_type_proto)))))
            }
            Some(LeafProto::RelationType(relation_type_proto)) => {
                Ok(Some(Leaf::Concept(Concept::RelationType(RelationType::from_proto(relation_type_proto)))))
            }
            Some(LeafProto::AttributeType(attribute_type_proto)) => {
                Ok(Some(Leaf::Concept(Concept::AttributeType(AttributeType::from_proto(attribute_type_proto)))))
            }
            Some(LeafProto::RoleType(role_type_proto)) => {
                Ok(Some(Leaf::Concept(Concept::RoleType(RoleType::from_proto(role_type_proto)))))
            }
            Some(LeafProto::ValueType(value_type_proto)) => {
                ValueType::try_from_proto(value_type_proto).map(|value_type| Some(Leaf::ValueType(value_type)))
            }
            Some(LeafProto::Attribute(attribute_proto)) => {
                Attribute::try_from_proto(attribute_proto).map(|attr| Some(Leaf::Concept(Concept::Attribute(attr))))
            }
            Some(LeafProto::Value(value_proto)) => {
                Value::try_from_proto(value_proto).map(|value| Some(Leaf::Concept(Concept::Value(value))))
            }
            Some(LeafProto::Kind(kind_proto)) => Kind::try_from_proto(kind_proto).map(|kind| Some(Leaf::Kind(kind))),
            None => Ok(None),
        }
    }
}

impl FromProto<EntityTypeProto> for EntityType {
    fn from_proto(proto: EntityTypeProto) -> Self {
        let EntityTypeProto { label } = proto;
        Self { label }
    }
}

impl FromProto<RelationTypeProto> for RelationType {
    fn from_proto(proto: RelationTypeProto) -> Self {
        let RelationTypeProto { label } = proto;
        Self { label }
    }
}

impl FromProto<AttributeTypeProto> for AttributeType {
    fn from_proto(proto: AttributeTypeProto) -> Self {
        let AttributeTypeProto { label, value_type } = proto;
        let value_type = match value_type {
            None => None,
            Some(proto) => Some(ValueType::from_proto(proto.value_type.unwrap())),
        };
        Self { label, value_type }
    }
}

impl FromProto<RoleTypeProto> for RoleType {
    fn from_proto(proto: RoleTypeProto) -> Self {
        let RoleTypeProto { label } = proto;
        Self { label }
    }
}

impl FromProto<ValueTypeProto> for ValueType {
    fn from_proto(proto: ValueTypeProto) -> Self {
        match proto {
            ValueTypeProto::Boolean(_) => Self::Boolean,
            ValueTypeProto::Integer(_) => Self::Integer,
            ValueTypeProto::Double(_) => Self::Double,
            ValueTypeProto::String(_) => Self::String,
            ValueTypeProto::Decimal(_) => Self::Decimal,
            ValueTypeProto::Date(_) => Self::Date,
            ValueTypeProto::Datetime(_) => Self::Datetime,
            ValueTypeProto::DatetimeTz(_) => Self::DatetimeTZ,
            ValueTypeProto::Duration(_) => Self::Duration,
            ValueTypeProto::Struct(typedb_protocol::value_type::Struct { name }) => Self::Struct(name),
        }
    }
}

impl TryFromProto<ValueTypeStructProto> for ValueType {
    fn try_from_proto(proto: ValueTypeStructProto) -> Result<Self> {
        let ValueTypeStructProto { value_type: value_type_proto } = proto;
        Ok(ValueType::from_proto(value_type_proto.ok_or(MissingResponseField { field: "value_type" })?))
    }
}

impl TryFromProto<EntityProto> for Entity {
    fn try_from_proto(proto: EntityProto) -> Result<Self> {
        let EntityProto { iid, entity_type } = proto;
        Ok(Self { iid: iid.into(), type_: entity_type.map(EntityType::from_proto) })
    }
}

impl TryFromProto<RelationProto> for Relation {
    fn try_from_proto(proto: RelationProto) -> Result<Self> {
        let RelationProto { iid, relation_type } = proto;
        Ok(Self { iid: iid.into(), type_: relation_type.map(RelationType::from_proto) })
    }
}

impl TryFromProto<AttributeProto> for Attribute {
    fn try_from_proto(proto: AttributeProto) -> Result<Self> {
        let AttributeProto { iid, attribute_type, value } = proto;
        let type_ = attribute_type.map(AttributeType::from_proto);
        Ok(Self {
            iid: iid.into(),
            type_,
            value: Value::try_from_proto(value.ok_or(MissingResponseField { field: "value" })?)?,
        })
    }
}

impl TryFromProto<ValueProto> for Value {
    fn try_from_proto(proto: ValueProto) -> Result<Self> {
        match proto.value {
            Some(ValueProtoInner::Boolean(boolean)) => Ok(Self::Boolean(boolean)),
            Some(ValueProtoInner::Integer(integer)) => Ok(Self::Integer(integer)),
            Some(ValueProtoInner::Double(double)) => Ok(Self::Double(double)),
            Some(ValueProtoInner::String(string)) => Ok(Self::String(string)),
            Some(ValueProtoInner::Decimal(decimal)) => {
                Ok(Self::Decimal(Decimal::from_parts(decimal.integer, decimal.fractional)))
            }
            Some(ValueProtoInner::Date(date)) => Ok(Self::Date(
                Date::from_julian_day(
                    date.num_days_since_ce
                        .checked_add(1_721_425)
                        .ok_or_else(|| Error::Other("Date day count overflow".into()))?,
                )
                .map_err(|e| Error::Other(e.to_string()))?,
            )),
            Some(ValueProtoInner::Datetime(datetime)) => {
                Ok(Self::Datetime(Datetime::from_timestamp(datetime.seconds, datetime.nanos)?))
            }
            Some(ValueProtoInner::DatetimeTz(datetime_tz)) => {
                let datetime = datetime_tz
                    .datetime
                    .ok_or(Error::from(MissingResponseField { field: "Value.datetime_tz.datetime" }))?;
                let naive_datetime = Datetime::from_timestamp(datetime.seconds, datetime.nanos)?;
                let timezone = datetime_tz
                    .timezone
                    .ok_or(Error::from(MissingResponseField { field: "Value.datetime_tz.timezone" }))?;

                let time_zone = match timezone {
                    TimezoneProto::Named(name) => TimeZone::iana(&name)
                        .map_err(|_| Error::from(ValueTimeZoneNameNotRecognised { time_zone: name }))?,
                    TimezoneProto::Offset(offset_seconds) => TimeZone::fixed(offset_seconds)
                        .map_err(|_| Error::from(ValueTimeZoneOffsetNotRecognised { offset: offset_seconds }))?,
                };
                Ok(Self::DatetimeTZ(time_zone.from_utc_datetime(&naive_datetime)?))
            }
            Some(ValueProtoInner::Duration(duration)) => {
                Ok(Self::Duration(crate::concept::value::Duration::new(duration.months, duration.days, duration.nanos)))
            }
            Some(ValueProtoInner::Struct(_struct)) => {
                Err(ValueStructNotImplemented.into())
                // TODO: not implemented yet
            }
            None => Err(MissingResponseField { field: "Value.value" }.into()),
        }
    }
}

impl TryFromProto<i32> for Kind {
    fn try_from_proto(kind: i32) -> Result<Self> {
        use concept_document::node::leaf::Kind as KindProto;
        match KindProto::try_from(kind) {
            Ok(KindProto::Entity) => Ok(Self::Entity),
            Ok(KindProto::Relation) => Ok(Self::Relation),
            Ok(KindProto::Attribute) => Ok(Self::Attribute),
            Ok(KindProto::Role) => Ok(Self::Role),
            Err(_) => Err(ConnectionError::UnexpectedKind { kind }.into()),
        }
    }
}

// Into proto for selected concepts
impl IntoProto<ValueProto> for Value {
    fn into_proto(self) -> ValueProto {
        let value = match self {
            Value::Boolean(v) => ValueProtoInner::Boolean(v),
            Value::Integer(v) => ValueProtoInner::Integer(v),
            Value::Double(v) => ValueProtoInner::Double(v),
            Value::Decimal(v) => ValueProtoInner::Decimal(v.into_proto()),
            Value::String(v) => ValueProtoInner::String(v),
            Value::Date(v) => ValueProtoInner::Date(v.into_proto()),
            Value::Datetime(v) => ValueProtoInner::Datetime(v.into_proto()),
            Value::DatetimeTZ(v) => ValueProtoInner::DatetimeTz(v.into_proto()),
            Value::Duration(v) => ValueProtoInner::Duration(v.into_proto()),
            Value::Struct(_, _) => unreachable!("Unimplemented struct inputs"),
        };
        ValueProto { value: Some(value) }
    }
}

impl IntoProto<EntityProto> for Entity {
    fn into_proto(self) -> EntityProto {
        EntityProto { iid: self.iid.bytes_cloned(), entity_type: None }
    }
}

impl IntoProto<RelationProto> for Relation {
    fn into_proto(self) -> RelationProto {
        RelationProto { iid: self.iid.bytes_cloned(), relation_type: None }
    }
}

impl IntoProto<AttributeProto> for Attribute {
    fn into_proto(self) -> AttributeProto {
        AttributeProto { iid: self.iid.bytes_cloned(), attribute_type: None, value: None }
    }
}

impl IntoProto<value_proto::Decimal> for Decimal {
    fn into_proto(self) -> value_proto::Decimal {
        let Self { integer, fractional } = self;
        value_proto::Decimal { integer, fractional }
    }
}

impl IntoProto<value_proto::Duration> for Duration {
    fn into_proto(self) -> value_proto::Duration {
        let Self { months, days, nanos } = self;
        value_proto::Duration { months, days, nanos }
    }
}

impl IntoProto<value_proto::Date> for Date {
    fn into_proto(self) -> value_proto::Date {
        value_proto::Date { num_days_since_ce: self.to_julian_day() - 1_721_425 }
    }
}

impl IntoProto<value_proto::Datetime> for Datetime {
    fn into_proto(self) -> value_proto::Datetime {
        {
            let (seconds, nanos) = self.wire_parts();
            value_proto::Datetime { seconds, nanos }
        }
    }
}

impl IntoProto<value_proto::DatetimeTz> for DatetimeTZ {
    fn into_proto(self) -> value_proto::DatetimeTz {
        let date_time = self.naive_utc().into_proto();
        let timezone = match self.timezone() {
            TimeZone::IANA(tz) => value_proto::datetime_tz::Timezone::Named(tz.name().to_string()),
            TimeZone::Fixed(fixed) => value_proto::datetime_tz::Timezone::Offset(fixed.whole_seconds()),
        };
        value_proto::DatetimeTz { datetime: Some(date_time), timezone: Some(timezone) }
    }
}

#[cfg(test)]
mod temporal_tests {
    use super::*;
    #[test]
    fn datetime_wire_roundtrip_and_errors() {
        for (seconds, nanos) in [(0, 0), (-1, 999999999), (1483228799, 1500000000)] {
            for zone in [TimeZone::iana("US/Eastern").unwrap(), TimeZone::fixed(1172).unwrap()] {
                let native = zone.from_utc_datetime(&Datetime::from_timestamp(seconds, nanos).unwrap()).unwrap();
                let proto: value_proto::DatetimeTz = native.into_proto();
                let decoded =
                    Value::try_from_proto(ValueProto { value: Some(ValueProtoInner::DatetimeTz(proto)) }).unwrap();
                let Value::DatetimeTZ(decoded) = decoded else { panic!("wrong value kind") };
                assert_eq!(decoded.wire_parts(), (seconds, nanos));
                assert_eq!(decoded.timezone(), zone);
            }
        }
        for (seconds, nanos) in [(i64::MAX, 0), (0, 1000000000), (59, 2000000000)] {
            assert!(
                Value::try_from_proto(ValueProto {
                    value: Some(ValueProtoInner::Datetime(value_proto::Datetime { seconds, nanos }))
                })
                .is_err()
            );
        }
        assert!(
            Value::try_from_proto(ValueProto {
                value: Some(ValueProtoInner::Date(value_proto::Date { num_days_since_ce: i32::MAX }))
            })
            .is_err()
        );
    }
}
